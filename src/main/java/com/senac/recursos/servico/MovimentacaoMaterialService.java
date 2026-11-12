package com.senac.recursos.servico;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class MovimentacaoMaterialService {
    private final ReservaRepository reservas;
    private final MovimentacaoMaterialRepository movimentacoes;
    private final ReservaService reservaService;

    public MovimentacaoMaterialService(ReservaRepository reservas,
                                       MovimentacaoMaterialRepository movimentacoes,
                                       ReservaService reservaService) {
        this.reservas = reservas;
        this.movimentacoes = movimentacoes;
        this.reservaService = reservaService;
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoMaterial> listar() {
        return movimentacoes.findAll();
    }

    /** Mantém a API anterior para reservas de um único material. */
    @Transactional
    public MovimentacaoMaterial retirar(Long reservaId, String responsavel) {
        return retirar(reservaId, null, responsavel);
    }

    @Transactional
    public MovimentacaoMaterial retirar(Long reservaId, Long materialId, String responsavel) {
        Reserva reserva = reservas.buscarComLock(reservaId)
                .orElseThrow(() -> new NoSuchElementException("Reserva não encontrada"));
        Recurso material = selecionarMaterial(reserva, materialId);

        if (movimentacoes.findByReserva_IdAndMaterial_Id(reservaId, material.getId()).isPresent()) {
            throw new IllegalStateException("A retirada deste material já foi registrada");
        }
        if (reserva.getEstado() != EstadoReserva.APROVADA && reserva.getEstado() != EstadoReserva.EM_USO) {
            throw new IllegalStateException("Somente reservas aprovadas ou em uso podem registrar retiradas");
        }
        if (reserva.getEstado() == EstadoReserva.APROVADA) {
            reservaService.alterarEstado(reservaId, EstadoReserva.EM_USO);
        }
        return movimentacoes.save(new MovimentacaoMaterial(reserva, material, Instant.now(), responsavel));
    }

    /** Mantém a API anterior para reservas de um único material. */
    @Transactional
    public MovimentacaoMaterial devolver(Long reservaId, String responsavel) {
        return devolver(reservaId, null, responsavel);
    }

    @Transactional
    public MovimentacaoMaterial devolver(Long reservaId, Long materialId, String responsavel) {
        Reserva reserva = reservas.buscarComLock(reservaId)
                .orElseThrow(() -> new NoSuchElementException("Reserva não encontrada"));
        Recurso material = selecionarMaterial(reserva, materialId);
        MovimentacaoMaterial movimento = movimentacoes
                .findByReserva_IdAndMaterial_Id(reservaId, material.getId())
                .orElseThrow(() -> new NoSuchElementException("Retirada deste material não encontrada"));

        if (reserva.getEstado() != EstadoReserva.EM_USO) {
            throw new IllegalStateException("Somente reservas em uso permitem devolução");
        }
        movimento.registrarDevolucao(Instant.now(), responsavel);
        movimentacoes.saveAndFlush(movimento);

        // A reserva conjunta só é concluída depois da devolução de TODOS os materiais.
        boolean todosDevolvidos = materiaisDaReserva(reserva).stream().allMatch(item ->
                movimentacoes.findByReserva_IdAndMaterial_Id(reservaId, item.getId())
                        .map(mov -> mov.getDevolucaoEm() != null).orElse(false));
        if (todosDevolvidos) {
            reservaService.alterarEstado(reservaId, EstadoReserva.CONCLUIDA);
        }
        return movimento;
    }

    private static List<Recurso> materiaisDaReserva(Reserva reserva) {
        List<Recurso> materiais = new ArrayList<>(reserva.getMateriais());
        if (reserva.getRecurso().getTipo() == TipoRecurso.MATERIAL) {
            materiais.add(reserva.getRecurso());
        }
        return materiais;
    }

    private static Recurso selecionarMaterial(Reserva reserva, Long materialId) {
        List<Recurso> materiais = materiaisDaReserva(reserva);
        if (materialId == null) {
            if (materiais.size() != 1) {
                throw new IllegalArgumentException("Informe o material específico desta reserva");
            }
            return materiais.get(0);
        }
        return materiais.stream()
                .filter(material -> material.getId().equals(materialId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Material não pertence a esta reserva"));
    }
}
