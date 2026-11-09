package com.senac.recursos.dominio;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "movimentacoes_material")
public class MovimentacaoMaterial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private Reserva reserva;
    @ManyToOne(optional = false)
    @JoinColumn(name = "recurso_id", nullable = false)
    private Recurso material;
    @Column(name = "retirada_em", nullable = false)
    private Instant retiradaEm;
    @Column(name = "retirada_por", nullable = false, length = 80)
    private String retiradaPor;
    @Column(name = "devolucao_em")
    private Instant devolucaoEm;
    @Column(name = "devolucao_por", length = 80)
    private String devolucaoPor;

    protected MovimentacaoMaterial() {}
    public MovimentacaoMaterial(Reserva reserva, Recurso material, Instant retiradaEm, String retiradaPor) {
        this.reserva = reserva;
        this.material = material;
        this.retiradaEm = retiradaEm;
        this.retiradaPor = retiradaPor;
    }
    public void registrarDevolucao(Instant quando, String quem) {
        if (devolucaoEm != null) throw new IllegalStateException("Material já foi devolvido");
        this.devolucaoEm = quando;
        this.devolucaoPor = quem;
    }
    public Long getId() { return id; }
    public Long getReservaId() { return reserva.getId(); }
    public Long getMaterialId() { return material.getId(); }
    public Instant getRetiradaEm() { return retiradaEm; }
    public String getRetiradaPor() { return retiradaPor; }
    public Instant getDevolucaoEm() { return devolucaoEm; }
    public String getDevolucaoPor() { return devolucaoPor; }
}
