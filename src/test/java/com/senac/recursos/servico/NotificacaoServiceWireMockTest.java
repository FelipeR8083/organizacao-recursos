package com.senac.recursos.servico;

import com.senac.recursos.dominio.NotificacaoSimulada;
import com.senac.recursos.repositorio.NotificacaoSimuladaRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificacaoServiceWireMockTest {
    @Test
    void enviaWebhookERegistraCanalHttp() {
        WireMockServer wiremock = new WireMockServer(com.github.tomakehurst.wiremock.core.WireMockConfiguration.options().dynamicPort());
        wiremock.start();
        try {
            wiremock.stubFor(post(urlEqualTo("/notifications"))
                    .willReturn(aResponse().withStatus(202)));
            NotificacaoSimuladaRepository repository = mock(NotificacaoSimuladaRepository.class);
            NotificacaoService service = new NotificacaoService(repository, RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory()),
                    wiremock.baseUrl() + "/notifications");

            service.enviar("prof.ana", "Reserva aprovada", "Sua reserva foi aprovada.");

            wiremock.verify(1, postRequestedFor(urlEqualTo("/notifications"))
                    .withRequestBody(matchingJsonPath("$.destinatario", equalTo("prof.ana"))));
            var captor = org.mockito.ArgumentCaptor.forClass(NotificacaoSimulada.class);
            verify(repository).save(captor.capture());
            assertEquals("HTTP", captor.getValue().getCanal());
        } finally {
            wiremock.stop();
        }
    }

    @Test
    void falhaNoWebhookNaoRegistraNotificacaoComoEnviada() {
        WireMockServer wiremock = new WireMockServer(com.github.tomakehurst.wiremock.core.WireMockConfiguration.options().dynamicPort());
        wiremock.start();
        try {
            wiremock.stubFor(post(urlEqualTo("/notifications"))
                    .willReturn(aResponse().withStatus(503)));
            NotificacaoSimuladaRepository repository = mock(NotificacaoSimuladaRepository.class);
            NotificacaoService service = new NotificacaoService(repository, RestClient.builder(),
                    wiremock.baseUrl() + "/notifications");

            assertThrows(Exception.class,
                    () -> service.enviar("prof.ana", "Reserva", "Mensagem"));
            verify(repository, never()).save(any());
        } finally {
            wiremock.stop();
        }
    }
}
