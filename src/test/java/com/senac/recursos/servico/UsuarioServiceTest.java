package com.senac.recursos.servico;

import com.senac.recursos.dominio.PerfilAcesso;
import com.senac.recursos.dominio.Usuario;
import com.senac.recursos.repositorio.UsuarioRepository;
import org.junit.jupiter.api.Test;
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
}
