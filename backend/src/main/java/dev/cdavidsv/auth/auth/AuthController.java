package dev.cdavidsv.auth.auth;

import dev.cdavidsv.auth.auth.dto.*;
import dev.cdavidsv.auth.core.service.AuthenticationService;
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

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticatedUserResponseDTO> registerUser(
        HttpServletRequest request,
        @Valid @RequestBody RegisterUserRequestDTO registerUserRequest
    ) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        AuthenticationService.AuthenticateResult result = authenticationService.registerUser(new AuthenticationService.RegisterUserRequest(
                registerUserRequest.username(),
                registerUserRequest.password(),
                registerUserRequest.email(),
                ip,
                userAgent
        ));

        AuthenticatedUserResponseDTO response = new AuthenticatedUserResponseDTO(
                result.user().getId().toString(),
                result.accessToken(),
                result.refreshToken(),
                result.expiresAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticatedUserResponseDTO> loginUser(
        HttpServletRequest request,
        @Valid @RequestBody LoginUserRequestDTO loginUserRequest
    ) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        AuthenticationService.AuthenticateResult result = authenticationService.authenticate(new AuthenticationService.AuthenticateUserRequest(
                loginUserRequest.email(),
                loginUserRequest.password(),
                ip,
                userAgent
        ));

        AuthenticatedUserResponseDTO response = new AuthenticatedUserResponseDTO(
                result.user().getId().toString(),
                result.accessToken(),
                result.refreshToken(),
                result.expiresAt()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(@AuthenticationPrincipal TokenService.AccessTokenClaims claims) {
        UUID sessionId = claims.sessionId();
        authenticationService.terminateSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/token")
    public ResponseEntity<TokenRefreshResponseDTO> refreshToken(@Valid @RequestBody RefreshAccessTokenDTO tokenRequest) {
        AuthenticationService.TokenRefreshResult result = authenticationService.refreshAccessToken(tokenRequest.refresh_token());
        TokenRefreshResponseDTO response = new TokenRefreshResponseDTO(
                result.accessToken(),
                result.refreshToken(),
                result.expiresAt()
        );
        return ResponseEntity.ok(response);
    }
}
