package com.senac.recursos.config;

import com.senac.recursos.repositorio.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
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
                .csrf(csrf -> {
                    if (!producao) {
                        csrf.disable();
                    } else {
                        csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                                .ignoringRequestMatchers("/login", "/cadastro-professor",
                                        "/confirmacao-email-professor");
                    }
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        request -> {
                            String path = request.getServletPath();
                            return path.startsWith("/recursos") || path.startsWith("/reservas")
                                    || path.startsWith("/usuarios") || path.startsWith("/relatorios")
                                    || path.startsWith("/notificacoes") || path.startsWith("/sessao")
                                    || path.startsWith("/auditoria-administrativa");
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login.html", "/cadastro.html", "/confirmar-email.html", "/login", "/css/**", "/favicon.ico",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/cadastro-professor").permitAll()
                        .requestMatchers(HttpMethod.POST, "/confirmacao-email-professor").permitAll()
                        .requestMatchers("/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/auditoria-administrativa/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/relatorios/**", "/notificacoes/**").hasAnyRole("RESPONSAVEL", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/recursos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/recursos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/recursos/**", "/reservas/**")
                            .hasAnyRole("SOLICITANTE", "PROFESSOR", "RESPONSAVEL", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/reservas/*/retirada", "/reservas/*/devolucao")
                            .hasAnyRole("RESPONSAVEL", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/reservas/**")
                            .hasAnyRole("SOLICITANTE", "PROFESSOR", "RESPONSAVEL", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/reservas/**")
                            .hasAnyRole("SOLICITANTE", "PROFESSOR", "RESPONSAVEL", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/reservas/**")
                            .hasAnyRole("SOLICITANTE", "PROFESSOR", "RESPONSAVEL", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/reservas/**")
                            .hasAnyRole("RESPONSAVEL", "ADMINISTRADOR")
                        .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login.html").loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true).failureUrl("/login.html?erro")
                        .permitAll())
                .logout(logout -> logout.logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?saiu").invalidateHttpSession(true)
                        .clearAuthentication(true).deleteCookies("JSESSIONID"))
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
