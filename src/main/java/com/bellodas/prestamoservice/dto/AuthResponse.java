package com.bellodas.prestamoservice.dto;

import java.time.LocalDateTime;

public record AuthResponse(
        String token,
        String tipo,
        LocalDateTime expiracion,
        String email
) {
}
