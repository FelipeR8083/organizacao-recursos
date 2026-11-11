package com.senac.recursos.servico;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.*;
import java.time.Clock;
import com.senac.recursos.dominio.ReservaAuditoria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.NoSuchElementException;

@Service
public class ReservaService {

    private final RecursoRepository recursos;
    private final ReservaRepository reservas;
    private final ReservaAuditoriaRepository auditoria;
    private final Clock clock;
    private final ConflitoEvitadoService conflitosEvitados;
    private final NotificacaoService notificacoes;

    public ReservaService(RecursoRepository recursos, ReservaRepository reservas,
                          ReservaAuditoriaRepository auditoria, ConflitoEvitadoService conflitosEvitados,
                          NotificacaoService notificacoes) {
        this.recursos = recursos;
        this.reservas = reservas;
        this.auditoria = auditoria;
        this.conflitosEvitados = conflitosEvitados;
        this.notificacoes = notificacoes;
        this.clock = Clock.systemUTC();
    }

    @Transactional(readOnly = true)
    public List<Reserva> listar() { return reservas.findAll(); }

    /** Solicitantes veem somente suas reservas; gestores podem consultar todas. */
    @Transactional(readOnly = true)
    public List<Reserva> listarPara(String login, boolean gestor) {
        if (gestor) return listar();
        if (login == null || login.isBlank()) {
            throw new org.springframework.security.access.AccessDeniedException("Autenticação obrigatória");
        }
        return reservas.findAll().stream()
                .filter(r -> r.getSolicitante().equalsIgnoreCase(login))
                .toList();
    }

    /** A existência do histórico de terceiros não deve ser revelada a solicitantes. */
    @Transactional(readOnly = true)
    public List<ReservaAuditoria> listarAuditoriaPara(Long reservaId, String login, boolean gestor) {
        Reserva reserva = reservas.findById(reservaId)
                .orElseThrow(() -> new NoSuchElementException("Reserva não encontrada"));
        if (!gestor && (login == null || !reserva.getSolicitante().equalsIgnoreCase(login))) {
            throw new NoSuchElementException("Reserva não encontrada");
        }
        return auditoria.findByReserva_IdOrderByOcorridoEmAscIdAsc(reservaId);
    }

    @Transactional
    public Reserva alterarEstado(Long id, EstadoReserva estado) {
        return alterarEstado(id, estado, origemAtual(), false);
    }

