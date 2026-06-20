package org.example.aiassistantklawa.user.api;

import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.aiassistantklawa.config.JwtService;
import org.example.aiassistantklawa.user.domain.Role;
import org.example.aiassistantklawa.user.domain.User;
import org.example.aiassistantklawa.user.infrastructure.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    @Transactional
    public AuthenticationResponse register( RegisterRequest request) {
           var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Role.User).isActive(true)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse
                .builder()
                .token(jwtToken)
                .build();
    }
    @Transactional
    public  AuthenticationResponse authenticate( AuthenticationRequest request) {
        authenticationManager
                .authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse
                .builder()
                .token(jwtToken)
                .build();
    }


}
