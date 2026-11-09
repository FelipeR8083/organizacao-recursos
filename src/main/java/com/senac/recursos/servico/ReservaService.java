package com.senac.recursos.servico;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReservaService {

    private final RecursoRepository recursos;
    private final ReservaRepository reservas;

    public ReservaService(RecursoRepository recursos, ReservaRepository reservas) {
        this.recursos = recursos;
        this.reservas = reservas;
    }

    @Transactional(readOnly = true)
    public List<Reserva> listar() { return reservas.findAll(); }

    @Transactional
    public Reserva alterarEstado(Long id, EstadoReserva estado) {
        if (estado == null) {
            throw new IllegalArgumentException("Estado é obrigatório");
        }
        Reserva reserva = reservas.buscarComLock(id)
                .orElseThrow(() -> new NoSuchElementException("Reserva não encontrada"));
        reserva.transicionarPara(estado);
        return reservas.save(reserva);
    }

    @Transactional
    public Reserva criar(Long recursoId, String solicitante, Instant inicio, Instant fim) {
        Periodo periodo = new Periodo(inicio, fim);
        String solicitanteNormalizado = solicitante == null ? "" : solicitante.trim();
        if (solicitanteNormalizado.isBlank()) {
            throw new IllegalArgumentException("Solicitante é obrigatório");
        }

        // O lock na linha do recurso garante que duas solicitações simultâneas
        // sejam processadas uma de cada vez: apenas uma será aceita.
        Recurso recurso = recursos.buscarComLock(recursoId)
                .orElseThrow(() -> new NoSuchElementException("Recurso não encontrado: " + recursoId));

        if (recurso.isEmManutencao()) {
            throw new RecursoIndisponivelException("Recurso em manutenção");
        }
        if (reservas.existeConflito(recursoId, EstadoReserva.ativos(), periodo.inicio(), periodo.fim())) {
            throw new ConflitoHorarioException("Já existe reserva neste horário para o recurso");
        }
        return reservas.save(new Reserva(recurso, periodo, solicitanteNormalizado));
    }
}
