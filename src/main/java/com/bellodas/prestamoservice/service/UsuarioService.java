package com.bellodas.prestamoservice.service;

import com.bellodas.prestamoservice.dto.RegisterRequest;
import com.bellodas.prestamoservice.entity.Usuario;
import com.bellodas.prestamoservice.exception.EmailAlreadyExistsException;
import com.bellodas.prestamoservice.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrar(RegisterRequest peticion) {
        if (usuarioRepository.existsByEmail(peticion.email())) {
            throw new EmailAlreadyExistsException("Ya existe un prestamista con ese email");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(peticion.nombre());
        usuario.setEmail(peticion.email());
        usuario.setPassword(passwordEncoder.encode(peticion.password()));
        usuario.setRol("USER");
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Prestamista autenticado no encontrado"));
    }
}
