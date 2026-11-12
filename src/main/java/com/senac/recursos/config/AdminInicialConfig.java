package com.senac.recursos.config;

import com.senac.recursos.dominio.PerfilAcesso;
import com.senac.recursos.repositorio.UsuarioRepository;
import com.senac.recursos.servico.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminInicialConfig {
    @Bean
    ApplicationRunner criarAdministradorInicial(
            UsuarioRepository usuarios,
            UsuarioService service,
            @Value("${APP_ADMIN_USERNAME:}") String login,
            @Value("${APP_ADMIN_PASSWORD:}") String senha) {
        return args -> {
            if (login.isBlank() && senha.isBlank()) return;
            if (login.isBlank() || senha.length() < 12) {
                throw new IllegalStateException("Configure APP_ADMIN_USERNAME e APP_ADMIN_PASSWORD com pelo menos 12 caracteres");
            }
            if (!usuarios.existsByLoginIgnoreCase(login.trim())) {
                service.criar(login, senha, PerfilAcesso.ADMINISTRADOR);
            }
        };
    }
}
