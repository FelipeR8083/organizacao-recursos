package com.senac.recursos.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class PeriodoTest {

    private static Periodo p(int ini, int fim) {
        Instant base = Instant.parse("2026-11-10T00:00:00Z");
        return new Periodo(base.plusSeconds(ini * 3600L), base.plusSeconds(fim * 3600L));
    }

    @ParameterizedTest(name = "[{0},{1}) vs [{2},{3}) => {4}")
    @CsvSource({
        "8,10, 9,11,  true",
        "9,11, 8,10,  true",
        "8,12, 9,10,  true",
        "9,10, 8,12,  true",
        "8,10, 8,10,  true",
        "8,10, 10,12, false",
        "10,12, 8,10, false",
        "8,9, 11,12,  false"
    })
    void detectaSobreposicao(int a, int b, int c, int d, boolean esperado) {
        assertEquals(esperado, p(a, b).sobrepoe(p(c, d)));
    }

    @Test
    void terminoDeveSerPosteriorAoInicio() {
        assertThrows(IllegalArgumentException.class, () -> p(10, 10));
        assertThrows(IllegalArgumentException.class, () -> p(10, 8));
    }

    @Test
    void rejeitaNulos() {
        assertThrows(IllegalArgumentException.class, () -> new Periodo(null, Instant.now()));
    }
}
