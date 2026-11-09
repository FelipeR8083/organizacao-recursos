package com.senac.recursos.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import static com.senac.recursos.dominio.EstadoReserva.*;
import static org.junit.jupiter.api.Assertions.*;

class EstadoReservaTest {

    @ParameterizedTest
    @CsvSource({
        "SOLICITADA,APROVADA", "SOLICITADA,REJEITADA", "SOLICITADA,CANCELADA",
        "APROVADA,EM_USO", "APROVADA,CANCELADA", "APROVADA,NAO_COMPARECEU",
        "EM_USO,CONCLUIDA"
    })
    void transicoesValidas(EstadoReserva de, EstadoReserva para) {
        assertTrue(de.podeIrPara(para));
    }

    @ParameterizedTest
    @CsvSource({
        "SOLICITADA,EM_USO", "SOLICITADA,CONCLUIDA", "APROVADA,SOLICITADA",
        "EM_USO,CANCELADA",
        "EM_USO,APROVADA", "CONCLUIDA,EM_USO"
    })
    void transicoesInvalidas(EstadoReserva de, EstadoReserva para) {
        assertFalse(de.podeIrPara(para));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoReserva.class, names = {"CONCLUIDA", "REJEITADA", "CANCELADA", "NAO_COMPARECEU"})
    void estadosFinaisNaoTemSaida(EstadoReserva estadoFinal) {
        assertTrue(estadoFinal.proximosPermitidos().isEmpty());
    }

    @Test
    void apenasEstadosAtivosOcupamOHorario() {
        assertEquals(java.util.Set.of(SOLICITADA, APROVADA, EM_USO), EstadoReserva.ativos());
    }
}
