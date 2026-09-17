package dev.cdavidsv.auth.core.service;

import dev.cdavidsv.auth.core.exception.InvalidCredentialsException;
import dev.cdavidsv.auth.core.exception.InvalidRefreshTokenException;
import dev.cdavidsv.auth.core.exception.SessionExpiredException;
import dev.cdavidsv.auth.core.exception.UserAlreadyRegisteredException;
import dev.cdavidsv.auth.core.model.entity.Session;
import dev.cdavidsv.auth.core.model.entity.User;
import dev.cdavidsv.auth.core.model.entity.UserStatus;
import dev.cdavidsv.auth.core.repository.SessionRepository;
import dev.cdavidsv.auth.core.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private record SessionCreationResult(Session session, String accessToken, String refreshToken, Instant accessTokenExpiresAt, Instant sessionExpiresAt) {}

    private final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private final long SESSION_LIFETIME_SECONDS;
    private final long REFRESH_TOKEN_GRACE_PERIOD_SECONDS;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final TokenService tokenService;
    private final IpGeolocationService ipGeolocationService;
    private final UserAgentService userAgentService;

    public AuthenticationServiceImpl(@Value("${session.lifetime-seconds}") long sessionLifetimeSeconds, @Value("${refresh-token.grace-period-seconds}") long refreshTokenGracePeriodSeconds, BCryptPasswordEncoder passwordEncoder, UserRepository userRepository, SessionRepository sessionRepository, TokenService tokenService, IpGeolocationService ipGeolocationService, UserAgentService userAgentService) {
        this.SESSION_LIFETIME_SECONDS = sessionLifetimeSeconds;
        this.REFRESH_TOKEN_GRACE_PERIOD_SECONDS = refreshTokenGracePeriodSeconds;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.tokenService = tokenService;
        this.ipGeolocationService = ipGeolocationService;
        this.userAgentService = userAgentService;
    }

    /**
     * @param registerUserRequest request containing the username, password, email, and IP address of the user to register
     * @return AuthenticateResult containing the registered user, access token, refresh token, and expiration time
     */
    @Override
    public AuthenticateResult registerUser(RegisterUserRequest registerUserRequest) {
        User user = new User(
                null,
                registerUserRequest.email().toLowerCase(),
                registerUserRequest.username(),
                passwordEncoder.encode(registerUserRequest.password()),
                false,
                UserStatus.ACTIVE,
                null,
                null,
                null
        );

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyRegisteredException();
        }

        SessionCreationResult sessionCreationResult = createSession(user, registerUserRequest.ipAddress(), registerUserRequest.userAgent());

        return new AuthenticateResult(
                user,
                sessionCreationResult.accessToken(),
                sessionCreationResult.refreshToken(),
                sessionCreationResult.accessTokenExpiresAt().toEpochMilli()
        );
    }

    /**
     * @param authenticateUserRequest request containing the email, password, IP address, and user agent of the user to authenticate
     * @return AuthenticateResult containing the authenticated user, access token, refresh token, and expiration time
     */
    @Override
    public AuthenticateResult authenticate(AuthenticateUserRequest authenticateUserRequest) {
        User user = userRepository.findByEmail(authenticateUserRequest.email().toLowerCase()).orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(authenticateUserRequest.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        SessionCreationResult sessionCreationResult = createSession(user, authenticateUserRequest.ipAddress(), authenticateUserRequest.userAgent());

        return new AuthenticateResult(
                user,
                sessionCreationResult.accessToken(),
                sessionCreationResult.refreshToken(),
                sessionCreationResult.accessTokenExpiresAt().toEpochMilli()
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
    public TokenRefreshResult refreshAccessToken(String refreshToken) {
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
     * @return TokenRefreshResult containing the new access token, refresh token, and expiration time
     */
    private TokenRefreshResult rotateRefreshToken(Session s) {
        TokenService.AccessTokenResult accessTokenResult = tokenService.generateAccessToken(s.getUser(), s.getId().toString());
        String newRefreshToken = tokenService.generateRefreshToken();
        String newHashedRefreshToken = tokenService.hash(newRefreshToken);

        s.setPreviousRefreshTokenHash(s.getCurrentRefreshTokenHash());
        s.setCurrentRefreshTokenHash(newHashedRefreshToken);
        s.setUpdatedAt(Instant.now());
        sessionRepository.save(s);

        return new TokenRefreshResult(
                accessTokenResult.token(),
                newRefreshToken,
                accessTokenResult.expiresAt().toEpochMilli()
        );
    }

    /**
     *
     * @param s Session object
     * @return TokenRefreshResult containing the new access token, current refresh token, and expiration time
     */
    private TokenRefreshResult reissueCurrentRefreshToken(Session s) {
        TokenService.AccessTokenResult accessTokenResult = tokenService.generateAccessToken(s.getUser(), s.getId().toString());

        return new TokenRefreshResult(
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
}
