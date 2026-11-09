package com.senac.recursos.dominio;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Histórico imutável de mudanças de estado de uma reserva.
 * A origem identifica o canal da operação; não representa identidade de usuário,
 * pois o sistema ainda não possui autenticação.
 */
@Entity
@org.hibernate.annotations.Immutable
@Table(name = "reserva_auditoria", indexes = {
        @Index(name = "idx_auditoria_reserva_data_id", columnList = "reserva_id, ocorrido_em, id")
})
public class ReservaAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false, updatable = false)
    private Reserva reserva;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 30, updatable = false)
    private EstadoReserva estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_novo", nullable = false, length = 30, updatable = false)
    private EstadoReserva estadoNovo;

    @Column(name = "ocorrido_em", nullable = false, updatable = false)
    private Instant ocorridoEm;

    @Column(nullable = false, length = 80, updatable = false)
    private String origem;

    protected ReservaAuditoria() {}

    public ReservaAuditoria(Reserva reserva, EstadoReserva estadoAnterior,
                            EstadoReserva estadoNovo, Instant ocorridoEm, String origem) {
        this.reserva = reserva;
        this.estadoAnterior = estadoAnterior;
        this.estadoNovo = estadoNovo;
        this.ocorridoEm = ocorridoEm;
        this.origem = origem;
    }

    public Long getId() { return id; }
    public Long getReservaId() { return reserva.getId(); }
    public EstadoReserva getEstadoAnterior() { return estadoAnterior; }
    public EstadoReserva getEstadoNovo() { return estadoNovo; }
    public Instant getOcorridoEm() { return ocorridoEm; }
    public String getOrigem() { return origem; }
}
