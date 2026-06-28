package com.bankapp.banking.service.impl;

import com.bankapp.banking.dto.AuthResponse;
import com.bankapp.banking.dto.LoginRequest;
import com.bankapp.banking.dto.RegisterRequest;
import com.bankapp.banking.entity.User;
import com.bankapp.banking.enums.Role;
import com.bankapp.banking.exception.DuplicateResourceException;
import com.bankapp.banking.repository.UserRepository;
import com.bankapp.banking.security.JwtService;
import com.bankapp.banking.security.UserPrincipal;
import com.bankapp.banking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }

        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER); // every self-registered user starts as a normal customer

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                // NEVER store the raw password - always hash with BCrypt before persisting
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .roles(roles)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(saved);
        String token = jwtService.generateToken(principal);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(saved.getId())
                .username(saved.getUsername())
                .fullName(saved.getFullName())
                .roles(roles.stream().map(Enum::name).collect(Collectors.toList()))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Delegates to the AuthenticationProvider configured in SecurityConfig,
        // which loads the user via CustomUserDetailsService and checks the
        // password hash via PasswordEncoder.matches(). Throws
        // BadCredentialsException automatically on mismatch (handled globally).
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);

        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(); // safe: authentication just succeeded for this username

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .roles(roles)
                .build();
    }
}
