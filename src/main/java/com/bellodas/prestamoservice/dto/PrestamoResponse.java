package com.bellodas.prestamoservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PrestamoResponse(
        Long id,
        String cliente,
        String dni,
        BigDecimal monto,
        BigDecimal interes,
        BigDecimal total,
        LocalDate fechaPrestamo,
        LocalDate fechaVencimiento,
        boolean pagado
) {
}
