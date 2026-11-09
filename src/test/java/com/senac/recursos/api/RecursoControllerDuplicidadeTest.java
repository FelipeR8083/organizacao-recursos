package com.senac.recursos.api;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.RecursoRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.NoSuchElementException;

class RecursoControllerDuplicidadeTest {
    private final RecursoRepository repository = mock(RecursoRepository.class);
    private final RecursoController controller = new RecursoController(repository);

    @Test void rejeitaCadastroComMesmoNomeTipoIgnorandoEspacosEMaiusculas() {
        when(repository.existeNomeNormalizado(TipoRecurso.SALA, "sala 101", null)).thenReturn(true);
        assertThrows(NomeRecursoDuplicadoException.class,
            () -> controller.criar(new RecursoController.Dados(TipoRecurso.SALA, "  SALA 101  ", false, false)));
        verify(repository, never()).save(any());
    }

    @Test void aceitaNomeIgualEmOutroTipo() {
        when(repository.existeNomeNormalizado(TipoRecurso.MATERIAL, "sala 101", null)).thenReturn(false);
        when(repository.save(any(Recurso.class))).thenAnswer(i -> i.getArgument(0));
        assertEquals(201, controller.criar(new RecursoController.Dados(TipoRecurso.MATERIAL, "Sala 101", false, false)).getStatusCode().value());
    }

    @Test void rejeitaRenomeacaoParaNomeJaUsado() {
        Recurso existente = new Recurso(TipoRecurso.SALA, "Sala 102", false);
        when(repository.findById(2L)).thenReturn(Optional.of(existente));
        when(repository.existeNomeNormalizado(TipoRecurso.SALA, "sala 101", 2L)).thenReturn(true);
        assertThrows(NomeRecursoDuplicadoException.class,
            () -> controller.atualizar(2L, new RecursoController.Alteracao("Sala 101", false, false)));
    }

    @Test void atualizaNomeRestricaoEManutencaoDoRecurso() {
        Recurso existente = new Recurso(TipoRecurso.SALA, "Sala antiga", false);
        when(repository.findById(7L)).thenReturn(Optional.of(existente));
        when(repository.existeNomeNormalizado(TipoRecurso.SALA, "sala nova", 7L)).thenReturn(false);
        when(repository.save(existente)).thenReturn(existente);

        Recurso atualizado = controller.atualizar(7L,
                new RecursoController.Alteracao("  Sala Nova  ", true, true));

        assertEquals("Sala Nova", atualizado.getNome());
        assertTrue(atualizado.isRestrito());
        assertTrue(atualizado.isEmManutencao());
        verify(repository).save(existente);
    }

    @Test void atualizarRecursoInexistenteRetornaExcecaoDeNaoEncontrado() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> controller.atualizar(999L,
                        new RecursoController.Alteracao("Sala Nova", false, false)));
        verify(repository, never()).save(any());
    }

}
