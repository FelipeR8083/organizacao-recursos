package com.senac.recursos.servico;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {
    @Mock RecursoRepository recursos;
    @Mock ReservaRepository reservas;
    @Mock ConflitoEvitadoRepository conflitos;
    @InjectMocks RelatorioService relatorios;

    @Test
    void contabilizaMaterialAdicionalDaReservaAprovada() {
        Recurso sala = mock(Recurso.class);
        when(sala.getId()).thenReturn(1L);
        when(sala.getNome()).thenReturn("Sala 101");
        when(sala.getTipo()).thenReturn(TipoRecurso.SALA);

        Recurso projetor = mock(Recurso.class);
        when(projetor.getId()).thenReturn(2L);
        when(projetor.getNome()).thenReturn("Projetor");
        when(projetor.getTipo()).thenReturn(TipoRecurso.MATERIAL);

        Reserva reserva = mock(Reserva.class);
        when(reserva.getRecurso()).thenReturn(sala);
        when(reserva.getMateriais()).thenReturn(List.of(projetor));
        when(reserva.getEstado()).thenReturn(EstadoReserva.APROVADA);
        when(reserva.getInicio()).thenReturn(Instant.parse("2026-11-14T14:00:00Z"));
        when(reserva.getFim()).thenReturn(Instant.parse("2026-11-14T15:00:00Z"));
        when(recursos.findAll()).thenReturn(List.of(sala, projetor));
        when(reservas.findAll()).thenReturn(List.of(reserva));

        List<RelatorioService.Utilizacao> resultado = relatorios.utilizacao(
                Instant.parse("2026-11-14T00:00:00Z"),
                Instant.parse("2026-11-15T00:00:00Z"));

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).reservas());
        assertEquals(60, resultado.get(0).minutosAlocados());
        assertEquals(1, resultado.get(1).reservas());
        assertEquals(60, resultado.get(1).minutosAlocados());
    }
}
