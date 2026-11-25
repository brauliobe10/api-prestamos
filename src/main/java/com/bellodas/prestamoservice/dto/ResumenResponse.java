package com.bellodas.prestamoservice.dto;

import java.math.BigDecimal;

public record ResumenResponse(
        BigDecimal capitalPrestado,
        BigDecimal totalPorCobrar,
        BigDecimal gananciaEsperada,
        long totalPrestamos,
        long prestamosPendientes
) {
}
