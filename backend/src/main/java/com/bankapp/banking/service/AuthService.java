package com.bankapp.banking.service;

import com.bankapp.banking.dto.AuthResponse;
import com.bankapp.banking.dto.LoginRequest;
import com.bankapp.banking.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
