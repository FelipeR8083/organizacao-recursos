package com.senac.recursos.api;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.RecursoRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;

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
}
