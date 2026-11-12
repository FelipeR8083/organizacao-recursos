package com.senac.recursos.api;

import com.senac.recursos.servico.VerificacaoEmailProfessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/confirmacao-email-professor")
public class ConfirmacaoEmailProfessorController {
    private final VerificacaoEmailProfessorService verificacao;

    public ConfirmacaoEmailProfessorController(VerificacaoEmailProfessorService verificacao) {
        this.verificacao = verificacao;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> confirmar(@RequestBody Map<String, String> dados) {
        verificacao.confirmar(dados.get("token"));
        return ResponseEntity.ok(Map.of("mensagem",
                "E-mail confirmado. Aguarde a aprovacao do administrador para acessar o sistema."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> tokenInvalido(IllegalArgumentException erro) {
        return ResponseEntity.unprocessableEntity()
                .body(Map.of("erro", "Link de confirmacao invalido, expirado ou ja utilizado."));
    }
}
