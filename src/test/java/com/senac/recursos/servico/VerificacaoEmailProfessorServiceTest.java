package com.senac.recursos.servico;

import com.senac.recursos.dominio.PerfilAcesso;
import com.senac.recursos.dominio.Usuario;
import com.senac.recursos.dominio.VerificacaoEmailProfessor;
import com.senac.recursos.repositorio.VerificacaoEmailProfessorRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.time.Instant;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VerificacaoEmailProfessorServiceTest {
    private final VerificacaoEmailProfessorRepository repositorio = mock(VerificacaoEmailProfessorRepository.class);
    private final VerificacaoEmailProfessorService service = new VerificacaoEmailProfessorService(repositorio);

    private Usuario pendente() {
        Usuario usuario = new Usuario("ana@sp.senac.br", "hash", PerfilAcesso.PROFESSOR);
        usuario.setAtivo(false);
        usuario.setVerificacaoEmailObrigatoria(true);
        return usuario;
    }

    @Test
    void emiteTokenAleatorioComPrazoEArmazenaSomenteHash() {
        Usuario usuario = pendente();
        String token = service.emitir(usuario);
        assertTrue(token.matches("[0-9a-f]{64}"));
        ArgumentCaptor<VerificacaoEmailProfessor> captor = ArgumentCaptor.forClass(VerificacaoEmailProfessor.class);
        verify(repositorio).save(captor.capture());
        assertEquals(usuario, captor.getValue().getUsuario());
        assertTrue(captor.getValue().getExpiraEm().isAfter(Instant.now().plusSeconds(23 * 3600)));
        verify(repositorio, never()).findByTokenHash(token);
    }

    @Test
    void naoEmiteTokenQuandoVerificacaoNaoExigida() {
        Usuario usuario = new Usuario("ana@sp.senac.br", "hash", PerfilAcesso.PROFESSOR);
        assertThrows(IllegalStateException.class, () -> service.emitir(usuario));
        verifyNoInteractions(repositorio);
    }

    @Test
    void naoEmiteTokenQuandoEmailJaConfirmado() {
        Usuario usuario = pendente();
        usuario.setEmailVerificadoEm(Instant.now());
        assertThrows(IllegalStateException.class, () -> service.emitir(usuario));
        verifyNoInteractions(repositorio);
    }

    @Test
    void confirmaTokenValidoEImpedeSegundoUso() {
        Usuario usuario = pendente();
        String token = service.emitir(usuario);
        ArgumentCaptor<VerificacaoEmailProfessor> captor = ArgumentCaptor.forClass(VerificacaoEmailProfessor.class);
        verify(repositorio).save(captor.capture());
        VerificacaoEmailProfessor registro = captor.getValue();
        when(repositorio.findByTokenHash(any())).thenReturn(Optional.of(registro));

        service.confirmar(token);
        assertNotNull(usuario.getEmailVerificadoEm());
        assertNotNull(registro.getConsumidoEm());
        assertThrows(IllegalArgumentException.class, () -> service.confirmar(token));
    }

    @Test
    void rejeitaTokenInvalidoOuDesconhecido() {
        assertThrows(IllegalArgumentException.class, () -> service.confirmar("curto"));
        assertThrows(IllegalArgumentException.class, () -> service.confirmar(null));
        assertThrows(IllegalArgumentException.class, () -> service.confirmar("a".repeat(64)));
    }

    @Test
    void rejeitaTokenExpirado() {
        Usuario usuario = pendente();
        VerificacaoEmailProfessor registro = new VerificacaoEmailProfessor(usuario, "hash",
                Instant.now().minusSeconds(90000), Instant.now().minusSeconds(3600));
        when(repositorio.findByTokenHash(any())).thenReturn(Optional.of(registro));
        assertThrows(IllegalArgumentException.class, () -> service.confirmar("a".repeat(64)));
        assertNull(usuario.getEmailVerificadoEm());
    }

    @Test
    void rejeitaConfirmacaoQuandoEmailJaConfirmado() {
        Usuario usuario = pendente();
        usuario.setEmailVerificadoEm(Instant.now());
        VerificacaoEmailProfessor registro = new VerificacaoEmailProfessor(usuario, "hash",
                Instant.now(), Instant.now().plusSeconds(3600));
        when(repositorio.findByTokenHash(any())).thenReturn(Optional.of(registro));
        assertThrows(IllegalArgumentException.class, () -> service.confirmar("a".repeat(64)));
    }
}
