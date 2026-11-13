package com.senac.recursos.config;

import com.senac.recursos.dominio.Usuario;
import com.senac.recursos.repositorio.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Revalida a situação da conta em cada requisição, inclusive em sessões já abertas. */
public class UsuarioAtivoFilter extends OncePerRequestFilter {
    private final UsuarioRepository usuarios;

    public UsuarioAtivoFilter(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.isAuthenticated()
                && autenticacao.getPrincipal() instanceof Usuario usuario) {
            Long id = usuario.getId();
            boolean ativo = id != null && usuarios.findById(id).map(Usuario::isAtivo).orElse(false);
            if (!ativo) {
                SecurityContextHolder.clearContext();
                HttpSession sessao = request.getSession(false);
                if (sessao != null) {
                    sessao.invalidate();
                }
                if ("GET".equals(request.getMethod())
                        && ("/".equals(request.getServletPath()) || "/index.html".equals(request.getServletPath()))) {
                    response.sendRedirect(request.getContextPath() + "/login.html?desativado");
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                }
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
