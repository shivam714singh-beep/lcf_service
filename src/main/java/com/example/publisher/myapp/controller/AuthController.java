package com.example.publisher.myapp.controller;

import com.example.publisher.myapp.auth.*;
import com.example.publisher.myapp.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j  // ADDED
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(), loginRequest.password()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.username());

            // ADDED: Fetch user entity to get ID for logging
            UserEntity user = userRepository.findByUsername(loginRequest.username())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // ADDED: Log user ID on successful login
            log.info("User logged in successfully with ID: {}", user.getId());

            log.info("User logged in successfully with ID: {} and username: {}",
                    user.getId(), user.getUsername());



            String token = jwtUtils.generateToken(userDetails.getUsername());
            String role = userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority())
                    .orElse("ROLE_USER");

            return ResponseEntity.ok(new LoginResponse(token, role));
        } catch (BadCredentialsException e) {
            // ADDED: Log failed login attempts
            log.warn("Failed login attempt for username: {}", loginRequest.username());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            // ADDED THESE 2 LINES:
            log.warn("Registration attempt failed - User already exists with username: {}", request.username());
            return ResponseEntity.badRequest().body("Username already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        UserEntity user = UserEntity.builder()
                .username(request.username())
                .password(encodedPassword)
                .role("USER")
                .build();

        // MODIFIED: Capture saved user to get ID
        UserEntity savedUser = userRepository.save(user);

        // ADDED: Log successful registration with ID
        log.info("User registered successfully with ID: {} and username: {}",
                savedUser.getId(), savedUser.getUsername());

        return ResponseEntity.ok("User registered successfully");
    }

    public record LoginRequest(String username, String password) {}
    public record RegisterRequest(String username, String password) {}
    public record LoginResponse(String token, String role) {}
}
