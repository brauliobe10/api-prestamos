package com.bellodas.prestamoservice.dto;

import java.time.LocalDateTime;

public record ApiError(
        int status,
        String error,
        String mensaje,
        String path,
        LocalDateTime timestamp
) {
}
