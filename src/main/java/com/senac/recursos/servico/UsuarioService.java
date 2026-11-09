package com.senac.recursos.servico;

import com.senac.recursos.dominio.PerfilAcesso;
import com.senac.recursos.dominio.Usuario;
import com.senac.recursos.repositorio.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional
    public Usuario criar(String login, String senha, PerfilAcesso perfil) {
        String nome = login == null ? "" : login.trim();
        if (nome.isBlank() || senha == null || senha.length() < 12 || perfil == null) {
            throw new IllegalArgumentException("Informe login, perfil e senha com pelo menos 12 caracteres");
        }
        if (usuarios.existsByLoginIgnoreCase(nome)) {
            throw new IllegalArgumentException("Já existe usuário com este login");
        }
        return usuarios.save(new Usuario(nome, encoder.encode(senha), perfil));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() { return usuarios.findAll(); }

    @Transactional
    public void alterarAtivo(Long id, boolean ativo, String ator) {
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
        if (!ativo && usuario.getUsername().equalsIgnoreCase(ator)) {
            throw new IllegalArgumentException("Não é permitido desativar o próprio usuário");
        }
        if (!ativo && usuario.getPerfil() == PerfilAcesso.ADMINISTRADOR
                && usuarios.countByPerfilAndAtivo(PerfilAcesso.ADMINISTRADOR, true) <= 1) {
            throw new IllegalStateException("O último administrador ativo não pode ser desativado");
        }
        usuario.setAtivo(ativo);
    }
}
