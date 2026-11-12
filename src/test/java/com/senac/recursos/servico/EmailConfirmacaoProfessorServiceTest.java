package com.senac.recursos.servico;

import com.senac.recursos.dominio.PerfilAcesso;
import com.senac.recursos.dominio.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailConfirmacaoProfessorServiceTest {
    private final JavaMailSender mail = mock(JavaMailSender.class);
    private final VerificacaoEmailProfessorService verificacao = mock(VerificacaoEmailProfessorService.class);
    private final Usuario professor = new Usuario("ana@sp.senac.br", "hash", PerfilAcesso.PROFESSOR);

    @Test
    void enviaLinkComTokenSomenteParaEmailCadastrado() {
        when(verificacao.emitir(professor)).thenReturn("a".repeat(64));
        EmailConfirmacaoProfessorService service = new EmailConfirmacaoProfessorService(
                mail, verificacao, "contato@senac.br", "https://exemplo.test/", true);
        service.enviar(professor);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail).send(captor.capture());
        assertArrayEquals(new String[]{"ana@sp.senac.br"}, captor.getValue().getTo());
        assertEquals("contato@senac.br", captor.getValue().getFrom());
        assertTrue(captor.getValue().getText().contains(
                "https://exemplo.test/confirmar-email.html#token=" + "a".repeat(64)));
    }

    @Test
    void removeVariasBarrasDaUrlBaseSemAlterarOToken() {
        when(verificacao.emitir(professor)).thenReturn("b".repeat(64));
        EmailConfirmacaoProfessorService service = new EmailConfirmacaoProfessorService(
                mail, verificacao, "contato@senac.br", "https://exemplo.test/////", true);
        service.enviar(professor);
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail).send(captor.capture());
        assertTrue(captor.getValue().getText().contains(
                "https://exemplo.test/confirmar-email.html#token=" + "b".repeat(64)));
    }

    @Test
    void naoGeraTokenNemEnviaQuandoDesabilitado() {
        EmailConfirmacaoProfessorService service = new EmailConfirmacaoProfessorService(
                mail, verificacao, "contato@senac.br", "https://exemplo.test", false);
        assertFalse(service.habilitado());
        assertThrows(IllegalStateException.class, () -> service.enviar(professor));
        verifyNoInteractions(mail, verificacao);
    }
}
