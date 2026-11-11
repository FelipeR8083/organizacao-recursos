package com.senac.recursos.dominio;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "verificacao_email_professor")
public class VerificacaoEmailProfessor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expira_em", nullable = false)
    private Instant expiraEm;

    @Column(name = "consumido_em")
    private Instant consumidoEm;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected VerificacaoEmailProfessor() {}

    public VerificacaoEmailProfessor(Usuario usuario, String tokenHash, Instant criadoEm, Instant expiraEm) {
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.criadoEm = criadoEm;
        this.expiraEm = expiraEm;
    }

    public Usuario getUsuario() { return usuario; }
    public Instant getExpiraEm() { return expiraEm; }
    public Instant getConsumidoEm() { return consumidoEm; }
    public void consumir(Instant instante) { this.consumidoEm = instante; }
}
