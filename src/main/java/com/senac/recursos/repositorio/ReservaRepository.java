package com.senac.recursos.repositorio;

import com.senac.recursos.dominio.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
        select count(r) > 0 from Reserva r left join r.professor p
        where (r.recurso.id = :recursoId or p.id = :recursoId)
          and r.estado in :estados
          and r.inicio < :fim and r.fim > :inicio
        """)
    boolean existeConflito(@Param("recursoId") Long recursoId,
                           @Param("estados") Collection<EstadoReserva> estados,
                           @Param("inicio") Instant inicio,
                           @Param("fim") Instant fim);


    @Query("""
        select count(r) > 0 from Reserva r
        where (r.recurso.id = :recursoId or r.professor.id = :recursoId)
          and r.estado in :estados
          and r.id <> :ignorarId
          and r.inicio < :fim and r.fim > :inicio
        """)
    boolean existeConflitoIgnorando(@Param("recursoId") Long recursoId,
                                    @Param("estados") Collection<EstadoReserva> estados,
                                    @Param("inicio") Instant inicio,
                                    @Param("fim") Instant fim,
                                    @Param("ignorarId") Long ignorarId);

    /** Serializa transições simultâneas da mesma reserva. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> buscarComLock(@Param("id") Long id);
}
