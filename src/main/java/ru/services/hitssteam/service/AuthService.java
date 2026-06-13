package ru.services.hitssteam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import ru.services.hitssteam.dto.*;
import ru.services.hitssteam.model.User;
import ru.services.hitssteam.repository.UserRepository;
import ru.services.hitssteam.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public User register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername()))
            throw new RuntimeException("Username taken");
        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email taken");

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(DigestUtils.md5DigestAsHex(req.getPassword().getBytes()))
                .role("USER")
                .build();
        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String hash = DigestUtils.md5DigestAsHex(req.getPassword().getBytes());
        if (!hash.equals(user.getPasswordHash()))
            throw new RuntimeException("Invalid password");
        return new AuthResponse(jwtService.generate(user.getUsername(), user.getRole(), user.getId()));
    }
}
