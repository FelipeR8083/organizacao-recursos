package com.senac.recursos.api;

import org.springframework.lang.Nullable;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * Informa ao cliente web se deve enviar um token CSRF.
 * Em ambientes onde o filtro CSRF esta desativado, nenhum token e fornecido.
 */
@RestController
public class CsrfController {
    @GetMapping("/csrf")
    public Map<String, Object> token(@Nullable CsrfToken token) {
        if (token == null) {
            return Map.of("enabled", false);
        }
        return Map.of("enabled", true,
                "headerName", token.getHeaderName(),
                "parameterName", token.getParameterName(),
                "token", token.getToken());
    }
}
