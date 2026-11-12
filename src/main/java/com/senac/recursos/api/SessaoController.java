package com.senac.recursos.api;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/** Identidade ativa para que a interface exiba apenas ações permitidas ao perfil. */
@RestController
public class SessaoController {
    @GetMapping("/sessao")
    public Map<String, String> atual(Authentication autenticacao) {
        String perfil = autenticacao.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(role -> role.startsWith("ROLE_"))
                .map(role -> role.substring("ROLE_".length()))
                .findFirst()
                .orElse("SEM_PERFIL");
        return Map.of("login", autenticacao.getName(), "perfil", perfil);
    }
}
