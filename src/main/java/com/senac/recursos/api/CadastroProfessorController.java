package com.senac.recursos.api;

import com.senac.recursos.servico.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/cadastro-professor")
public class CadastroProfessorController {
    public record Pedido(@NotBlank @Email String email,
                         @NotBlank @Size(min = 12) String senha) {}

    private final UsuarioService usuarios;
    private final com.senac.recursos.servico.EmailConfirmacaoProfessorService emailConfirmacao;
    public CadastroProfessorController(UsuarioService usuarios,
            com.senac.recursos.servico.EmailConfirmacaoProfessorService emailConfirmacao) {
        this.usuarios = usuarios;
        this.emailConfirmacao = emailConfirmacao;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> cadastrar(@Valid @RequestBody Pedido pedido) {
        usuarios.solicitarCadastroProfessor(pedido.email(), pedido.senha());
        String mensagem = emailConfirmacao.habilitado()
                ? "Cadastro recebido. Confirme o e-mail institucional enviado e aguarde a aprovação do administrador."
                : "Cadastro recebido. Aguarde a aprovação do administrador.";
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensagem", mensagem));
    }
}
