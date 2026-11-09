package com.senac.recursos.dominio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecursoTest {

    @Test
    void guardaDadosInformadosNaCriacao() {
        Recurso r = new Recurso(TipoRecurso.MATERIAL, "Projetor", true);
        assertEquals(TipoRecurso.MATERIAL, r.getTipo());
        assertEquals("Projetor", r.getNome());
        assertTrue(r.isRestrito());
        assertFalse(r.isEmManutencao());
    }

    @Test
    void manutencaoPodeSerLigadaEDesligada() {
        Recurso r = new Recurso(TipoRecurso.SALA, "Lab 2", false);
        r.setEmManutencao(true);
        assertTrue(r.isEmManutencao());
        r.setEmManutencao(false);
        assertFalse(r.isEmManutencao());
    }
}
