package com.senac.recursos.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    private static final Instant INICIO = Instant.parse("2026-11-10T10:00:00Z");
    private static final Instant FIM = Instant.parse("2026-11-10T11:00:00Z");

    private Recurso sala;
    private Reserva reserva;

    @BeforeEach
    void preparar() {
        sala = new Recurso(TipoRecurso.SALA, "Sala 101", false);
        reserva = new Reserva(sala, new Periodo(INICIO, FIM), "prof.ana");
    }

    @Test
    void novaReservaComecaSolicitadaECopiaOsDados() {
        assertEquals(EstadoReserva.SOLICITADA, reserva.getEstado());
        assertEquals(INICIO, reserva.getInicio());
        assertEquals(FIM, reserva.getFim());
        assertEquals("prof.ana", reserva.getSolicitante());
        assertSame(sala, reserva.getRecurso());
    }

    @Test
    void percorreOFluxoCompletoAteConcluida() {
        reserva.transicionarPara(EstadoReserva.APROVADA);
        reserva.transicionarPara(EstadoReserva.EM_USO);
        reserva.transicionarPara(EstadoReserva.CONCLUIDA);
        assertEquals(EstadoReserva.CONCLUIDA, reserva.getEstado());
    }

    @Test
    void transicaoInvalidaLancaExcecaoEMantemOEstado() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> reserva.transicionarPara(EstadoReserva.EM_USO));
        assertTrue(e.getMessage().contains("SOLICITADA"));
        assertEquals(EstadoReserva.SOLICITADA, reserva.getEstado());
    }

    @Test
    void reservaEmUsoNaoPodeSerCancelada() {
        reserva.transicionarPara(EstadoReserva.APROVADA);
        reserva.transicionarPara(EstadoReserva.EM_USO);
        assertThrows(IllegalStateException.class,
                () -> reserva.transicionarPara(EstadoReserva.CANCELADA));
        assertEquals(EstadoReserva.EM_USO, reserva.getEstado());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoReserva.class, names = {"REJEITADA", "CANCELADA"})
    void solicitadaPodeTerminarRejeitadaOuCancelada(EstadoReserva destino) {
        reserva.transicionarPara(destino);
        assertEquals(destino, reserva.getEstado());
    }

    @Test
    void estadoFinalNaoPermiteNovasTransicoes() {
        reserva.transicionarPara(EstadoReserva.CANCELADA);
        for (EstadoReserva destino : EstadoReserva.values()) {
            assertThrows(IllegalStateException.class, () -> reserva.transicionarPara(destino));
        }
    }
}
