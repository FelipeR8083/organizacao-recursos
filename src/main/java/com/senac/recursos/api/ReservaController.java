package com.senac.recursos.api;

import com.senac.recursos.dominio.Reserva;
import com.senac.recursos.dominio.ReservaAuditoria;
import com.senac.recursos.servico.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import com.senac.recursos.dominio.EstadoReserva;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    public record NovaReserva(@NotNull Long recursoId, @NotBlank String solicitante,
                              @NotNull Instant inicio, @NotNull Instant fim) {}
    public record AlteracaoEstado(@NotNull EstadoReserva estado) {}

    private final ReservaService service;

    public ReservaController(ReservaService service) { this.service = service; }

    @GetMapping
    public List<Reserva> listar() { return service.listar(); }

    @GetMapping("/{id}/auditoria")
    public List<ReservaAuditoria> auditoria(@PathVariable Long id) {
        return service.listarAuditoria(id);
    }

    @PatchMapping("/{id}/estado")
    public Reserva alterarEstado(@PathVariable Long id, @Valid @RequestBody AlteracaoEstado req) {
        return service.alterarEstado(id, req.estado());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> criar(@Valid @RequestBody NovaReserva req) {
        Reserva r = service.criar(req.recursoId(), req.solicitante(), req.inicio(), req.fim());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", r.getId(), "estado", r.getEstado()));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String, String>> transicaoInvalida(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(ConflitoHorarioException.class)
    ResponseEntity<Map<String, String>> conflito(ConflitoHorarioException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler({RecursoIndisponivelException.class, IllegalArgumentException.class})
    ResponseEntity<Map<String, String>> invalido(RuntimeException e) {
        return ResponseEntity.unprocessableEntity().body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String, String>> naoEncontrado(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
    }
}
