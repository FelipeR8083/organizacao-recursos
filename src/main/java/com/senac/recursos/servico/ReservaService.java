package com.senac.recursos.servico;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.*;
import java.time.Clock;
import com.senac.recursos.dominio.ReservaAuditoria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReservaService {

    private final RecursoRepository recursos;
    private final ReservaRepository reservas;
    private final ReservaAuditoriaRepository auditoria;
    private final Clock clock;

    public ReservaService(RecursoRepository recursos, ReservaRepository reservas,
                          ReservaAuditoriaRepository auditoria) {
        this.recursos = recursos;
        this.reservas = reservas;
        this.auditoria = auditoria;
        this.clock = Clock.systemUTC();
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
        EstadoReserva anterior = reserva.getEstado();
        reserva.transicionarPara(estado);
        Reserva salva = reservas.save(reserva);
        auditoria.save(new ReservaAuditoria(salva, anterior, estado, Instant.now(clock), origemAtual()));
        return salva;
    }


    @Transactional
    public void cancelar(Long id, String login, boolean podeGerenciar) {
        Reserva reserva = reservas.buscarComLock(id)
                .orElseThrow(() -> new NoSuchElementException("Reserva não encontrada"));
        if (!podeGerenciar && (login == null || !reserva.getSolicitante().equalsIgnoreCase(login))) {
            throw new org.springframework.security.access.AccessDeniedException("Você só pode cancelar suas próprias reservas");
        }
        EstadoReserva anterior = reserva.getEstado();
        reserva.transicionarPara(EstadoReserva.CANCELADA);
        Reserva salva = reservas.save(reserva);
        auditoria.save(new ReservaAuditoria(salva, anterior, EstadoReserva.CANCELADA,
                Instant.now(clock), origemAtual()));
    }

    @Transactional(readOnly = true)
    public List<ReservaAuditoria> listarAuditoria(Long reservaId) {
        if (!reservas.existsById(reservaId)) {
            throw new NoSuchElementException("Reserva não encontrada");
        }
        return auditoria.findByReserva_IdOrderByOcorridoEmAscIdAsc(reservaId);
    }

    private String origemAtual() {
        var autenticacao = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (autenticacao == null || !autenticacao.isAuthenticated()
                || "anonymousUser".equals(autenticacao.getPrincipal())) return "API";
        return autenticacao.getName();
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
        Reserva criada = reservas.save(new Reserva(recurso, periodo, solicitanteNormalizado));
        auditoria.save(new ReservaAuditoria(criada, null, EstadoReserva.SOLICITADA,
                Instant.now(clock), origemAtual()));
        return criada;
    }
}
