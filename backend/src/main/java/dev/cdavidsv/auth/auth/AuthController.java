package dev.cdavidsv.auth.auth;

import dev.cdavidsv.auth.auth.dto.*;
import dev.cdavidsv.auth.core.exception.ResourceNotFoundException;
import dev.cdavidsv.auth.core.exception.TicketNotFoundException;
import dev.cdavidsv.auth.core.model.entity.MfaMethod;
import dev.cdavidsv.auth.core.service.AuthenticationService;
import dev.cdavidsv.auth.core.service.MfaService;
import dev.cdavidsv.auth.core.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("v1/auth")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final MfaService mfaService;

    public AuthController(AuthenticationService authenticationService, MfaService mfaService) {
        this.mfaService = mfaService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticatedUserResponseDTO> registerUser(
        HttpServletRequest request,
        @Valid @RequestBody RegisterUserRequestDTO registerUserRequest
    ) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        AuthenticationService.RegisterResponse result = authenticationService.registerUser(new AuthenticationService.RegisterUserRequest(
                registerUserRequest.username(),
                registerUserRequest.password(),
                registerUserRequest.email(),
                ip,
                userAgent
        ));

        AuthenticatedUserResponseDTO response = new AuthenticatedUserResponseDTO(
                result.user().getId().toString(),
                result.tokens().accessToken(),
                result.tokens().refreshToken(),
                result.tokens().expiresAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
        HttpServletRequest request,
        @Valid @RequestBody LoginUserRequestDTO loginUserRequest
    ) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        AuthenticationService.AuthenticationResult result = authenticationService.authenticate(new AuthenticationService.AuthenticateUserRequest(
                loginUserRequest.email(),
                loginUserRequest.password(),
                ip,
                userAgent
        ));

        switch (result) {
            case AuthenticationService.AuthenticationResult.Authenticated authenticated -> {
                AuthenticatedUserResponseDTO response = new AuthenticatedUserResponseDTO(
                        authenticated.userId(),
                        authenticated.tokens().accessToken(),
                        authenticated.tokens().refreshToken(),
                        authenticated.tokens().expiresAt()
                );
                return ResponseEntity.ok(response);
            }
            case AuthenticationService.AuthenticationResult.MfaRequired mfaRequired -> {
                MfaRequiredResponseDTO response = new MfaRequiredResponseDTO(
                        mfaRequired.userId(),
                        mfaRequired.ticket(),
                        mfaRequired.loginReferenceId(),
                        mfaRequired.methods()
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(@AuthenticationPrincipal TokenService.AccessTokenClaims claims) {
        UUID sessionId = claims.sessionId();
        authenticationService.terminateSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/token")
    public ResponseEntity<TokenRefreshResponseDTO> refreshToken(@Valid @RequestBody RefreshAccessTokenDTO tokenRequest) {
        AuthenticationService.TokenPair result = authenticationService.refreshAccessToken(tokenRequest.refresh_token());
        TokenRefreshResponseDTO response = new TokenRefreshResponseDTO(
                result.accessToken(),
                result.refreshToken(),
                result.expiresAt()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sms/send")
    public ResponseEntity<?> sendSmsMfaChallenge() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/email/send")
    public ResponseEntity<Void> sendEmailMfaChallenge(@Valid @RequestBody SendEmailMfaChallengeRequestDTO request) {
        try {
            mfaService.challenge(UUID.fromString(request.user_id()), request.ticket(), MfaMethod.EMAIL);
        } catch (TicketNotFoundException e) {
            throw new ResourceNotFoundException();
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyMfaCode(@Valid @RequestBody VerifyMfaCodeRequestDTO request) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
