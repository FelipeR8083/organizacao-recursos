package com.senac.recursos.dominio;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_login", columnNames = "login"))
public class Usuario implements UserDetails {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String login;
    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private PerfilAcesso perfil;
    @Column(nullable = false)
    private boolean ativo = true;

    protected Usuario() {}

    public Usuario(String login, String senhaHash, PerfilAcesso perfil) {
        this.login = login;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
    }

    public Long getId() { return id; }
    public PerfilAcesso getPerfil() { return perfil; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }
    @Override public String getPassword() { return senhaHash; }
    @Override public String getUsername() { return login; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return ativo; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return ativo; }
}
