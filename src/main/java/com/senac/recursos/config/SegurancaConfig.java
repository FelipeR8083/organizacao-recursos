package com.senac.recursos.config;

import com.senac.recursos.repositorio.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableMethodSecurity
public class SegurancaConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return login -> usuarios.findByLoginIgnoreCase(login)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/auditoria-administrativa/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/relatorios/**", "/notificacoes/**").hasAnyRole("RESPONSAVEL", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/recursos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/recursos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/recursos/**", "/reservas/**")
                            .hasAnyRole("SOLICITANTE", "RESPONSAVEL", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/reservas/*/retirada", "/reservas/*/devolucao")
                            .hasAnyRole("RESPONSAVEL", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/reservas/**")
                            .hasAnyRole("SOLICITANTE", "RESPONSAVEL", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/reservas/**")
                            .hasAnyRole("SOLICITANTE", "RESPONSAVEL", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/reservas/**")
                            .hasAnyRole("SOLICITANTE", "RESPONSAVEL", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/reservas/**")
                            .hasAnyRole("RESPONSAVEL", "ADMINISTRADOR")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
