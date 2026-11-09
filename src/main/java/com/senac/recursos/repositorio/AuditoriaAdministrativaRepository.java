package com.senac.recursos.repositorio;

import com.senac.recursos.dominio.AuditoriaAdministrativa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaAdministrativaRepository extends JpaRepository<AuditoriaAdministrativa, Long> {
    List<AuditoriaAdministrativa> findAllByOrderByOcorridoEmDescIdDesc();
}
