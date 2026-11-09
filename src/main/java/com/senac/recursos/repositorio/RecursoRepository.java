package com.senac.recursos.repositorio;

import com.senac.recursos.dominio.Recurso;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import com.senac.recursos.dominio.TipoRecurso;

public interface RecursoRepository extends JpaRepository<Recurso, Long> {
    @Query("select count(r) > 0 from Recurso r where r.tipo = :tipo and lower(trim(r.nome)) = :nome and (:id is null or r.id <> :id)")
    boolean existeNomeNormalizado(@Param("tipo") TipoRecurso tipo, @Param("nome") String nome, @Param("id") Long id);


    /** SELECT ... FOR UPDATE: serializa reservas concorrentes do mesmo recurso. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Recurso r where r.id = :id")
    Optional<Recurso> buscarComLock(@Param("id") Long id);
}
