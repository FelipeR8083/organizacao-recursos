package com.senac.recursos.servico;

import com.senac.recursos.dominio.PerfilAcesso;
import com.senac.recursos.dominio.Usuario;
import com.senac.recursos.repositorio.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.util.stream.Stream;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final UsuarioService service = new UsuarioService(usuarios, encoder);

    @Test
    void criaUsuarioComSenhaCodificada() {
        when(usuarios.existsByLoginIgnoreCase("ana")).thenReturn(false);
        when(encoder.encode("senha-segura-123")).thenReturn("hash-bcrypt");
        when(usuarios.save(any(Usuario.class))).thenAnswer(call -> call.getArgument(0));

        Usuario criado = service.criar(" ana ", "senha-segura-123", PerfilAcesso.SOLICITANTE);

        assertEquals("ana", criado.getUsername());
        assertEquals("hash-bcrypt", criado.getPassword());
        assertEquals(PerfilAcesso.SOLICITANTE, criado.getPerfil());
        verify(encoder).encode("senha-segura-123");
    }

    @Test
    void rejeitaSenhaCurta() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar("ana", "curta", PerfilAcesso.SOLICITANTE));
        verifyNoInteractions(encoder, usuarios);
    }

    @Test
    void impedeDesativarPropriaConta() {
        Usuario admin = new Usuario("admin", "hash", PerfilAcesso.ADMINISTRADOR);
        when(usuarios.findById(1L)).thenReturn(Optional.of(admin));
        assertThrows(IllegalArgumentException.class, () -> service.alterarAtivo(1L, false, "admin"));
        assertTrue(admin.isAtivo());
    }

    @Test
    void impedeDesativarUltimoAdministradorAtivo() {
        Usuario admin = new Usuario("admin", "hash", PerfilAcesso.ADMINISTRADOR);
        when(usuarios.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarios.countByPerfilAndAtivo(PerfilAcesso.ADMINISTRADOR, true)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.alterarAtivo(1L, false, "outro-admin"));
        assertTrue(admin.isAtivo());
    }
    static Stream<Arguments> dadosInvalidos() {
        return Stream.of(
                Arguments.of(null, "senha-valida-123", PerfilAcesso.SOLICITANTE),
                Arguments.of("   ", "senha-valida-123", PerfilAcesso.SOLICITANTE),
                Arguments.of("ana", null, PerfilAcesso.SOLICITANTE),
                Arguments.of("ana", "curta", PerfilAcesso.SOLICITANTE),
                Arguments.of("ana", "senha-valida-123", null)
        );
    }

    @ParameterizedTest
    @MethodSource("dadosInvalidos")
    void rejeitaDadosInvalidosSemConsultarPersistencia(String login, String senha, PerfilAcesso perfil) {
        assertThrows(IllegalArgumentException.class, () -> service.criar(login, senha, perfil));
        verifyNoInteractions(encoder, usuarios);
    }

    @Test
    void rejeitaCadastroComLoginDuplicado() {
        when(usuarios.existsByLoginIgnoreCase("ana")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(" ana ", "senha-valida-123", PerfilAcesso.SOLICITANTE));
        verify(usuarios, never()).save(any());
        verifyNoInteractions(encoder);
    }

    @Test
    void rejeitaAlteracaoDeUsuarioInexistente() {
        when(usuarios.findById(77L)).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class,
                () -> service.alterarAtivo(77L, false, "admin"));
    }

    @Test
    void permiteDesativarSolicitanteSemAfetarRegraDeUltimoAdmin() {
        Usuario usuario = new Usuario("ana", "hash", PerfilAcesso.SOLICITANTE);
        when(usuarios.findById(3L)).thenReturn(Optional.of(usuario));
        service.alterarAtivo(3L, false, "admin");
        assertFalse(usuario.isAtivo());
        verify(usuarios, never()).countByPerfilAndAtivo(any(), anyBoolean());
    }

    @Test
    void permiteDesativarAdministradorQuandoExisteOutroAtivo() {
        Usuario usuario = new Usuario("admin-secundario", "hash", PerfilAcesso.ADMINISTRADOR);
        when(usuarios.findById(4L)).thenReturn(Optional.of(usuario));
        when(usuarios.countByPerfilAndAtivo(PerfilAcesso.ADMINISTRADOR, true)).thenReturn(2L);
        service.alterarAtivo(4L, false, "admin-principal");
        assertFalse(usuario.isAtivo());
    }

    @Test
    void permiteReativarUsuarioSemConsultarQuantidadeDeAdmins() {
        Usuario usuario = new Usuario("admin-secundario", "hash", PerfilAcesso.ADMINISTRADOR);
        usuario.setAtivo(false);
        when(usuarios.findById(4L)).thenReturn(Optional.of(usuario));
        service.alterarAtivo(4L, true, "admin-principal");
        assertTrue(usuario.isAtivo());
        verify(usuarios, never()).countByPerfilAndAtivo(any(), anyBoolean());
    }

    @Test
    void cadastroProfessorSenacFicaPendenteDeAprovacao() {
        when(encoder.encode("senha-segura-123")).thenReturn("hash");
        when(usuarios.save(any(Usuario.class))).thenAnswer(call -> call.getArgument(0));

        Usuario professor = service.solicitarCadastroProfessor(" ANA@SP.SENAC.BR ", "senha-segura-123");

        assertEquals("ana@sp.senac.br", professor.getUsername());
        assertEquals(PerfilAcesso.PROFESSOR, professor.getPerfil());
        assertFalse(professor.isAtivo());
        verify(usuarios).save(any(Usuario.class));
    }

    @Test
    void cadastroComEmailHabilitadoExigeVerificacaoEEnviaMensagem() {
        EmailConfirmacaoProfessorService email = mock(EmailConfirmacaoProfessorService.class);
        when(email.habilitado()).thenReturn(true);
        when(encoder.encode("senha-segura-123")).thenReturn("hash");
        when(usuarios.save(any(Usuario.class))).thenAnswer(call -> call.getArgument(0));
        UsuarioService comEmail = new UsuarioService(usuarios, encoder, null, email);

        Usuario professor = comEmail.solicitarCadastroProfessor("ana@sp.senac.br", "senha-segura-123");

        assertFalse(professor.isAtivo());
        assertTrue(professor.isVerificacaoEmailObrigatoria());
        assertNull(professor.getEmailVerificadoEm());
        verify(email).enviar(professor);
    }

    @Test
    void falhaNoEnvioInterrompeCadastroParaRollbackTransacional() {
        EmailConfirmacaoProfessorService email = mock(EmailConfirmacaoProfessorService.class);
        when(email.habilitado()).thenReturn(true);
        when(encoder.encode("senha-segura-123")).thenReturn("hash");
        when(usuarios.save(any(Usuario.class))).thenAnswer(call -> call.getArgument(0));
        doThrow(new IllegalStateException("SMTP indisponivel")).when(email).enviar(any());
        UsuarioService comEmail = new UsuarioService(usuarios, encoder, null, email);

        assertThrows(IllegalStateException.class,
                () -> comEmail.solicitarCadastroProfessor("ana@sp.senac.br", "senha-segura-123"));
    }

    @Test
    void rejeitaEmailExternoNoCadastroProfessor() {
        assertThrows(IllegalArgumentException.class,
                () -> service.solicitarCadastroProfessor("ana@gmail.com", "senha-segura-123"));
        verifyNoInteractions(usuarios, encoder);
    }

    @Test
    void rejeitaDominioQueApenasTerminaComSenacBr() {
        assertThrows(IllegalArgumentException.class,
                () -> service.solicitarCadastroProfessor("ana@falsosenac.br", "senha-segura-123"));
    }

    @Test
    void impedeCadastroProfessorDuplicado() {
        when(usuarios.existsByLoginIgnoreCase("ana@sp.senac.br")).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> service.solicitarCadastroProfessor("ana@sp.senac.br", "senha-segura-123"));
        verify(usuarios, never()).save(any());
    }

    static Stream<String> emailsInstitucionaisInvalidos() {
        return Stream.of(
                "ana@senac.br.atacante.com",
                "ana@falso-senac.br",
                "ana@.senac.br",
                "ana@-sp.senac.br",
                "ana@sp..senac.br",
                ".ana@sp.senac.br",
                "ana..silva@sp.senac.br",
                "ana.@sp.senac.br",
                "ana@@senac.br",
                "ana@senac.br outro@senac.br",
                "ana@",
                "ana@gmail.com"
        );
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.MethodSource("emailsInstitucionaisInvalidos")
    void rejeitaEmailsInstitucionaisForjadosSemGravar(String email) {
        assertThrows(IllegalArgumentException.class,
                () -> service.solicitarCadastroProfessor(email, "senha-segura-123"));
        verifyNoInteractions(usuarios, encoder);
    }

    @Test
    void cadastroAceitaParteLocalComCaracteresPermitidos() {
        when(encoder.encode("senha-segura-123")).thenReturn("hash");
        when(usuarios.save(any(Usuario.class))).thenAnswer(call -> call.getArgument(0));
        Usuario professor = service.solicitarCadastroProfessor(
                "ana.silva_2026+curso%turma-sp@unidade-1.senac.br", "senha-segura-123");
        assertEquals("ana.silva_2026+curso%turma-sp@unidade-1.senac.br",
                professor.getUsername());
        assertFalse(professor.isAtivo());
    }

    @Test
    void cadastroRejeitaEmailsLongosAntesDeValidarSegmentos() {
        assertThrows(IllegalArgumentException.class,
                () -> service.solicitarCadastroProfessor(
                        "a.".repeat(10000) + "a@sp.senac.br", "senha-segura-123"));
        verifyNoInteractions(usuarios, encoder);
    }

    @Test
    void rejeitaSenhaCurtaNoCadastroProfessorSemPersistir() {
        assertThrows(IllegalArgumentException.class,
                () -> service.solicitarCadastroProfessor("ana@sp.senac.br", "curta"));
        verify(usuarios, never()).save(any());
        verifyNoInteractions(encoder);
    }

    @Test
    void impedeAtivacaoDeProfessorSemConfirmacaoObrigatoriaDeEmail() {
        Usuario professor = new Usuario("ana@sp.senac.br", "hash", PerfilAcesso.PROFESSOR);
        professor.setAtivo(false);
        professor.setVerificacaoEmailObrigatoria(true);
        when(usuarios.findById(42L)).thenReturn(Optional.of(professor));

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> service.alterarAtivo(42L, true, "admin"));
        assertTrue(erro.getMessage().contains("e-mail institucional"));
        assertFalse(professor.isAtivo());
    }

    @Test
    void permiteAtivacaoDeProfessorComEmailConfirmado() {
        Usuario professor = new Usuario("ana@sp.senac.br", "hash", PerfilAcesso.PROFESSOR);
        professor.setAtivo(false);
        professor.setVerificacaoEmailObrigatoria(true);
        professor.setEmailVerificadoEm(java.time.Instant.now());
        when(usuarios.findById(43L)).thenReturn(Optional.of(professor));

        service.alterarAtivo(43L, true, "admin");
        assertTrue(professor.isAtivo());
    }

}
