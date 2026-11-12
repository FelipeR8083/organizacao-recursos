package com.senac.recursos.dominio;

import jakarta.persistence.*;

@Entity
@Table(indexes = {
        @Index(name = "idx_recurso_tipo_nome", columnList = "tipo, nome"),
        @Index(name = "idx_recurso_localizacao", columnList = "localizacao")
})
public class Recurso {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TipoRecurso tipo;
    @Column(nullable = false)
    private String nome;
    private boolean emManutencao;
    private boolean restrito;
    private Integer capacidade;
    @Column(length = 160)
    private String localizacao;
    @Column(length = 500)
    private String competencias;

    protected Recurso() {}

    public Recurso(TipoRecurso tipo, String nome, boolean restrito) {
        this.tipo = tipo;
        this.nome = nome;
        this.restrito = restrito;
    }

    public void atualizarDetalhes(Integer capacidade, String localizacao, String competencias) {
        if (capacidade != null && capacidade < 1) {
            throw new IllegalArgumentException("Capacidade deve ser maior que zero");
        }
        this.capacidade = capacidade;
        this.localizacao = normalizar(localizacao);
        this.competencias = normalizar(competencias);
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public Long getId() { return id; }
    public TipoRecurso getTipo() { return tipo; }
    public String getNome() { return nome; }
    public boolean isEmManutencao() { return emManutencao; }
    public void setEmManutencao(boolean emManutencao) { this.emManutencao = emManutencao; }
    public boolean isRestrito() { return restrito; }
    public void setNome(String nome) { this.nome = nome; }
    public void setRestrito(boolean restrito) { this.restrito = restrito; }
    public Integer getCapacidade() { return capacidade; }
    public String getLocalizacao() { return localizacao; }
    public String getCompetencias() { return competencias; }
}
