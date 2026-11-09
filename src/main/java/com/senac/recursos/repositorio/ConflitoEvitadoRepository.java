package com.senac.recursos.repositorio;

import com.senac.recursos.dominio.ConflitoEvitado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface ConflitoEvitadoRepository extends JpaRepository<ConflitoEvitado, Long> {
    long countByOcorridoEmBetween(Instant inicio, Instant fim);
    List<ConflitoEvitado> findByOcorridoEmBetweenOrderByOcorridoEmDesc(Instant inicio, Instant fim);
}
