package com.senac.recursos.dominio;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Reserva {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Recurso recurso;
    @ManyToOne
    private Recurso professor;
    @Column(nullable = false)
    private Instant inicio;
    @Column(nullable = false)
    private Instant fim;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private EstadoReserva estado = EstadoReserva.SOLICITADA;
    @Column(nullable = false)
    private String solicitante;

    protected Reserva() {}

    public Reserva(Recurso recurso, Periodo periodo, String solicitante) {
        this(recurso, null, periodo, solicitante);
    }

    public Reserva(Recurso recurso, Recurso professor, Periodo periodo, String solicitante) {
        this.recurso = recurso;
        this.professor = professor;
        this.inicio = periodo.inicio();
        this.fim = periodo.fim();
        this.solicitante = solicitante;
    }

    public void transicionarPara(EstadoReserva destino) {
        if (!estado.podeIrPara(destino)) {
            throw new IllegalStateException("Transição inválida: " + estado + " -> " + destino);
        }
        // O serviço registra a transição na mesma transação, preservando o histórico.
        this.estado = destino;
    }

    public Long getId() { return id; }
    public Recurso getRecurso() { return recurso; }
    public Recurso getProfessor() { return professor; }
    public Instant getInicio() { return inicio; }
    public Instant getFim() { return fim; }
    public EstadoReserva getEstado() { return estado; }
    public String getSolicitante() { return solicitante; }
}
