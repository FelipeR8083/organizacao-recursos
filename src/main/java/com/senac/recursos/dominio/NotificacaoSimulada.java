package com.senac.recursos.dominio;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notificacoes_simuladas", indexes = @Index(name = "idx_notificacao_destinatario_data", columnList = "destinatario, ocorrido_em"))
public class NotificacaoSimulada {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String destinatario;
    @Column(nullable = false, length = 100)
    private String assunto;
    @Column(nullable = false, length = 500)
    private String mensagem;
    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;
    @Column(nullable = false, length = 20)
    private String canal = "SIMULADO";

    protected NotificacaoSimulada() {}
    public NotificacaoSimulada(String destinatario, String assunto, String mensagem, Instant ocorridoEm) {
        this.destinatario = destinatario;
        this.assunto = assunto;
        this.mensagem = mensagem;
        this.ocorridoEm = ocorridoEm;
    }
    public Long getId() { return id; }
    public String getDestinatario() { return destinatario; }
    public String getAssunto() { return assunto; }
    public String getMensagem() { return mensagem; }
    public Instant getOcorridoEm() { return ocorridoEm; }
    public String getCanal() { return canal; }
}
