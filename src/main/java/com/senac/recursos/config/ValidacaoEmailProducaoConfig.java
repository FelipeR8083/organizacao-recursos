package com.senac.recursos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import jakarta.annotation.PostConstruct;

/**
 * Em producao, impede cadastros sem verificacao institucional ou com links inseguros.
 * Ative o perfil prod somente depois de configurar SMTP e APP_PUBLIC_URL.
 */
@Configuration
@Profile("prod")
public class ValidacaoEmailProducaoConfig {
    private final boolean envioHabilitado;
    private final String urlPublica;
    private final String smtpHost;

    public ValidacaoEmailProducaoConfig(
            @Value("${app.email.envio-habilitado:false}") boolean envioHabilitado,
            @Value("${app.email.url-publica:http://localhost:8080}") String urlPublica,
            @Value("${spring.mail.host:localhost}") String smtpHost) {
        this.envioHabilitado = envioHabilitado;
        this.urlPublica = urlPublica;
        this.smtpHost = smtpHost;
    }

    @PostConstruct
    public void validar() {
        if (!envioHabilitado) {
            throw new IllegalStateException("Em producao, EMAIL_ENVIO_HABILITADO deve ser true");
        }
        if (!urlPublica.matches("https://[^\\s/#?]+(?:/[^\\s#?]*)?")) {
            throw new IllegalStateException("Em producao, APP_PUBLIC_URL deve usar HTTPS");
        }
        if (smtpHost.isBlank() || smtpHost.equalsIgnoreCase("localhost")) {
            throw new IllegalStateException("Em producao, SMTP_HOST deve apontar para um servidor SMTP");
        }
    }
}
