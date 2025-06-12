package com.cofeecode.application.powerhauscore.controllers.api;

import com.cofeecode.application.powerhauscore.data.User;
import com.cofeecode.application.powerhauscore.dto.JwtResponseDTO;
import com.cofeecode.application.powerhauscore.dto.LoginRequestDTO;
import com.cofeecode.application.powerhauscore.repository.UserRepository;
import com.cofeecode.application.powerhauscore.security.JwtTokenProvider;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwt = jwtTokenProvider.generateTokenFromUsername(userDetails.getUsername(), userDetails.getAuthorities());

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(
                () -> new RuntimeException("User " + userDetails.getUsername() + " not found after authentication")
        );

        return ResponseEntity.ok(new JwtResponseDTO(jwt, user.getId(), user.getUsername(), user.getName(), user.getRoles()));
    }
}
