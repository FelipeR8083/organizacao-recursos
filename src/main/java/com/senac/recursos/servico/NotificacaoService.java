package com.senac.recursos.servico;

import com.senac.recursos.dominio.NotificacaoSimulada;
import com.senac.recursos.repositorio.NotificacaoSimuladaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class NotificacaoService {
    private final NotificacaoSimuladaRepository repository;
    private final RestClient.Builder restClientBuilder;
    private final String webhookUrl;

    public NotificacaoService(NotificacaoSimuladaRepository repository,
                              RestClient.Builder restClientBuilder,
                              @Value("${NOTIFICACAO_URL:}") String webhookUrl) {
        this.repository = repository;
        this.restClientBuilder = restClientBuilder;
        this.webhookUrl = webhookUrl;
    }

    @Transactional
    public void enviar(String destinatario, String assunto, String mensagem) {
        if (destinatario == null || destinatario.isBlank()) return;
        String canal = "SIMULADO";
        if (webhookUrl != null && !webhookUrl.isBlank()) {
            restClientBuilder.build().post().uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("destinatario", destinatario, "assunto", assunto, "mensagem", mensagem))
                    .retrieve().toBodilessEntity();
            canal = "HTTP";
        }
        repository.save(new NotificacaoSimulada(destinatario, assunto, mensagem, Instant.now(), canal));
    }

    @Transactional(readOnly = true)
    public List<NotificacaoSimulada> listar() {
        return repository.findAllByOrderByOcorridoEmDesc();
    }
}
