package com.senac.recursos.repositorio;

import com.senac.recursos.dominio.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Collection;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
        select count(r) > 0 from Reserva r
        where r.recurso.id = :recursoId
          and r.estado in :estados
          and r.inicio < :fim and r.fim > :inicio
        """)
    boolean existeConflito(@Param("recursoId") Long recursoId,
                           @Param("estados") Collection<EstadoReserva> estados,
                           @Param("inicio") Instant inicio,
                           @Param("fim") Instant fim);
}
