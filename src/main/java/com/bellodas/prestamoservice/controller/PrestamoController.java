package com.bellodas.prestamoservice.controller;

import com.bellodas.prestamoservice.dto.PrestamoRequest;
import com.bellodas.prestamoservice.dto.PrestamoResponse;
import com.bellodas.prestamoservice.dto.ResumenResponse;
import com.bellodas.prestamoservice.service.PrestamoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public List<PrestamoResponse> listar(Authentication autenticacion) {
        return prestamoService.listar(autenticacion.getName());
    }

    @GetMapping("/resumen")
    public ResumenResponse resumen(Authentication autenticacion) {
        return prestamoService.resumen(autenticacion.getName());
    }

    @GetMapping("/{id}")
    public PrestamoResponse obtener(@PathVariable Long id, Authentication autenticacion) {
        return prestamoService.obtener(autenticacion.getName(), id);
    }

    @PostMapping
    public ResponseEntity<PrestamoResponse> crear(@Valid @RequestBody PrestamoRequest peticion,
                                                  Authentication autenticacion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prestamoService.crear(autenticacion.getName(), peticion));
    }

    @PutMapping("/{id}")
    public PrestamoResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody PrestamoRequest peticion,
                                       Authentication autenticacion) {
        return prestamoService.actualizar(autenticacion.getName(), id, peticion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication autenticacion) {
        prestamoService.eliminar(autenticacion.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
