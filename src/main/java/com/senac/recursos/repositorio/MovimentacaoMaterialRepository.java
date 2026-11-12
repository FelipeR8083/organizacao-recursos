package com.senac.recursos.repositorio;

import com.senac.recursos.dominio.MovimentacaoMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface MovimentacaoMaterialRepository extends JpaRepository<MovimentacaoMaterial, Long> {
    Optional<MovimentacaoMaterial> findByReserva_Id(Long reservaId);
    Optional<MovimentacaoMaterial> findByReserva_IdAndMaterial_Id(Long reservaId, Long materialId);
    List<MovimentacaoMaterial> findAllByReserva_Id(Long reservaId);
}
