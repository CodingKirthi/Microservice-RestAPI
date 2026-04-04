package com.microservices.apigateway.controller;

import com.microservices.apigateway.dto.AuthRequest;
import com.microservices.apigateway.dto.AuthResponse;
import com.microservices.apigateway.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    // Hardcoded users for demo: username -> password
    private static final Map<String, String> USERS = Map.of(
            "admin", "password",
            "user",  "password"
    );

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        String expectedPassword = USERS.get(request.getUsername());
        if (expectedPassword != null && expectedPassword.equals(request.getPassword())) {
            String token = jwtUtil.generateToken(request.getUsername());
            log.info("User '{}' logged in successfully", request.getUsername());
            return Mono.just(ResponseEntity.ok(new AuthResponse(token, request.getUsername())));
        }
        log.warn("Failed login attempt for user '{}'", request.getUsername());
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping("/validate")
    public Mono<ResponseEntity<String>> validate(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                return Mono.just(ResponseEntity.ok("Token valid for user: " + username));
            }
        }
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token"));
    }
}
