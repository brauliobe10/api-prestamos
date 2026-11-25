package com.bellodas.prestamoservice.repository;

import com.bellodas.prestamoservice.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findAllByUsuarioIdOrderByFechaPrestamoDescIdDesc(Long usuarioId);

    Optional<Prestamo> findByIdAndUsuarioId(Long id, Long usuarioId);
}
