package com.bellodas.prestamoservice.controller;

import com.bellodas.prestamoservice.dto.AuthResponse;
import com.bellodas.prestamoservice.dto.LoginRequest;
import com.bellodas.prestamoservice.dto.RegisterRequest;
import com.bellodas.prestamoservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegisterRequest peticion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(peticion));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest peticion) {
        return authService.login(peticion);
    }
}
