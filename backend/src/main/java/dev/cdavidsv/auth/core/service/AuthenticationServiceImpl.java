package dev.cdavidsv.auth.core.service;

import dev.cdavidsv.auth.core.exception.*;
import dev.cdavidsv.auth.core.model.entity.*;
import dev.cdavidsv.auth.core.repository.SessionRepository;
import dev.cdavidsv.auth.core.repository.UserMfaMethodRepository;
import dev.cdavidsv.auth.core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private record SessionCreationResult(Session session, String accessToken, String refreshToken, Instant accessTokenExpiresAt, Instant sessionExpiresAt) {}

    private final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private final long SESSION_LIFETIME_SECONDS;
    private final long REFRESH_TOKEN_GRACE_PERIOD_SECONDS;
    private final long LOGIN_REFERENCE_EXPIRATION_SECONDS;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final UserMfaMethodRepository userMfaMethodRepository;
    private final TokenService tokenService;
    private final IpGeolocationService ipGeolocationService;
    private final UserAgentService userAgentService;
    private final MfaService mfaService;
    private final RedisTemplate<String, Object> redisTemplate;

    public AuthenticationServiceImpl(@Value("${session.lifetime-seconds}") long sessionLifetimeSeconds, @Value("${refresh-token.grace-period-seconds}") long refreshTokenGracePeriodSeconds, @Value("${login-reference.expiration-seconds}") long loginReferenceExpirationSeconds, BCryptPasswordEncoder passwordEncoder, UserRepository userRepository, SessionRepository sessionRepository, UserMfaMethodRepository userMfaMethodRepository, TokenService tokenService, IpGeolocationService ipGeolocationService, UserAgentService userAgentService, MfaService mfaService, RedisTemplate<String, Object> redisTemplate) {
        this.SESSION_LIFETIME_SECONDS = sessionLifetimeSeconds;
        this.REFRESH_TOKEN_GRACE_PERIOD_SECONDS = refreshTokenGracePeriodSeconds;
        this.LOGIN_REFERENCE_EXPIRATION_SECONDS = loginReferenceExpirationSeconds;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userMfaMethodRepository = userMfaMethodRepository;
        this.sessionRepository = sessionRepository;
        this.tokenService = tokenService;
        this.ipGeolocationService = ipGeolocationService;
        this.userAgentService = userAgentService;
        this.mfaService = mfaService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * @param registerUserRequest request containing the username, password, email, and IP address of the user to register
     * @return RegisterResponse containing the registered user and a TokenPair with the access token, refresh token, and expiration time
     */
    @Override
    public RegisterResponse registerUser(RegisterUserRequest registerUserRequest) {
        User user = new User(
                null,
                registerUserRequest.email().toLowerCase(),
                registerUserRequest.username(),
                passwordEncoder.encode(registerUserRequest.password()),
                false,
                UserStatus.ACTIVE,
                null,
                null,
                null,
                null
        );

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyRegisteredException();
        }

        UserMfaMethod userMfaMethod = new UserMfaMethod(
                null,
                user,
                MfaMethod.EMAIL,
                null
        );

        userMfaMethodRepository.save(userMfaMethod);

        SessionCreationResult sessionCreationResult = createSession(user, registerUserRequest.ipAddress(), registerUserRequest.userAgent());

        return new RegisterResponse(
                user,
                new TokenPair(
                        sessionCreationResult.accessToken(),
                        sessionCreationResult.refreshToken(),
                        sessionCreationResult.accessTokenExpiresAt().toEpochMilli()
                )
        );
    }

    /**
     * @param authenticateUserRequest request containing the email, password, IP address, and user agent of the user to authenticate
     * @return AuthenticateResult containing the authenticated user, access token, refresh token, and expiration time
     */
    @Override
    public AuthenticationResult authenticate(AuthenticateUserRequest authenticateUserRequest) {
        User user = userRepository.findByEmail(authenticateUserRequest.email().toLowerCase()).orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(authenticateUserRequest.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw getExceptionForUserStatus(user.getStatus());
        }

        if (user.getMfaMethods() != null && !user.getMfaMethods().isEmpty()) {
            MfaService.InitiateMfaResponse mfaResponse = mfaService.initiate(user);

            return new AuthenticationResult.MfaRequired(
                    user.getId().toString(),
                    mfaResponse.ticket(),
                    createLoginReference(user.getId().toString()),
                    user.getMfaMethods().stream().map(userMfaMethod -> userMfaMethod.getMfaMethod().toString()).toList(),
                    Instant.now().plusSeconds(LOGIN_REFERENCE_EXPIRATION_SECONDS).toEpochMilli()
            );
        }

        SessionCreationResult sessionCreationResult = createSession(user, authenticateUserRequest.ipAddress(), authenticateUserRequest.userAgent());

        return new AuthenticationResult.Authenticated(
                user.getId().toString(),
                new TokenPair(
                        sessionCreationResult.accessToken(),
                        sessionCreationResult.refreshToken(),
                        sessionCreationResult.accessTokenExpiresAt().toEpochMilli()
                )
        );
    }

    /**
     * @param request request containing the ticket, login reference ID, MFA method, code, IP address, and user agent
     * @return TokenPair containing the new access token, refresh token, and expiration time
     */
    @Override
    public TokenPair verifyMfaCode(VerifyMfaRequest request) {
        if (!validLoginReference(request.userId(), request.loginReferenceId())) {
            throw new InvalidMfaCodeException();
        }

        Optional<User> user = userRepository.findById(UUID.fromString(request.userId()));
        if (user.isEmpty()) {
            throw new InvalidMfaCodeException();
        }

        boolean valid;
        try {
            valid = mfaService.verify(user.get(), request.ticket(), request.method(), request.code());
        } catch (TicketNotFoundException | InvalidMfaProviderException e) {
            logger.warn("Error verifying MFA code for userId={}, ticket={}, method={}: {}", request.userId(), request.ticket(), request.method(), e.getMessage());
            throw new InvalidMfaCodeException();
        }

        if (!valid) {
            throw new InvalidMfaCodeException();
        }

        SessionCreationResult result = createSession(user.get(), request.ipAddress(), request.userAgent());
        return new TokenPair(
                result.accessToken(),
                result.refreshToken(),
                result.accessTokenExpiresAt().toEpochMilli()
        );
    }

    /**
     * @param sessionId session ID of the session to terminate
     */
    @Override
    public boolean terminateSession(UUID sessionId) {
        Session session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            session.setRevokedAt(Instant.now());
            session.setPreviousRefreshTokenHash(null);
            session.setCurrentRefreshTokenHash(null);
            sessionRepository.save(session);
            return true;
        }

        return false;
    }

    /**
     * @param refreshToken refresh token to use for refreshing the access token
     * @return RefreshAccessTokenResult containing the new access token, refresh token, and expiration time
     */
    @Override
    public TokenPair refreshAccessToken(String refreshToken) {
        String hashedRefreshToken = tokenService.hash(refreshToken);
        Optional<Session> session = sessionRepository.findByCurrentRefreshTokenHash(hashedRefreshToken);

        if (session.isPresent()) {
            Session s = session.get();
            if (s.getRevokedAt() != null || s.getExpiresAt().isBefore(Instant.now())) {
                throw new SessionExpiredException();
            }

            return rotateRefreshToken(s);
        }

        session = sessionRepository.findByPreviousRefreshTokenHash(hashedRefreshToken);
        if (session.isPresent()) {
            Session s = session.get();

            boolean withinGracePeriod = s.getUpdatedAt().plusSeconds(REFRESH_TOKEN_GRACE_PERIOD_SECONDS).isAfter(Instant.now());
            if (withinGracePeriod) {
                return reissueCurrentRefreshToken(s);
            } else {
                handleRefreshTokenReuse(s);
            }
        }

        throw new InvalidRefreshTokenException();
    }

    /**
     *
     * @param s Session object
     * @return TokenPair containing the new access token, refresh token, and expiration time
     */
    private TokenPair rotateRefreshToken(Session s) {
        TokenService.AccessTokenResult accessTokenResult = tokenService.generateAccessToken(s.getUser(), s.getId().toString());
        String newRefreshToken = tokenService.generateRefreshToken();
        String newHashedRefreshToken = tokenService.hash(newRefreshToken);

        s.setPreviousRefreshTokenHash(s.getCurrentRefreshTokenHash());
        s.setCurrentRefreshTokenHash(newHashedRefreshToken);
        s.setUpdatedAt(Instant.now());
        sessionRepository.save(s);

        return new TokenPair(
                accessTokenResult.token(),
                newRefreshToken,
                accessTokenResult.expiresAt().toEpochMilli()
        );
    }

    /**
     *
     * @param s Session object
     * @return TokenPair containing the new access token, current refresh token, and expiration time
     */
    private TokenPair reissueCurrentRefreshToken(Session s) {
        TokenService.AccessTokenResult accessTokenResult = tokenService.generateAccessToken(s.getUser(), s.getId().toString());

        return new TokenPair(
                accessTokenResult.token(),
                s.getCurrentRefreshTokenHash(),
                accessTokenResult.expiresAt().toEpochMilli()
        );
    }

    /**
     * Handles the case where a refresh token is reused after it has been rotated. This indicates a potential security issue.
     *
     * @param s Session object
     */
    private void handleRefreshTokenReuse(Session s) {
        s.setRevokedAt(Instant.now());
        s.setPreviousRefreshTokenHash(null);
        s.setCurrentRefreshTokenHash(null);
        sessionRepository.save(s);

        sessionRepository.revokeAllByUserId(s.getUser().getId());
        logger.warn("Refresh token reuse detected, userId={}, sessionId={}", s.getUser().getId(), s.getId());
    }

    /**
     * @param user User object
     * @param ipAddress Ip address of the requesting user
     * @param userAgent User agent of the requesting user
     * @return SessionCreationResult containing the created session and tokens
     */
    private SessionCreationResult createSession(User user, String ipAddress, String userAgent) {
        UUID sessionId = UUID.randomUUID();
        TokenService.AccessTokenResult accessTokenResult = tokenService.generateAccessToken(user, sessionId.toString());
        String refreshToken = tokenService.generateRefreshToken();
        String hashedRefreshToken = tokenService.hash(refreshToken);

        Instant now = Instant.now();
        Instant sessionExpiresAt = now.plusSeconds(SESSION_LIFETIME_SECONDS);

        IpGeolocationService.GeolocationResult geolocationResult = ipGeolocationService.getIpGeolocation(ipAddress).orElse(null);
        UserAgentService.UserAgentInfo userAgentInfo = userAgentService.parseUserAgent(userAgent);

        String location = "Unknown location";
        if  (geolocationResult != null) {
            location = geolocationResult.city() + ", " + geolocationResult.region() + ", " + geolocationResult.country();
        }

        Session session = new Session(
                sessionId,
                user,
                null,
                hashedRefreshToken,
                ipAddress,
                userAgentInfo.deviceName(),
                userAgentInfo.operatingSystem(),
                userAgentInfo.browser(),
                location,
                now,
                sessionExpiresAt,
                now,
                null
        );

        session = sessionRepository.save(session);

        return new SessionCreationResult(
                session,
                accessTokenResult.token(),
                refreshToken,
                accessTokenResult.expiresAt(),
                sessionExpiresAt
        );
    }

    /**
     * Returns the appropriate exception based on the user's status.
     *
     * @param userStatus UserStatus enum value
     * @return ApiException corresponding to the user's status
     */
    private ApiException getExceptionForUserStatus(UserStatus userStatus) {
        if (userStatus == UserStatus.SUSPENDED) {
            return new UserSuspendedException();
        } else if (userStatus == UserStatus.DEACTIVATED) {
            return new UserDeactivatedException();
        } else {
            return new InvalidCredentialsException();
        }
    }

    /**
     * Creates a unique login reference for the user.
     *
     * @param userId The ID of the user for whom the login reference is being created.
     * @return The generated login reference ID.
     */
    private String createLoginReference(String userId) {
        String referenceId = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("login_reference:%s:%s".formatted(userId, referenceId), userId, Duration.ofSeconds(LOGIN_REFERENCE_EXPIRATION_SECONDS));
        return referenceId;
    }

    /**
     * Verifies the provided login reference ID for the user.
     *
     * @param referenceId The login reference ID to verify.
     * @return boolean indicating whether the login reference is valid for the user.
     */
    private boolean validLoginReference(String userId, String referenceId) {
        String key = "login_reference:%s:%s".formatted(userId, referenceId);
        String storedUserId = (String) redisTemplate.opsForValue().get(key);
        return storedUserId != null && storedUserId.equals(userId);
    }

    /**
     * Invalidates the provided login reference ID for the user.
     *
     * @param referenceId The login reference ID to invalidate.
     */
    private void invalidateLoginReference(String userId, String referenceId) {
        String key = "login_reference:%s:%s".formatted(userId, referenceId);
        redisTemplate.delete(key);
    }
}