    @Transactional
    public Reserva alterarEstado(Long id, EstadoReserva estado, String login, boolean responsavel) {
        if (estado == null) {
            throw new IllegalArgumentException("Estado é obrigatório");
        }
        Reserva reserva = reservas.buscarComLock(id)
                .orElseThrow(() -> new NoSuchElementException("Reserva não encontrada"));
        boolean recursoRestrito = reserva.getRecurso().isRestrito()
                || (reserva.getProfessor() != null && reserva.getProfessor().isRestrito());
        if (estado == EstadoReserva.APROVADA && recursoRestrito && !responsavel) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Somente um responsável pode aprovar reserva de recurso restrito");
        }
        EstadoReserva anterior = reserva.getEstado();
        reserva.transicionarPara(estado);
        Reserva salva = reservas.save(reserva);
        auditoria.save(new ReservaAuditoria(salva, anterior, estado, Instant.now(clock), origemAtual()));
        notificacoes.enviar(salva.getSolicitante(), "Reserva atualizada", "A reserva " + id + " mudou de " + anterior + " para " + estado + ".");
        return salva;
    }



    @Transactional
    public Reserva alterar(Long id, Long recursoId, Long professorId,
                           Instant inicio, Instant fim, String login, boolean podeGerenciar) {
        Periodo periodo = new Periodo(inicio, fim);
        Reserva reserva = reservas.buscarComLock(id)
                .orElseThrow(() -> new NoSuchElementException("Reserva não encontrada"));
        if (!podeGerenciar && (login == null || !reserva.getSolicitante().equalsIgnoreCase(login))) {
            throw new org.springframework.security.access.AccessDeniedException("Você só pode alterar suas próprias reservas");
        }
        if (professorId != null && professorId.equals(recursoId)) {
            throw new IllegalArgumentException("O professor deve ser diferente do recurso principal");
        }

        Recurso principal;
        Recurso professor = null;
        if (professorId == null) {
            principal = recursos.buscarComLock(recursoId)
                    .orElseThrow(() -> new NoSuchElementException("Recurso não encontrado: " + recursoId));
        } else {
            Long primeiroId = Math.min(recursoId, professorId);
            Long segundoId = Math.max(recursoId, professorId);
            Recurso primeiro = recursos.buscarComLock(primeiroId)
                    .orElseThrow(() -> new NoSuchElementException("Recurso não encontrado: " + primeiroId));
            Recurso segundo = recursos.buscarComLock(segundoId)
                    .orElseThrow(() -> new NoSuchElementException("Recurso não encontrado: " + segundoId));
            principal = recursoId.equals(primeiroId) ? primeiro : segundo;
            professor = professorId.equals(primeiroId) ? primeiro : segundo;
        }
        if (principal.getTipo() != TipoRecurso.SALA && principal.getTipo() != TipoRecurso.MATERIAL) {
            throw new IllegalArgumentException("O recurso principal deve ser uma sala ou um material");
        }
        if (principal.isEmManutencao() || (professor != null && professor.isEmManutencao())) {
            throw new RecursoIndisponivelException("Recurso ou professor em manutenção");
        }
        if (professor != null && professor.getTipo() != TipoRecurso.PROFESSOR) {
            throw new IllegalArgumentException("O recurso vinculado como professor deve ser do tipo PROFESSOR");
        }
        if (reservas.existeConflitoIgnorando(recursoId, EstadoReserva.ativos(), periodo.inicio(), periodo.fim(), id)) {
            conflitosEvitados.registrar(recursoId, periodo.inicio(), periodo.fim(), "Conflito ao alterar recurso principal");
            throw new ConflitoHorarioException("Já existe reserva neste horário para o recurso principal");
        }
        if (professorId != null && reservas.existeConflitoIgnorando(professorId, EstadoReserva.ativos(), periodo.inicio(), periodo.fim(), id)) {
            conflitosEvitados.registrar(professorId, periodo.inicio(), periodo.fim(), "Conflito ao alterar agenda do professor");
            throw new ConflitoHorarioException("O professor já possui reserva neste horário");
        }
        if (!reserva.getMateriais().isEmpty() && principal.getTipo() != TipoRecurso.SALA) {
            throw new IllegalArgumentException("Uma reserva com materiais vinculados exige uma sala");
        }
        for (Recurso material : reserva.getMateriais()) {
            if (material.isEmManutencao()) {
                throw new RecursoIndisponivelException("Material em manutenção: " + material.getId());
            }
            if (reservas.existeConflitoIgnorando(material.getId(), EstadoReserva.ativos(),
                    periodo.inicio(), periodo.fim(), id)) {
                throw new ConflitoHorarioException("Material já reservado neste horário: " + material.getNome());
            }
        }
        String detalhes = "Recurso " + reserva.getRecurso().getId() + " -> " + recursoId
                + "; professor " + (reserva.getProfessor() == null ? "-" : reserva.getProfessor().getId())
                + " -> " + (professorId == null ? "-" : professorId)
                + "; período " + reserva.getInicio() + "/" + reserva.getFim()
                + " -> " + periodo.inicio() + "/" + periodo.fim();
        EstadoReserva estado = reserva.getEstado();
        reserva.reagendar(principal, professor, periodo);
        Reserva salva = reservas.save(reserva);
        auditoria.save(new ReservaAuditoria(salva, estado, estado, Instant.now(clock),
                origemAtual(), "ALTERACAO", detalhes));
        notificacoes.enviar(salva.getSolicitante(), "Reserva alterada", "A reserva " + id + " teve seus dados atualizados.");
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
        notificacoes.enviar(salva.getSolicitante(), "Reserva cancelada", "A reserva " + id + " foi cancelada.");
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
        return criar(recursoId, null, solicitante, inicio, fim);
    }

    @Transactional
    public Reserva criar(Long recursoId, Long professorId, String solicitante, Instant inicio, Instant fim) {
        return criar(recursoId, professorId, List.of(), solicitante, inicio, fim);
    }

    @Transactional
    public Reserva criar(Long recursoId, Long professorId, List<Long> materialIds,
                         String solicitante, Instant inicio, Instant fim) {
        Periodo periodo = new Periodo(inicio, fim);
        String solicitanteNormalizado = solicitante == null ? "" : solicitante.trim();
        if (solicitanteNormalizado.isBlank()) {
            throw new IllegalArgumentException("Solicitante é obrigatório");
        }
        if (professorId != null && professorId.equals(recursoId)) {
            throw new IllegalArgumentException("O professor deve ser um recurso diferente do recurso principal");
        }

        List<Long> materiaisSolicitados = materialIds == null ? List.of() : materialIds;
        if (materiaisSolicitados.stream().anyMatch(id -> id == null || id.equals(recursoId)
                || id.equals(professorId))) {
            throw new IllegalArgumentException("Materiais devem ser diferentes da sala e do professor");
        }
        if (new TreeSet<>(materiaisSolicitados).size() != materiaisSolicitados.size()) {
            throw new IllegalArgumentException("Não repita o mesmo material");
        }
        if (!materiaisSolicitados.isEmpty() && recursos.findById(recursoId)
                .map(recurso -> recurso.getTipo() != TipoRecurso.SALA).orElse(false)) {
            throw new IllegalArgumentException("A reserva conjunta exige uma sala como recurso principal");
        }
        // Trava todos os recursos em uma única ordem global para evitar deadlocks
        // quando reservas concorrentes usam sala, professor e materiais diferentes.
        TreeSet<Long> idsParaBloquear = new TreeSet<>(materiaisSolicitados);
        idsParaBloquear.add(recursoId);
        if (professorId != null) idsParaBloquear.add(professorId);
        java.util.Map<Long, Recurso> bloqueados = new java.util.HashMap<>();
        for (Long idRecurso : idsParaBloquear) {
            bloqueados.put(idRecurso, recursos.buscarComLock(idRecurso)
                    .orElseThrow(() -> new NoSuchElementException("Recurso não encontrado: " + idRecurso)));
        }
        Recurso principal = bloqueados.get(recursoId);
        Recurso professor = professorId == null ? null : bloqueados.get(professorId);
        List<Recurso> materiais = new ArrayList<>();
        for (Long materialId : new TreeSet<>(materiaisSolicitados)) {
            Recurso material = bloqueados.get(materialId);
            if (material.getTipo() != TipoRecurso.MATERIAL) {
                throw new IllegalArgumentException("O recurso vinculado deve ser do tipo MATERIAL");
            }
            if (material.isEmManutencao()) {
                throw new RecursoIndisponivelException("Material em manutenção: " + materialId);
            }
            materiais.add(material);
        }

        if (principal.getTipo() != TipoRecurso.SALA && principal.getTipo() != TipoRecurso.MATERIAL) {
            throw new IllegalArgumentException("O recurso principal deve ser uma sala ou um material");
        }
        if (principal.isEmManutencao() || (professor != null && professor.isEmManutencao())) {
            throw new RecursoIndisponivelException("Recurso ou professor em manutenção");
        }
        if (professor != null && professor.getTipo() != TipoRecurso.PROFESSOR) {
            throw new IllegalArgumentException("O recurso vinculado como professor deve ser do tipo PROFESSOR");
        }
        if (reservas.existeConflito(recursoId, EstadoReserva.ativos(), periodo.inicio(), periodo.fim())) {
            conflitosEvitados.registrar(recursoId, periodo.inicio(), periodo.fim(), "Conflito no recurso principal");
            throw new ConflitoHorarioException("Já existe reserva neste horário para o recurso principal");
        }
        if (professorId != null && reservas.existeConflito(professorId, EstadoReserva.ativos(), periodo.inicio(), periodo.fim())) {
            conflitosEvitados.registrar(professorId, periodo.inicio(), periodo.fim(), "Conflito na agenda do professor");
            throw new ConflitoHorarioException("O professor já possui reserva neste horário");
        }
        for (Recurso material : materiais) {
            if (reservas.existeConflito(material.getId(), EstadoReserva.ativos(), periodo.inicio(), periodo.fim())) {
                conflitosEvitados.registrar(material.getId(), periodo.inicio(), periodo.fim(), "Conflito no material");
                throw new ConflitoHorarioException("Material já reservado neste horário: " + material.getNome());
            }
        }
        Reserva novaReserva = new Reserva(principal, professor, periodo, solicitanteNormalizado);
        novaReserva.definirMateriais(materiais);
        Reserva criada = reservas.save(novaReserva);
        auditoria.save(new ReservaAuditoria(criada, null, EstadoReserva.SOLICITADA,
                Instant.now(clock), origemAtual()));
        notificacoes.enviar(criada.getSolicitante(), "Reserva solicitada", "Sua solicitação de reserva " + criada.getId() + " foi registrada.");
        return criada;
    }
}
