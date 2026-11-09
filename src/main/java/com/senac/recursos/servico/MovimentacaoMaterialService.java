package com.senac.recursos.servico;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
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

    @Transactional
    public MovimentacaoMaterial retirar(Long reservaId, String responsavel) {
        Reserva reserva = reservas.buscarComLock(reservaId)
                .orElseThrow(() -> new NoSuchElementException("Reserva não encontrada"));
        if (reserva.getRecurso().getTipo() != TipoRecurso.MATERIAL) {
            throw new IllegalArgumentException("A reserva não é de um material/equipamento");
        }
        if (movimentacoes.findByReserva_Id(reservaId).isPresent()) {
            throw new IllegalStateException("A retirada já foi registrada");
        }
        if (reserva.getEstado() != EstadoReserva.APROVADA) {
            throw new IllegalStateException("Somente reservas aprovadas podem ter retirada registrada");
        }
        reservaService.alterarEstado(reservaId, EstadoReserva.EM_USO);
        return movimentacoes.save(new MovimentacaoMaterial(reserva, reserva.getRecurso(),
                Instant.now(), responsavel));
    }

    @Transactional
    public MovimentacaoMaterial devolver(Long reservaId, String responsavel) {
        Reserva reserva = reservas.buscarComLock(reservaId)
                .orElseThrow(() -> new NoSuchElementException("Reserva não encontrada"));
        MovimentacaoMaterial movimentacao = movimentacoes.findByReserva_Id(reservaId)
                .orElseThrow(() -> new NoSuchElementException("Retirada não encontrada"));
        if (reserva.getEstado() != EstadoReserva.EM_USO) {
            throw new IllegalStateException("Somente material em uso pode ser devolvido");
        }
        movimentacao.registrarDevolucao(Instant.now(), responsavel);
        reservaService.alterarEstado(reservaId, EstadoReserva.CONCLUIDA);
        return movimentacao;
    }
}
