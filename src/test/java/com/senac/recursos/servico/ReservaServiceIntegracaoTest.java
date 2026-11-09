package com.senac.recursos.servico;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.Instant;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

/** Regras de reserva verificadas contra um PostgreSQL real (requer Docker). */
@Testcontainers
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class ReservaServiceIntegracaoTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final Instant DIA = Instant.parse("2026-11-10T00:00:00Z");

    @Autowired ReservaService service;
    @Autowired RecursoRepository recursos;
    @Autowired ReservaRepository reservas;
    @Autowired ReservaAuditoriaRepository auditoria;

    private Recurso sala;

    private static Instant hora(int h) { return DIA.plusSeconds(h * 3600L); }

    @BeforeEach
    void limpar() {
        auditoria.deleteAll();
        reservas.deleteAll();
        recursos.deleteAll();
        sala = recursos.save(new Recurso(TipoRecurso.SALA, "Sala 101", false));
    }

    @Test
    void criaReservaSolicitadaQuandoHorarioEstaLivre() {
        Reserva r = service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        assertNotNull(r.getId());
        assertEquals(EstadoReserva.SOLICITADA, r.getEstado());
        assertEquals(1, reservas.count());
    }

    @Test
    void reservasQueApenasSeTocamSaoAceitas() {
        service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        assertDoesNotThrow(() -> service.criar(sala.getId(), "prof.bia", hora(11), hora(12)));
        assertDoesNotThrow(() -> service.criar(sala.getId(), "prof.caio", hora(9), hora(10)));
        assertEquals(3, reservas.count());
    }

    @Test
    void sobreposicaoParcialEhRejeitada() {
        service.criar(sala.getId(), "prof.ana", hora(10), hora(12));
        assertThrows(ConflitoHorarioException.class,
                () -> service.criar(sala.getId(), "prof.bia", hora(11), hora(13)));
        assertEquals(1, reservas.count());
    }

    @Test
    void reservaContidaEmOutraEhRejeitada() {
        service.criar(sala.getId(), "prof.ana", hora(8), hora(12));
        assertThrows(ConflitoHorarioException.class,
                () -> service.criar(sala.getId(), "prof.bia", hora(9), hora(10)));
    }

    @Test
    void mesmoHorarioEmRecursosDiferentesEhPermitido() {
        Recurso outraSala = recursos.save(new Recurso(TipoRecurso.SALA, "Sala 102", false));
        service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        assertDoesNotThrow(() -> service.criar(outraSala.getId(), "prof.bia", hora(10), hora(11)));
    }

    @Test
    void professorTambemNaoPodeTerHorariosSobrepostos() {
        Recurso professor = recursos.save(new Recurso(TipoRecurso.PROFESSOR, "Prof. Silva", false));
        service.criar(professor.getId(), "coord.ana", hora(10), hora(12));
        assertThrows(ConflitoHorarioException.class,
                () -> service.criar(professor.getId(), "coord.bia", hora(11), hora(13)));
    }

    @Test
    void reservaCanceladaLiberaOHorario() {
        Reserva primeira = service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        primeira.transicionarPara(EstadoReserva.CANCELADA);
        reservas.save(primeira);

        assertDoesNotThrow(() -> service.criar(sala.getId(), "prof.bia", hora(10), hora(11)));
    }

    @Test
    void recursoInexistenteLancaNoSuchElement() {
        assertThrows(NoSuchElementException.class,
                () -> service.criar(999_999L, "prof.ana", hora(10), hora(11)));
    }

    @Test
    void periodoInvalidoEhRejeitadoSemGravarNada() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(sala.getId(), "prof.ana", hora(11), hora(10)));
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(sala.getId(), "prof.ana", hora(10), hora(10)));
        assertEquals(0, reservas.count());
    }
    @Test
    void solicitanteVazioOuComEspacosEhRejeitado() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(sala.getId(), "   ", hora(10), hora(11)));
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(sala.getId(), null, hora(10), hora(11)));
        assertEquals(0, reservas.count());
    }

    @Test
    void solicitanteEhArmazenadoSemEspacosNasExtremidades() {
        Reserva reserva = service.criar(sala.getId(), "  prof.ana  ", hora(10), hora(11));
        assertEquals("prof.ana", reserva.getSolicitante());
    }
}
