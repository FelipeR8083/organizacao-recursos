package com.senac.recursos.api;

import com.senac.recursos.dominio.AuditoriaAdministrativa;
import com.senac.recursos.servico.AuditoriaAdministrativaService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/auditoria-administrativa")
public class AuditoriaAdministrativaController {
    private final AuditoriaAdministrativaService service;
    public AuditoriaAdministrativaController(AuditoriaAdministrativaService service) {
        this.service = service;
    }

    @GetMapping
    public List<AuditoriaAdministrativa> listar() {
        return service.listar();
    }
}
