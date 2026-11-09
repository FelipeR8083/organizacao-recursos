package com.senac.recursos.dominio;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Histórico imutável de eventos de uma reserva, incluindo transições e alterações.
 * A origem registra o login autenticado ou o canal técnico quando não há contexto de segurança.
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

    @Column(name = "tipo_evento", nullable = false, length = 30, updatable = false)
    private String tipoEvento = "ESTADO";

    @Column(length = 500, updatable = false)
    private String detalhes;

    protected ReservaAuditoria() {}

    public ReservaAuditoria(Reserva reserva, EstadoReserva estadoAnterior,
                            EstadoReserva estadoNovo, Instant ocorridoEm, String origem) {
        this(reserva, estadoAnterior, estadoNovo, ocorridoEm, origem, "ESTADO", null);
    }

    public ReservaAuditoria(Reserva reserva, EstadoReserva estadoAnterior,
                            EstadoReserva estadoNovo, Instant ocorridoEm, String origem,
                            String tipoEvento, String detalhes) {
        this.reserva = reserva;
        this.estadoAnterior = estadoAnterior;
        this.estadoNovo = estadoNovo;
        this.ocorridoEm = ocorridoEm;
        this.origem = origem;
        this.tipoEvento = tipoEvento;
        this.detalhes = detalhes;
    }

    public Long getId() { return id; }
    public Long getReservaId() { return reserva.getId(); }
    public EstadoReserva getEstadoAnterior() { return estadoAnterior; }
    public EstadoReserva getEstadoNovo() { return estadoNovo; }
    public Instant getOcorridoEm() { return ocorridoEm; }
    public String getOrigem() { return origem; }
    public String getTipoEvento() { return tipoEvento; }
    public String getDetalhes() { return detalhes; }
}
