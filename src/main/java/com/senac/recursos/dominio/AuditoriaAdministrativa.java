package com.senac.recursos.dominio;

import jakarta.persistence.*;
import java.time.Instant;

/** Eventos administrativos sem dados sensíveis como senhas ou hashes. */
@Entity
@org.hibernate.annotations.Immutable
@Table(name = "auditoria_administrativa", indexes = {
        @Index(name = "idx_auditoria_admin_data", columnList = "ocorrido_em")
})
public class AuditoriaAdministrativa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30, updatable = false)
    private String entidade;
    @Column(name = "referencia_id", nullable = false, updatable = false)
    private Long referenciaId;
    @Column(nullable = false, length = 30, updatable = false)
    private String acao;
    @Column(nullable = false, length = 80, updatable = false)
    private String ator;
    @Column(length = 500, updatable = false)
    private String detalhes;
    @Column(name = "ocorrido_em", nullable = false, updatable = false)
    private Instant ocorridoEm;

    protected AuditoriaAdministrativa() {}
    public AuditoriaAdministrativa(String entidade, Long referenciaId, String acao,
                                  String ator, String detalhes, Instant ocorridoEm) {
        this.entidade = entidade;
        this.referenciaId = referenciaId;
        this.acao = acao;
        this.ator = ator;
        this.detalhes = detalhes;
        this.ocorridoEm = ocorridoEm;
    }
    public Long getId() { return id; }
    public String getEntidade() { return entidade; }
    public Long getReferenciaId() { return referenciaId; }
    public String getAcao() { return acao; }
    public String getAtor() { return ator; }
    public String getDetalhes() { return detalhes; }
    public Instant getOcorridoEm() { return ocorridoEm; }
}
