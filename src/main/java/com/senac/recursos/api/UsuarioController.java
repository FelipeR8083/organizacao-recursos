package com.senac.recursos.api;

import com.senac.recursos.dominio.PerfilAcesso;
import com.senac.recursos.dominio.Usuario;
import com.senac.recursos.servico.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    public record NovoUsuario(@NotBlank String login, @NotBlank @Size(min = 12) String senha,
                              @NotNull PerfilAcesso perfil) {}
    public record UsuarioView(Long id, String login, PerfilAcesso perfil, boolean ativo) {
        static UsuarioView de(Usuario u) {
            return new UsuarioView(u.getId(), u.getUsername(), u.getPerfil(), u.isAtivo());
        }
    }

    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service = service; }

    @GetMapping
    public List<UsuarioView> listar() {
        return service.listar().stream().map(UsuarioView::de).toList();
    }

    @PostMapping
    public ResponseEntity<UsuarioView> criar(@Valid @RequestBody NovoUsuario dados) {
        Usuario criado = service.criar(dados.login(), dados.senha(), dados.perfil());
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioView.de(criado));
    }

    @PatchMapping("/{id}/ativo")
    public ResponseEntity<Map<String, String>> alterarAtivo(@PathVariable Long id,
                                                              @RequestBody Map<String, Boolean> dados) {
        Boolean ativo = dados.get("ativo");
        if (ativo == null) return ResponseEntity.badRequest().body(Map.of("erro", "Campo ativo é obrigatório"));
        service.alterarAtivo(id, ativo);
        return ResponseEntity.ok(Map.of("mensagem", "Situação do usuário atualizada"));
    }
}
