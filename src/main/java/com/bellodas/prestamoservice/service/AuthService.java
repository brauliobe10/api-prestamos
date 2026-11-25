package com.bellodas.prestamoservice.service;

import com.bellodas.prestamoservice.dto.AuthResponse;
import com.bellodas.prestamoservice.dto.LoginRequest;
import com.bellodas.prestamoservice.dto.RegisterRequest;
import com.bellodas.prestamoservice.entity.Usuario;
import com.bellodas.prestamoservice.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       UsuarioService usuarioService,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    public AuthResponse registrar(RegisterRequest peticion) {
        Usuario usuario = usuarioService.registrar(peticion);
        return responderConToken(usuario.getEmail());
    }

    public AuthResponse login(LoginRequest peticion) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(peticion.email(), peticion.password()));
        return responderConToken(peticion.email());
    }

    private AuthResponse responderConToken(String email) {
        String token = jwtService.generateToken(email);
        return new AuthResponse(token, "Bearer", jwtService.expiration(token), email);
    }
}
