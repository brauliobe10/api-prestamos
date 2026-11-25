package com.bellodas.prestamoservice.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PrestamoRequest(
        @NotBlank(message = "El cliente es obligatorio")
        @Size(max = 100, message = "El cliente no puede superar 100 caracteres")
        String cliente,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
        String dni,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "El monto debe tener máximo 8 enteros y 2 decimales")
        BigDecimal monto,

        @NotNull(message = "El interés es obligatorio")
        @DecimalMin(value = "0", message = "El interés no puede ser negativo")
        @DecimalMax(value = "100", message = "El interés no puede superar 100")
        @Digits(integer = 3, fraction = 2, message = "El interés debe tener máximo 3 enteros y 2 decimales")
        BigDecimal interes,

        @NotNull(message = "La fecha de vencimiento es obligatoria")
        LocalDate fechaVencimiento,

        Boolean pagado
) {
}
