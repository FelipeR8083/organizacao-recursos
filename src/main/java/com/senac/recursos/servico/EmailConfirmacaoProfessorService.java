package com.senac.recursos.servico;

import com.senac.recursos.dominio.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Envia o segredo exclusivamente para o endereco institucional cadastrado. */
@Service
public class EmailConfirmacaoProfessorService {
    private final JavaMailSender remetente;
    private final VerificacaoEmailProfessorService verificacao;
    private final String origem;
    private final String urlPublica;
    private final boolean habilitado;

    public EmailConfirmacaoProfessorService(JavaMailSender remetente,
            VerificacaoEmailProfessorService verificacao,
            @Value("${app.email.remetente}") String origem,
            @Value("${app.email.url-publica}") String urlPublica,
            @Value("${app.email.envio-habilitado:false}") boolean habilitado) {
        this.remetente = remetente;
        this.verificacao = verificacao;
        this.origem = origem;
        this.urlPublica = urlPublica;
        this.habilitado = habilitado;
    }

    public boolean habilitado() {
        return habilitado;
    }

    public void enviar(Usuario professor) {
        if (!habilitado) {
            throw new IllegalStateException("Envio de verificacao de e-mail desabilitado");
        }
        String token = verificacao.emitir(professor);
        // Remove barras finais sem regex para manter custo linear mesmo com entradas extensas.
        String baseUrl = urlPublica;
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String url = baseUrl + "/confirmar-email.html#token=" + token;
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(origem);
        mensagem.setTo(professor.getUsername());
        mensagem.setSubject("Confirme seu e-mail institucional");
        mensagem.setText("Para confirmar seu e-mail institucional, acesse o link abaixo em ate 24 horas:\n"
                + url + "\n\nApos confirmar, aguarde a aprovacao de um administrador.");
        remetente.send(mensagem);
    }
}
