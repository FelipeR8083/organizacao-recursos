package com.senac.recursos.api;

import com.senac.recursos.dominio.NotificacaoSimulada;
import com.senac.recursos.servico.NotificacaoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {
    private final NotificacaoService service;
    public NotificacaoController(NotificacaoService service) { this.service = service; }

    @GetMapping
    public List<NotificacaoSimulada> listar() { return service.listar(); }
}
