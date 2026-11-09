package com.senac.recursos.repositorio;

import com.senac.recursos.dominio.Recurso;
import com.senac.recursos.dominio.TipoRecurso;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RecursoRepository extends JpaRepository<Recurso, Long> {
    @Query("select count(r) > 0 from Recurso r where r.tipo = :tipo and lower(trim(r.nome)) = :nome and (:id is null or r.id <> :id)")
    boolean existeNomeNormalizado(@Param("tipo") TipoRecurso tipo, @Param("nome") String nome, @Param("id") Long id);

    @Query("""
        select r from Recurso r
        where (:tipo is null or r.tipo = :tipo)
          and (:capacidade is null or r.capacidade >= :capacidade)
          and (:localizacao is null or lower(coalesce(r.localizacao, '')) like lower(concat('%', :localizacao, '%')))
          and (:competencia is null or lower(coalesce(r.competencias, '')) like lower(concat('%', :competencia, '%')))
        order by r.tipo, r.nome
        """)
    List<Recurso> pesquisar(@Param("tipo") TipoRecurso tipo,
                            @Param("capacidade") Integer capacidade,
                            @Param("localizacao") String localizacao,
                            @Param("competencia") String competencia);

    /** SELECT ... FOR UPDATE: serializa reservas concorrentes do mesmo recurso. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Recurso r where r.id = :id")
    Optional<Recurso> buscarComLock(@Param("id") Long id);
}
