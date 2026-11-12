package com.senac.recursos.api;

import com.senac.recursos.dominio.MovimentacaoMaterial;
import com.senac.recursos.servico.MovimentacaoMaterialService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/reservas")
public class MovimentacaoMaterialController {
    private final MovimentacaoMaterialService service;
    public MovimentacaoMaterialController(MovimentacaoMaterialService service) { this.service = service; }

    @GetMapping("/movimentacoes")
    @PreAuthorize("hasAnyRole('RESPONSAVEL', 'ADMINISTRADOR')")
    public List<MovimentacaoMaterial> listar() {
        return service.listar();
    }

    @PostMapping("/{id}/materiais/{materialId}/retirada")
    public MovimentacaoMaterial retirarMaterial(@PathVariable Long id, @PathVariable Long materialId,
                                                 Authentication autenticacao) {
        return service.retirar(id, materialId, autenticacao.getName());
    }

    @PostMapping("/{id}/materiais/{materialId}/devolucao")
    public MovimentacaoMaterial devolverMaterial(@PathVariable Long id, @PathVariable Long materialId,
                                                  Authentication autenticacao) {
        return service.devolver(id, materialId, autenticacao.getName());
    }

    @PostMapping("/{id}/retirada")
    public MovimentacaoMaterial retirar(@PathVariable Long id, Authentication autenticacao) {
        return service.retirar(id, autenticacao.getName());
    }

    @PostMapping("/{id}/devolucao")
    public MovimentacaoMaterial devolver(@PathVariable Long id, Authentication autenticacao) {
        return service.devolver(id, autenticacao.getName());
    }
}
