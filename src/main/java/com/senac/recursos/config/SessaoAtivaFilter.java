package com.senac.recursos.config;

import com.senac.recursos.dominio.Usuario;
import com.senac.recursos.repositorio.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Revalida no banco a situacao da conta em cada requisicao autenticada.
 * Revoga sessoes abertas anteriormente assim que o administrador desativa a conta.
 */
public class SessaoAtivaFilter extends OncePerRequestFilter {
    private final UsuarioRepository usuarios;

    public SessaoAtivaFilter(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.isAuthenticated()
                && autenticacao.getPrincipal() instanceof Usuario usuario) {
            boolean ativo = usuario.getId() != null && usuarios.findById(usuario.getId())
                    .filter(atual -> atual.isAtivo()
                            && atual.getUsername().equalsIgnoreCase(usuario.getUsername()))
                    .isPresent();
            if (!ativo) {
                HttpSession sessao = request.getSession(false);
                if (sessao != null) sessao.invalidate();
                SecurityContextHolder.clearContext();

                String caminho = request.getRequestURI().substring(request.getContextPath().length());
                if ("GET".equals(request.getMethod())
                        && ("/".equals(caminho) || "/index.html".equals(caminho)
                            || "/login.html".equals(caminho))) {
                    response.sendRedirect(request.getContextPath() + "/login.html?desativado");
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"erro\":\"Sua conta foi desativada pelo administrador.\",\"contaDesativada\":true}");
                }
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
