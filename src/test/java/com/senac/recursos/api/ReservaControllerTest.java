package com.senac.recursos.api;

import com.senac.recursos.dominio.*;
import com.senac.recursos.servico.*;
import com.senac.recursos.repositorio.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Testa o contrato HTTP do controller; o serviço é isolado com mock (teste unitário). */
@WebMvcTest(ReservaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReservaControllerTest {

    private static final Instant INICIO = Instant.parse("2026-11-10T10:00:00Z");
    private static final Instant FIM = Instant.parse("2026-11-10T11:00:00Z");
    private static final String JSON_VALIDO =
            "{\"recursoId\":1,\"solicitante\":\"prof.ana\","
          + "\"inicio\":\"2026-11-10T10:00:00Z\",\"fim\":\"2026-11-10T11:00:00Z\"}";

    @Autowired MockMvc mvc;
    @MockBean ReservaService service;
    @MockBean UsuarioRepository usuarios;

    private ResultActions enviar(String json) throws Exception {
        return mvc.perform(post("/reservas").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    void criaReservaERetorna201ComIdEEstado() throws Exception {
        Reserva reserva = new Reserva(new Recurso(TipoRecurso.SALA, "Sala 101", false),
                new Periodo(INICIO, FIM), "prof.ana");
        ReflectionTestUtils.setField(reserva, "id", 42L);
        when(service.criar(eq(1L), isNull(), eq("prof.ana"), eq(INICIO), eq(FIM))).thenReturn(reserva);

        enviar(JSON_VALIDO)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.estado").value("SOLICITADA"));
    }

    @Test
    void conflitoDeHorarioRetorna409ComMensagem() throws Exception {
        when(service.criar(any(), isNull(), any(), any(), any()))
                .thenThrow(new ConflitoHorarioException("Já existe reserva neste horário para o recurso"));

        enviar(JSON_VALIDO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Já existe reserva neste horário para o recurso"));
    }

    @Test
    void recursoEmManutencaoRetorna422() throws Exception {
        when(service.criar(any(), isNull(), any(), any(), any()))
                .thenThrow(new RecursoIndisponivelException("Recurso em manutenção"));

        enviar(JSON_VALIDO)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("Recurso em manutenção"));
    }

    @Test
    void periodoInvalidoRetorna422() throws Exception {
        when(service.criar(any(), isNull(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("O término deve ser posterior ao início"));

        enviar(JSON_VALIDO)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("O término deve ser posterior ao início"));
    }

    @Test
    void recursoInexistenteRetorna404() throws Exception {
        when(service.criar(any(), isNull(), any(), any(), any()))
                .thenThrow(new NoSuchElementException("Recurso não encontrado: 1"));

        enviar(JSON_VALIDO)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado: 1"));
    }

    @Test
    void transicaoDeEstadoValidaEhEncaminhadaAoServico() throws Exception {
        mvc.perform(patch("/reservas/42/estado")
                        .principal(new UsernamePasswordAuthenticationToken("responsavel", "n/a",
                                java.util.List.of(new SimpleGrantedAuthority("ROLE_RESPONSAVEL"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"APROVADA"}
                                """))
                .andExpect(status().isOk());
        verify(service).alterarEstado(42L, EstadoReserva.APROVADA, "responsavel", true);
    }

    @Test
    void transicaoSemEstadoRetorna400ESemChamarServico() throws Exception {
        mvc.perform(patch("/reservas/42/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void consultaAuditoriaRetornaLista() throws Exception {
        when(service.listarAuditoria(42L)).thenReturn(java.util.List.of());
        mvc.perform(get("/reservas/42/auditoria"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void consultaAuditoriaExpõeDadosDoEvento() throws Exception {
        Reserva reserva = new Reserva(new Recurso(TipoRecurso.SALA, "Sala 101", false),
                new Periodo(INICIO, FIM), "prof.ana");
        ReflectionTestUtils.setField(reserva, "id", 42L);
        ReservaAuditoria evento = new ReservaAuditoria(reserva, null,
                EstadoReserva.SOLICITADA, INICIO, "API");
        when(service.listarAuditoria(42L)).thenReturn(java.util.List.of(evento));

        mvc.perform(get("/reservas/42/auditoria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reservaId").value(42))
                .andExpect(jsonPath("$[0].estadoAnterior").doesNotExist())
                .andExpect(jsonPath("$[0].estadoNovo").value("SOLICITADA"))
                .andExpect(jsonPath("$[0].ocorridoEm").value("2026-11-10T10:00:00Z"))
                .andExpect(jsonPath("$[0].origem").value("API"));
    }

    @Test
    void reservaInexistenteNaConsultaDeAuditoriaRetorna404() throws Exception {
        when(service.listarAuditoria(404L))
                .thenThrow(new NoSuchElementException("Reserva não encontrada"));

        mvc.perform(get("/reservas/404/auditoria"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Reserva não encontrada"));
    }

    @Test
    void estadoDesconhecidoRetorna400ESemChamarServico() throws Exception {
        mvc.perform(patch("/reservas/42/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"ESTADO_QUE_NAO_EXISTE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Dados enviados inválidos"));
        verifyNoInteractions(service);
    }

    static Stream<String> corposInvalidos() {
        return Stream.of(
            "{\"recursoId\":1,\"solicitante\":\"\",\"inicio\":\"2026-11-10T10:00:00Z\",\"fim\":\"2026-11-10T11:00:00Z\"}",
            "{\"solicitante\":\"prof.ana\",\"inicio\":\"2026-11-10T10:00:00Z\",\"fim\":\"2026-11-10T11:00:00Z\"}",
            "{\"recursoId\":1,\"solicitante\":\"prof.ana\",\"fim\":\"2026-11-10T11:00:00Z\"}",
            "{\"recursoId\":1,\"solicitante\":\"prof.ana\",\"inicio\":\"2026-11-10T10:00:00Z\"}",
            "{\"recursoId\":1,\"solicitante\":\"prof.ana\",\"inicio\":\"ontem\",\"fim\":\"2026-11-10T11:00:00Z\"}",
            "isto não é json"
        );
    }

    @ParameterizedTest
    @MethodSource("corposInvalidos")
    void entradaInvalidaRetorna400ENuncaChamaOServico(String corpo) throws Exception {
        enviar(corpo).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
