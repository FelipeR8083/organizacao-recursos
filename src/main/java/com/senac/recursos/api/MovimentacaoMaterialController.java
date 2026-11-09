package com.senac.recursos.api;

import com.senac.recursos.dominio.MovimentacaoMaterial;
import com.senac.recursos.servico.MovimentacaoMaterialService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservas")
public class MovimentacaoMaterialController {
    private final MovimentacaoMaterialService service;
    public MovimentacaoMaterialController(MovimentacaoMaterialService service) { this.service = service; }

    @PostMapping("/{id}/retirada")
    public MovimentacaoMaterial retirar(@PathVariable Long id, Authentication autenticacao) {
        return service.retirar(id, autenticacao.getName());
    }

    @PostMapping("/{id}/devolucao")
    public MovimentacaoMaterial devolver(@PathVariable Long id, Authentication autenticacao) {
        return service.devolver(id, autenticacao.getName());
    }
}
