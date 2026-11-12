package com.senac.recursos.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidacaoEmailProducaoConfigTest {
    @Test
    void bloqueiaProducaoSemEnvioDeEmail() {
        var config = new ValidacaoEmailProducaoConfig(false, "https://recursos.exemplo.br", "smtp.exemplo.br");
        assertThrows(IllegalStateException.class, config::validar);
    }

    @Test
    void bloqueiaProducaoComLinkHttp() {
        var config = new ValidacaoEmailProducaoConfig(true, "http://recursos.exemplo.br", "smtp.exemplo.br");
        assertThrows(IllegalStateException.class, config::validar);
    }

    @Test
    void bloqueiaProducaoSemServidorSmtp() {
        var config = new ValidacaoEmailProducaoConfig(true, "https://recursos.exemplo.br", "localhost");
        assertThrows(IllegalStateException.class, config::validar);
    }

    @Test
    void aceitaProducaoComSmtpELinkHttps() {
        var config = new ValidacaoEmailProducaoConfig(true, "https://recursos.exemplo.br", "smtp.exemplo.br");
        assertDoesNotThrow(config::validar);
    }
}
