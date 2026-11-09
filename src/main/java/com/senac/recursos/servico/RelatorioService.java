package com.senac.recursos.servico;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class RelatorioService {
    public record Utilizacao(Long recursoId, String recurso, TipoRecurso tipo,
                             long reservas, long minutosAlocados) {}
    public record RelatorioConflitos(long total, List<ConflitoEvitado> eventos) {}

    private final RecursoRepository recursos;
    private final ReservaRepository reservas;
    private final ConflitoEvitadoRepository conflitos;

    public RelatorioService(RecursoRepository recursos, ReservaRepository reservas,
                            ConflitoEvitadoRepository conflitos) {
        this.recursos = recursos;
        this.reservas = reservas;
        this.conflitos = conflitos;
    }

    @Transactional(readOnly = true)
    public List<Utilizacao> utilizacao(Instant inicio, Instant fim) {
        validarPeriodo(inicio, fim);
        List<Reserva> todas = reservas.findAll();
        Set<EstadoReserva> contabilizados = EnumSet.of(EstadoReserva.SOLICITADA,
                EstadoReserva.APROVADA, EstadoReserva.EM_USO, EstadoReserva.CONCLUIDA);
        List<Utilizacao> resultado = new ArrayList<>();
        for (Recurso recurso : recursos.findAll()) {
            long quantidade = 0;
            long minutos = 0;
            for (Reserva reserva : todas) {
                boolean utiliza = reserva.getRecurso().getId().equals(recurso.getId())
                        || (reserva.getProfessor() != null
                        && reserva.getProfessor().getId().equals(recurso.getId()));
                if (!utiliza || !contabilizados.contains(reserva.getEstado())
                        || !reserva.getInicio().isBefore(fim) || !reserva.getFim().isAfter(inicio)) continue;
                Instant de = reserva.getInicio().isAfter(inicio) ? reserva.getInicio() : inicio;
                Instant ate = reserva.getFim().isBefore(fim) ? reserva.getFim() : fim;
                quantidade++;
                minutos += Duration.between(de, ate).toMinutes();
            }
            resultado.add(new Utilizacao(recurso.getId(), recurso.getNome(), recurso.getTipo(),
                    quantidade, minutos));
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public RelatorioConflitos conflitos(Instant inicio, Instant fim) {
        validarPeriodo(inicio, fim);
        List<ConflitoEvitado> eventos = conflitos.findByOcorridoEmBetweenOrderByOcorridoEmDesc(inicio, fim);
        return new RelatorioConflitos(eventos.size(), eventos);
    }

    private static void validarPeriodo(Instant inicio, Instant fim) {
        if (inicio == null || fim == null || !fim.isAfter(inicio)) {
            throw new IllegalArgumentException("Informe um intervalo válido: fim posterior ao início");
        }
    }
}
