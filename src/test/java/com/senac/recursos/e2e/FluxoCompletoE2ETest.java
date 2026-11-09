package com.senac.recursos.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.Instant;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "APP_ADMIN_USERNAME=e2e-admin",
                "APP_ADMIN_PASSWORD=e2e-admin-password-123"
        })
class FluxoCompletoE2ETest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired TestRestTemplate http;

    private TestRestTemplate auth(String login, String senha) {
        TestRestTemplate client = http.withBasicAuth(login, senha);
        client.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()));
        return client;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void percorreCadastroSolicitacaoAprovacaoAuditoriaRelatorioENotificacao() {
        TestRestTemplate admin = auth("e2e-admin", "e2e-admin-password-123");

        ResponseEntity<JsonNode> salaResponse = admin.postForEntity("/recursos",
                new HttpEntity<>(Map.of("tipo", "SALA", "nome", "Sala E2E", "restrito", false,
                        "emManutencao", false, "capacidade", 25, "localizacao", "Bloco E",
                        "competencias", "Aula e laboratorio"), jsonHeaders()), JsonNode.class);
        assertEquals(HttpStatus.CREATED, salaResponse.getStatusCode());
        long salaId = salaResponse.getBody().get("id").asLong();

        ResponseEntity<JsonNode> professorResponse = admin.postForEntity("/recursos",
                new HttpEntity<>(Map.of("tipo", "PROFESSOR", "nome", "Prof E2E", "restrito", false,
                        "emManutencao", false), jsonHeaders()), JsonNode.class);
        assertEquals(HttpStatus.CREATED, professorResponse.getStatusCode());
        long professorId = professorResponse.getBody().get("id").asLong();

        admin.postForEntity("/usuarios",
                new HttpEntity<>(Map.of("login", "solicitante-e2e", "senha", "senha-solicitante-123",
                        "perfil", "SOLICITANTE"), jsonHeaders()), JsonNode.class);
        admin.postForEntity("/usuarios",
                new HttpEntity<>(Map.of("login", "responsavel-e2e", "senha", "senha-responsavel-123",
                        "perfil", "RESPONSAVEL"), jsonHeaders()), JsonNode.class);

        Instant inicio = Instant.now().plusSeconds(3600);
        Instant fim = inicio.plusSeconds(3600);
        TestRestTemplate solicitante = auth("solicitante-e2e", "senha-solicitante-123");
        ResponseEntity<JsonNode> sessaoSolicitante = solicitante.getForEntity("/sessao", JsonNode.class);
        assertEquals(HttpStatus.OK, sessaoSolicitante.getStatusCode());
        assertEquals("SOLICITANTE", sessaoSolicitante.getBody().get("perfil").asText());
        assertEquals("solicitante-e2e", sessaoSolicitante.getBody().get("login").asText());


        // RF-03: pesquisa real por metadados + disponibilidade via HTTP.
        ResponseEntity<JsonNode> pesquisa = solicitante.getForEntity(
                "/recursos?tipo=SALA&capacidadeMinima=20&localizacao=bloco&competencia=LABORATORIO",
                JsonNode.class);
        assertEquals(HttpStatus.OK, pesquisa.getStatusCode());
        assertEquals(1, pesquisa.getBody().size());
        assertEquals(salaId, pesquisa.getBody().get(0).get("id").asLong());

        String consultaDisponibilidade = "/recursos?tipo=SALA&inicio=" + inicio + "&fim=" + fim;
        ResponseEntity<JsonNode> livresAntes = solicitante.getForEntity(
                consultaDisponibilidade, JsonNode.class);
        assertEquals(HttpStatus.OK, livresAntes.getStatusCode());
        assertEquals(1, livresAntes.getBody().size());
        assertEquals(salaId, livresAntes.getBody().get(0).get("id").asLong());
        ResponseEntity<JsonNode> reservaResponse = solicitante.postForEntity("/reservas",
                new HttpEntity<>(Map.of("recursoId", salaId, "professorId", professorId,
                        "solicitante", "identidade-forjada", "inicio", inicio.toString(), "fim", fim.toString()),
                        jsonHeaders()), JsonNode.class);
        assertEquals(HttpStatus.CREATED, reservaResponse.getStatusCode());
        long reservaId = reservaResponse.getBody().get("id").asLong();

        ResponseEntity<JsonNode> ocupados = solicitante.getForEntity(
                consultaDisponibilidade, JsonNode.class);
        assertEquals(HttpStatus.OK, ocupados.getStatusCode());
        assertEquals(0, ocupados.getBody().size(), "Sala ocupada não deve aparecer como disponível");

        ResponseEntity<JsonNode> livresDepois = solicitante.getForEntity(
                "/recursos?tipo=SALA&inicio=" + fim + "&fim=" + fim.plusSeconds(3600),
                JsonNode.class);
        assertEquals(HttpStatus.OK, livresDepois.getStatusCode());
        assertEquals(1, livresDepois.getBody().size(),
                "Período adjacente à reserva deve continuar disponível");

        ResponseEntity<JsonNode> lista = solicitante.getForEntity("/reservas", JsonNode.class);
        assertEquals("solicitante-e2e", lista.getBody().get(0).get("solicitante").asText());
        // RF-01/RF-10: o solicitante não deve conhecer reservas nem históricos de terceiros.
        ResponseEntity<JsonNode> novoUsuario = admin.postForEntity("/usuarios",
                new HttpEntity<>(Map.of("login", "outra-pessoa-e2e",
                        "senha", "senha-outra-pessoa-123", "perfil", "SOLICITANTE"),
                        jsonHeaders()), JsonNode.class);
        assertEquals(HttpStatus.CREATED, novoUsuario.getStatusCode());
        TestRestTemplate outro = auth("outra-pessoa-e2e", "senha-outra-pessoa-123");
        ResponseEntity<JsonNode> outraReserva = outro.postForEntity("/reservas",
                new HttpEntity<>(Map.of("recursoId", salaId,
                        "solicitante", "identidade-forjada",
                        "inicio", fim.toString(), "fim", fim.plusSeconds(3600).toString()),
                        jsonHeaders()), JsonNode.class);
        assertEquals(HttpStatus.CREATED, outraReserva.getStatusCode());
        long outraReservaId = outraReserva.getBody().get("id").asLong();

        ResponseEntity<JsonNode> minhasReservas = solicitante.getForEntity("/reservas", JsonNode.class);
        assertEquals(1, minhasReservas.getBody().size());
        assertEquals(reservaId, minhasReservas.getBody().get(0).get("id").asLong());
        assertEquals(1, outro.getForEntity("/reservas", JsonNode.class).getBody().size());
        assertEquals(2, admin.getForEntity("/reservas", JsonNode.class).getBody().size());
        assertEquals(HttpStatus.NOT_FOUND,
                solicitante.getForEntity("/reservas/" + outraReservaId + "/auditoria",
                        String.class).getStatusCode());


        ResponseEntity<String> proibido = solicitante.exchange("/reservas/" + reservaId + "/estado",
                HttpMethod.PATCH, new HttpEntity<>(Map.of("estado", "APROVADA"), jsonHeaders()), String.class);
        assertEquals(HttpStatus.FORBIDDEN, proibido.getStatusCode());

        TestRestTemplate responsavel = auth("responsavel-e2e", "senha-responsavel-123");
        ResponseEntity<JsonNode> aprovada = responsavel.exchange("/reservas/" + reservaId + "/estado",
                HttpMethod.PATCH, new HttpEntity<>(Map.of("estado", "APROVADA"), jsonHeaders()), JsonNode.class);
        assertEquals(HttpStatus.OK, aprovada.getStatusCode());
        assertEquals("APROVADA", aprovada.getBody().get("estado").asText());

        ResponseEntity<JsonNode> historico = responsavel.getForEntity(
                "/reservas/" + reservaId + "/auditoria", JsonNode.class);
        assertEquals(2, historico.getBody().size());
        assertEquals("responsavel-e2e", historico.getBody().get(1).get("origem").asText());

        ResponseEntity<JsonNode> relatorio = responsavel.getForEntity(
                "/relatorios/utilizacao?inicio=" + inicio.minusSeconds(60) + "&fim=" + fim.plusSeconds(60),
                JsonNode.class);
        assertTrue(relatorio.getStatusCode().is2xxSuccessful());
        assertTrue(relatorio.getBody().toString().contains("Sala E2E"));

        ResponseEntity<JsonNode> notificacoes = responsavel.getForEntity("/notificacoes", JsonNode.class);
        assertTrue(notificacoes.getStatusCode().is2xxSuccessful());
        assertTrue(notificacoes.getBody().size() >= 2);

        // RF-10: auditoria administrativa existe, é restrita e não vaza senhas.
        ResponseEntity<JsonNode> auditoriaAdmin = admin.getForEntity(
                "/auditoria-administrativa", JsonNode.class);
        assertEquals(HttpStatus.OK, auditoriaAdmin.getStatusCode());
        assertTrue(auditoriaAdmin.getBody().size() >= 5);
        assertTrue(auditoriaAdmin.getBody().toString().contains("RECURSO"));
        assertTrue(auditoriaAdmin.getBody().toString().contains("USUARIO"));
        assertFalse(auditoriaAdmin.getBody().toString().contains("senha-solicitante-123"));
        assertEquals(HttpStatus.FORBIDDEN,
                solicitante.getForEntity("/auditoria-administrativa", String.class).getStatusCode());

        ResponseEntity<JsonNode> recursoAlterado = admin.exchange("/recursos/" + salaId,
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("nome", "Sala E2E atualizada", "restrito", false,
                        "emManutencao", false, "capacidade", 25, "localizacao", "Bloco E",
                        "competencias", "Aula e laboratorio"), jsonHeaders()), JsonNode.class);
        assertEquals(HttpStatus.OK, recursoAlterado.getStatusCode());
        ResponseEntity<JsonNode> auditoriaAtualizada = admin.getForEntity(
                "/auditoria-administrativa", JsonNode.class);
        assertTrue(auditoriaAtualizada.getBody().toString().contains("ALTERACAO"));

    }
}
