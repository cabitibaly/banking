package com.jiyuu.banking.controller;

import com.jiyuu.banking.config.JwtUtils;
import com.jiyuu.banking.dto.*;
import com.jiyuu.banking.entity.User;
import com.jiyuu.banking.exception.ValidationException;
import com.jiyuu.banking.service.RefreshTokenService;
import com.jiyuu.banking.service.TokenStoreService;
import com.jiyuu.banking.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final TokenStoreService tokenStoreService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest registerRequest) {
        User userCreated = this.userService.register(
                registerRequest.email(),
                registerRequest.password(),
                registerRequest.role()
        );
        return new ResponseEntity<>(userCreated, HttpStatus.CREATED);
    }

    @PostMapping("/activate")
    public void activate(@RequestParam String code) {
        this.userService.verifyCode(code);
    }

    @PostMapping("/resend-code")
    public void resendCode(@RequestParam String email) {
        this.userService.resendCode(email);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody AuthRequest authRequest) {

        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        if(userDetails == null) {
            throw new BadCredentialsException("Identifiant ou mot de passe incorrect");
        }

        Map<String, String> tokens = this.jwtUtils.generateToken(userDetails);
        AuthResponse authResponse = new AuthResponse(
                tokens.get("accessToken"),
                tokens.get("refreshToken")
        );
        ApiResponse<AuthResponse> response = new ApiResponse<>(
                authResponse,
                "Connexion réussie",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody Map<String, String> token) {
        Map<String, String> tokens = this.refreshTokenService.newTokenPair(token.get("token"));

        AuthResponse authResponse = new AuthResponse(
                tokens.get("accessToken"),
                tokens.get("refreshToken")
        );
        ApiResponse<AuthResponse> response = new ApiResponse<>(
                authResponse,
                "Reconnexion réussie",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>>logout(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, String> logoutRequest) {
        if (authHeader == null) {
            throw new ValidationException("Impossible de valider votre requête");
        }

        String accessToken = authHeader.replace("Bearer ", "");
        String refreshToken = logoutRequest.get("refreshToken");
        jwtUtils.logout(accessToken, refreshToken);

        ApiResponse<Object> response = new ApiResponse<>(
                null,
                "Déconnexion réussie",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@RequestParam String email) {
        this.userService.forgotPassword(email);
        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Un email a été envoyé à l'adresse mail fournie",
                HttpStatus.OK.value(),
                Instant.now()
        );
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestParam String token, @Valid @RequestBody ResetPassword resetPassword) {
        this.userService.resetPassword(token, resetPassword.oldPassword(), resetPassword.newPassword());
        ApiResponse<?> response = new ApiResponse<>(
                null,
                "Votre mot de passe a été modifié avec succès",
                HttpStatus.OK.value(),
                Instant.now()
        );
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PatchMapping("/update-email")
    public ResponseEntity<ApiResponse<AuthResponse>> updateEmail(@RequestBody Map<String, String> updateEmail) {
        Map<String, String> tokens = this.userService.updateEmail(updateEmail.get("email"));
        AuthResponse authResponse = new AuthResponse(
                tokens.get("accessToken"),
                tokens.get("refreshToken")
        );
        ApiResponse<AuthResponse> response = new ApiResponse<>(
                authResponse,
                "Connexion réussie",
                HttpStatus.OK.value(),
                Instant.now()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {

        Map<String, Object> response = new HashMap<>();

        response.put("message", "Succès");
        response.put("status", 200);
        response.put("data", List.of("Java", "Spring"));

        return ResponseEntity.ok(response);
    }
}
