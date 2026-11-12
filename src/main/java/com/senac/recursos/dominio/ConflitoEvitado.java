package com.senac.recursos.dominio;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "conflitos_evitados", indexes = @Index(name = "idx_conflito_ocorrido_em", columnList = "ocorrido_em"))
public class ConflitoEvitado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "recurso_id", nullable = false)
    private Long recursoId;
    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;
    @Column(name = "inicio_solicitado", nullable = false)
    private Instant inicioSolicitado;
    @Column(name = "fim_solicitado", nullable = false)
    private Instant fimSolicitado;
    @Column(nullable = false, length = 120)
    private String motivo;

    protected ConflitoEvitado() {}
    public ConflitoEvitado(Long recursoId, Instant ocorridoEm, Instant inicioSolicitado,
                           Instant fimSolicitado, String motivo) {
        this.recursoId = recursoId;
        this.ocorridoEm = ocorridoEm;
        this.inicioSolicitado = inicioSolicitado;
        this.fimSolicitado = fimSolicitado;
        this.motivo = motivo;
    }
    public Long getId() { return id; }
    public Long getRecursoId() { return recursoId; }
    public Instant getOcorridoEm() { return ocorridoEm; }
    public Instant getInicioSolicitado() { return inicioSolicitado; }
    public Instant getFimSolicitado() { return fimSolicitado; }
    public String getMotivo() { return motivo; }
}
