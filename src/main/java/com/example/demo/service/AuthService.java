package com.example.demo.service;

import com.example.demo.domain.AppUser;
import com.example.demo.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(String username, String email, String rawPassword) {
        if (repo.existsByUsername(username)) {
            throw new IllegalArgumentException("Имя пользователя уже занято");
        }
        if (repo.existsByEmail(email)) {
            throw new IllegalArgumentException("Email уже используется");
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(AppUser.Role.USER);
        return repo.save(user);
    }
}
