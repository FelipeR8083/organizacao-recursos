package com.senac.recursos.repositorio;

import com.senac.recursos.dominio.MovimentacaoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MovimentacaoMaterialRepository extends JpaRepository<MovimentacaoMaterial, Long> {
    Optional<MovimentacaoMaterial> findByReserva_Id(Long reservaId);
}
