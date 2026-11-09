package com.senac.recursos.dominio;

import jakarta.persistence.*;

@Entity
public class Recurso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TipoRecurso tipo;
    @Column(nullable = false)
    private String nome;
    private boolean emManutencao;
    private boolean restrito;

    protected Recurso() {}

    public Recurso(TipoRecurso tipo, String nome, boolean restrito) {
        this.tipo = tipo;
        this.nome = nome;
        this.restrito = restrito;
    }

    public Long getId() { return id; }
    public TipoRecurso getTipo() { return tipo; }
    public String getNome() { return nome; }
    public boolean isEmManutencao() { return emManutencao; }
    public void setEmManutencao(boolean emManutencao) { this.emManutencao = emManutencao; }
    public boolean isRestrito() { return restrito; }
    public void setNome(String nome) { this.nome = nome; }
    public void setRestrito(boolean restrito) { this.restrito = restrito; }
}
