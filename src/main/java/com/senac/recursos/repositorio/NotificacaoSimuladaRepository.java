package com.senac.recursos.repositorio;

import com.senac.recursos.dominio.NotificacaoSimulada;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacaoSimuladaRepository extends JpaRepository<NotificacaoSimulada, Long> {
    List<NotificacaoSimulada> findAllByOrderByOcorridoEmDesc();
    List<NotificacaoSimulada> findByDestinatarioIgnoreCaseOrderByOcorridoEmDesc(String destinatario);
}
