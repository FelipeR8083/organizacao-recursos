package com.senac.recursos.servico;

import com.senac.recursos.dominio.PerfilAcesso;
import com.senac.recursos.dominio.Usuario;
import com.senac.recursos.repositorio.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AuditoriaAdministrativaService auditoriaAdministrativa;
    private final EmailConfirmacaoProfessorService emailConfirmacao;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this(usuarios, encoder, null, null);
    }

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder,
                          AuditoriaAdministrativaService auditoriaAdministrativa) {
        this(usuarios, encoder, auditoriaAdministrativa, null);
    }

    @Autowired
    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder,
                          AuditoriaAdministrativaService auditoriaAdministrativa,
                          EmailConfirmacaoProfessorService emailConfirmacao) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.auditoriaAdministrativa = auditoriaAdministrativa;
        this.emailConfirmacao = emailConfirmacao;
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
        Usuario criado = usuarios.save(new Usuario(nome, encoder.encode(senha), perfil));
        if (auditoriaAdministrativa != null) {
            auditoriaAdministrativa.registrar("USUARIO", criado.getId(), "CRIACAO",
                    "login=" + criado.getUsername() + ", perfil=" + criado.getPerfil());
        }
        return criado;
    }

    /** Cadastro público: somente professor Senac, sempre pendente de aprovação. */
    @Transactional
    public Usuario solicitarCadastroProfessor(String email, String senha) {
        String normalizado = email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
        int arroba = normalizado.lastIndexOf('@');
        String dominio = arroba < 0 ? "" : normalizado.substring(arroba + 1);
        if (arroba <= 0 || normalizado.indexOf('@') != arroba
                || !normalizado.substring(0, arroba).matches("[a-z0-9_+%-]+(?:\\.[a-z0-9_+%-]+)*")
                || !(dominio.equals("senac.br") || dominio.endsWith(".senac.br"))
                || !dominio.matches("[a-z0-9]+(?:[a-z0-9-]*[a-z0-9])?(?:\\.[a-z0-9]+(?:[a-z0-9-]*[a-z0-9])?)*")
                || normalizado.length() > 80 || normalizado.contains(" ")) {
            throw new IllegalArgumentException("Informe um e-mail institucional do Senac válido");
        }
        if (senha == null || senha.length() < 12) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 12 caracteres");
        }
        if (usuarios.existsByLoginIgnoreCase(normalizado)) {
            throw new IllegalArgumentException("Já existe usuário com este e-mail");
        }
        Usuario professor = new Usuario(normalizado, encoder.encode(senha), PerfilAcesso.PROFESSOR);
        professor.setAtivo(false);
        boolean verificarEmail = emailConfirmacao != null && emailConfirmacao.habilitado();
        professor.setVerificacaoEmailObrigatoria(verificarEmail);
        Usuario salvo = usuarios.save(professor);
        if (verificarEmail) {
            emailConfirmacao.enviar(salvo);
        }
        if (auditoriaAdministrativa != null) {
            auditoriaAdministrativa.registrar("USUARIO", salvo.getId(), "SOLICITACAO_CADASTRO",
                    "login=" + salvo.getUsername() + ", perfil=PROFESSOR, ativo=false");
        }
        return salvo;
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
        if (ativo && usuario.getPerfil() == PerfilAcesso.PROFESSOR
                && usuario.isVerificacaoEmailObrigatoria()
                && usuario.getEmailVerificadoEm() == null) {
            throw new IllegalStateException("Confirme o e-mail institucional antes da aprovação administrativa");
        }
        boolean anterior = usuario.isAtivo();
        usuario.setAtivo(ativo);
        if (auditoriaAdministrativa != null && anterior != ativo) {
            auditoriaAdministrativa.registrar("USUARIO", usuario.getId(), "ATIVACAO",
                    "login=" + usuario.getUsername() + ", ativo=" + anterior + " -> " + ativo);
        }
    }
}
