package com.bellodas.prestamoservice.service;

import com.bellodas.prestamoservice.dto.PrestamoRequest;
import com.bellodas.prestamoservice.dto.PrestamoResponse;
import com.bellodas.prestamoservice.dto.ResumenResponse;
import com.bellodas.prestamoservice.entity.Prestamo;
import com.bellodas.prestamoservice.entity.Usuario;
import com.bellodas.prestamoservice.exception.ResourceNotFoundException;
import com.bellodas.prestamoservice.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class PrestamoService {

    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final PrestamoRepository prestamoRepository;
    private final UsuarioService usuarioService;

    public PrestamoService(PrestamoRepository prestamoRepository, UsuarioService usuarioService) {
        this.prestamoRepository = prestamoRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listar(String email) {
        Long usuarioId = usuarioService.buscarPorEmail(email).getId();
        return prestamoRepository.findAllByUsuarioIdOrderByFechaPrestamoDescIdDesc(usuarioId)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PrestamoResponse obtener(String email, Long id) {
        return aResponse(buscarPrestamo(email, id));
    }

    public PrestamoResponse crear(String email, PrestamoRequest peticion) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        Prestamo prestamo = new Prestamo();
        aplicarPeticion(prestamo, peticion);
        prestamo.setPagado(Boolean.TRUE.equals(peticion.pagado()));
        prestamo.setUsuario(usuario);
        return aResponse(prestamoRepository.save(prestamo));
    }

    public PrestamoResponse actualizar(String email, Long id, PrestamoRequest peticion) {
        Prestamo prestamo = buscarPrestamo(email, id);
        aplicarPeticion(prestamo, peticion);
        if (peticion.pagado() != null) {
            prestamo.setPagado(peticion.pagado());
        }
        return aResponse(prestamoRepository.save(prestamo));
    }

    public void eliminar(String email, Long id) {
        prestamoRepository.delete(buscarPrestamo(email, id));
    }

    @Transactional(readOnly = true)
    public ResumenResponse resumen(String email) {
        List<Prestamo> prestamos = prestamoRepository
                .findAllByUsuarioIdOrderByFechaPrestamoDescIdDesc(usuarioService.buscarPorEmail(email).getId());

        BigDecimal capitalPrestado = prestamos.stream()
                .map(Prestamo::getMonto)
                .reduce(CERO, BigDecimal::add);

        List<Prestamo> pendientes = prestamos.stream().filter(p -> !p.isPagado()).toList();

        BigDecimal totalPorCobrar = pendientes.stream()
                .map(this::total)
                .reduce(CERO, BigDecimal::add);

        BigDecimal gananciaEsperada = totalPorCobrar.subtract(
                pendientes.stream().map(Prestamo::getMonto).reduce(CERO, BigDecimal::add));

        return new ResumenResponse(
                capitalPrestado.setScale(2, RoundingMode.HALF_UP),
                totalPorCobrar.setScale(2, RoundingMode.HALF_UP),
                gananciaEsperada.setScale(2, RoundingMode.HALF_UP),
                prestamos.size(),
                pendientes.size()
        );
    }

    private Prestamo buscarPrestamo(String email, Long id) {
        Long usuarioId = usuarioService.buscarPorEmail(email).getId();
        return prestamoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado"));
    }

    private void aplicarPeticion(Prestamo prestamo, PrestamoRequest peticion) {
        prestamo.setCliente(peticion.cliente());
        prestamo.setDni(peticion.dni());
        prestamo.setMonto(peticion.monto());
        prestamo.setInteres(peticion.interes());
        prestamo.setFechaVencimiento(peticion.fechaVencimiento());
    }

    private PrestamoResponse aResponse(Prestamo prestamo) {
        return new PrestamoResponse(
                prestamo.getId(),
                prestamo.getCliente(),
                prestamo.getDni(),
                prestamo.getMonto(),
                prestamo.getInteres(),
                total(prestamo),
                prestamo.getFechaPrestamo(),
                prestamo.getFechaVencimiento(),
                prestamo.isPagado()
        );
    }

    private BigDecimal total(Prestamo prestamo) {
        BigDecimal interesCalculado = prestamo.getMonto()
                .multiply(prestamo.getInteres())
                .divide(CIEN, 2, RoundingMode.HALF_UP);
        return prestamo.getMonto().add(interesCalculado).setScale(2, RoundingMode.HALF_UP);
    }
}
