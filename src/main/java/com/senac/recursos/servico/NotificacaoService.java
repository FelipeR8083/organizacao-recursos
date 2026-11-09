package com.senac.recursos.servico;

import com.senac.recursos.dominio.NotificacaoSimulada;
import com.senac.recursos.repositorio.NotificacaoSimuladaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class NotificacaoService {
    private final NotificacaoSimuladaRepository repository;
    public NotificacaoService(NotificacaoSimuladaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void enviar(String destinatario, String assunto, String mensagem) {
        if (destinatario == null || destinatario.isBlank()) return;
        repository.save(new NotificacaoSimulada(destinatario, assunto, mensagem, Instant.now()));
    }

    @Transactional(readOnly = true)
    public List<NotificacaoSimulada> listar() {
        return repository.findAllByOrderByOcorridoEmDesc();
    }
}
