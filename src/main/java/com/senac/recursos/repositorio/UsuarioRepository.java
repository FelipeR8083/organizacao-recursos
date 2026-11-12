package com.senac.recursos.repositorio;

import com.senac.recursos.dominio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByLoginIgnoreCase(String login);
    boolean existsByLoginIgnoreCase(String login);
    long countByPerfilAndAtivo(com.senac.recursos.dominio.PerfilAcesso perfil, boolean ativo);
}
