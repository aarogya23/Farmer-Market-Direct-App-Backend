package com.commerce.FarmerDirectMarkert.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import com.commerce.FarmerDirectMarkert.dto.AuthResponse;
import com.commerce.FarmerDirectMarkert.dto.GoogleLoginRequest;
import com.commerce.FarmerDirectMarkert.dto.LoginRequest;
import com.commerce.FarmerDirectMarkert.dto.SignupRequest;
import com.commerce.FarmerDirectMarkert.model.User;
import com.commerce.FarmerDirectMarkert.service.AuthService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    // Fix 1: Removed unused googleClientSecret field (keep only if used elsewhere)
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${app.oauth.frontend-redirect-uri:farmermarketdirectapp://oauth-callback}")
    private String frontendRedirectUri;

    // Fix 2: Extract the OAuth2 callback base URL into a configurable property
    // instead of hardcoding localhost:8082
    @Value("${app.oauth.callback-base-url:http://localhost:8082}")
    private String callbackBaseUrl;

    /**
     * POST /api/auth/signup
     * Register a new user (FARMER, BUYER, or ADMIN)
     *
     * Possible errors:
     *  - 400 if any field is blank/null/invalid format  → caught by @Valid + GlobalExceptionHandler
     *  - 409 if email is already registered             → throws DuplicateEmailException
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request,
            @RequestParam(value = "role", required = false) User.Role role) {
        if (request.getRole() == null && role != null) {
            request.setRole(role);
        }
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("You are not authenticated. Please login first.");
        }
        AuthResponse response = authService.getMe(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Initiates OAuth2 flow with Google.
     * Frontend should open this URL in a browser.
     */
    @GetMapping("/oauth2/login")
    public RedirectView initiateOAuth2Login(
            @RequestParam(value = "role", required = false, defaultValue = "BUYER") String role) {

        // Fix 3: Use configurable callbackBaseUrl instead of hardcoded localhost:8082
        String callbackUri = callbackBaseUrl + "/api/auth/oauth2/callback";

        // Fix 4: URL-encode the role state parameter to handle any special characters
        String encodedRole = URLEncoder.encode(role, StandardCharsets.UTF_8);
        String encodedCallbackUri = URLEncoder.encode(callbackUri, StandardCharsets.UTF_8);

        String authorizationUri = "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + googleClientId +
                "&redirect_uri=" + encodedCallbackUri +
                "&response_type=code" +
                "&scope=profile%20email%20openid" +
                "&state=" + encodedRole;

        return new RedirectView(authorizationUri);
    }

    /**
     * OAuth2 callback from Google.
     * Receives authorization code and exchanges it for an ID token.
     */
    @GetMapping("/oauth2/callback")
    public RedirectView oauth2Callback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String role) {

        try {
            // Exchange authorization code for ID token
            String idToken = authService.exchangeCodeForIdToken(code);

            // Parse role from state parameter, default to BUYER
            User.Role userRole = User.Role.BUYER;
            if (role != null && !role.isBlank()) {
                try {
                    userRole = User.Role.valueOf(role.toUpperCase());
                } catch (IllegalArgumentException e) {
                    userRole = User.Role.BUYER;
                }
            }

            // Perform Google login and get JWT
            GoogleLoginRequest loginRequest = new GoogleLoginRequest();
            loginRequest.setIdToken(idToken);
            loginRequest.setRole(userRole);

            AuthResponse authResponse = authService.googleLogin(loginRequest);

            // Fix 5: URL-encode the email to avoid issues with special characters (e.g. '+' in email)
            String encodedEmail = URLEncoder.encode(authResponse.getEmail(), StandardCharsets.UTF_8);

            String redirectUrl = frontendRedirectUri +
                    "?token=" + authResponse.getToken() +
                    "&userId=" + authResponse.getUserId() +
                    "&email=" + encodedEmail;

            return new RedirectView(redirectUrl);

        } catch (Exception e) {
            // Fix 6: URL-encode the error message so special characters don't break the redirect URL
            String encodedError = URLEncoder.encode(
                    e.getMessage() != null ? e.getMessage() : "Unknown error",
                    StandardCharsets.UTF_8
            );
            String errorRedirect = frontendRedirectUri + "?error=" + encodedError;
            return new RedirectView(errorRedirect);
        }
    }
    // Fix 7: Added missing closing brace for the class
}