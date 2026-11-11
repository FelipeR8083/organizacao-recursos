package com.senac.recursos.api;

import com.senac.recursos.servico.VerificacaoEmailProfessorService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfirmacaoEmailProfessorControllerTest {
    private final VerificacaoEmailProfessorService verificacao = mock(VerificacaoEmailProfessorService.class);
    private final ConfirmacaoEmailProfessorController controller =
            new ConfirmacaoEmailProfessorController(verificacao);

    @Test
    void confirmaTokenRecebidoESolicitaAguardarAdministrador() {
        var resposta = controller.confirmar(Map.of("token", "abc123"));
        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertTrue(resposta.getBody().get("mensagem").contains("administrador"));
        verify(verificacao).confirmar("abc123");
    }

    @Test
    void tokenAusenteEhEncaminhadoParaValidacao() {
        controller.confirmar(Map.of());
        verify(verificacao).confirmar(null);
    }

    @Test
    void erroDeTokenRetornaRespostaSemVazarDetalhes() {
        var resposta = controller.tokenInvalido(new IllegalArgumentException("detalhe interno"));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, resposta.getStatusCode());
        assertTrue(resposta.getBody().get("erro").contains("invalido"));
        assertFalse(resposta.getBody().get("erro").contains("detalhe interno"));
    }
}
