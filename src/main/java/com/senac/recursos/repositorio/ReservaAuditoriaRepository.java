package com.senac.recursos.repositorio;

import com.senac.recursos.dominio.ReservaAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservaAuditoriaRepository extends JpaRepository<ReservaAuditoria, Long> {
    List<ReservaAuditoria> findByReserva_IdOrderByOcorridoEmAsc(Long reservaId);
}
