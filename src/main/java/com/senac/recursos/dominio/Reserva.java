package com.senac.recursos.dominio;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Reserva {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Recurso recurso;
    @ManyToOne
    private Recurso professor;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "reserva_material", joinColumns = @JoinColumn(name = "reserva_id"), inverseJoinColumns = @JoinColumn(name = "material_id"))
    private List<Recurso> materiais = new ArrayList<>();
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

    public void reagendar(Recurso novoRecurso, Recurso novoProfessor, Periodo periodo) {
        if (estado != EstadoReserva.SOLICITADA) {
            throw new IllegalStateException("Somente reservas solicitadas podem ser alteradas");
        }
        this.recurso = novoRecurso;
        this.professor = novoProfessor;
        this.inicio = periodo.inicio();
        this.fim = periodo.fim();
    }

    public void definirMateriais(List<Recurso> novosMateriais) {
        this.materiais.clear();
        this.materiais.addAll(novosMateriais);
    }

    public List<Recurso> getMateriais() { return List.copyOf(materiais); }

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
