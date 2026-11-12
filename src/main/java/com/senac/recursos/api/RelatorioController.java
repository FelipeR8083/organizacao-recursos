package com.senac.recursos.api;

import com.senac.recursos.servico.RelatorioService;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {
    private final RelatorioService service;
    public RelatorioController(RelatorioService service) { this.service = service; }

    @GetMapping("/utilizacao")
    public List<RelatorioService.Utilizacao> utilizacao(@RequestParam Instant inicio,
                                                        @RequestParam Instant fim) {
        return service.utilizacao(inicio, fim);
    }

    @GetMapping("/conflitos")
    public RelatorioService.RelatorioConflitos conflitos(@RequestParam Instant inicio,
                                                          @RequestParam Instant fim) {
        return service.conflitos(inicio, fim);
    }
}
