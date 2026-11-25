package com.udhayanidhi.fitly.service;

import com.udhayanidhi.fitly.dto.AuthResponse;
import com.udhayanidhi.fitly.dto.LoginRequest;
import com.udhayanidhi.fitly.dto.RegisterRequest;
import com.udhayanidhi.fitly.model.User;
import com.udhayanidhi.fitly.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return new AuthResponse(jwtService.generate(email, user.getRole()), user.getName(), email);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
            .filter(u -> u.getPassword() != null && passwordEncoder.matches(request.password(), u.getPassword()))
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        return new AuthResponse(jwtService.generate(email, user.getRole()), user.getName(), email);
    }
}