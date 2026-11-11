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
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    public record NovaReserva(@NotNull Long recursoId, Long professorId, List<Long> materialIds,
                              @NotBlank String solicitante, @NotNull Instant inicio, @NotNull Instant fim) {}
    public record AlteracaoEstado(@NotNull EstadoReserva estado) {}
    public record AlteracaoReserva(@NotNull Long recursoId, Long professorId,
                                   @NotNull Instant inicio, @NotNull Instant fim) {}

    private final ReservaService service;

    public ReservaController(ReservaService service) { this.service = service; }

    @GetMapping
    public List<Reserva> listar(Authentication autenticacao) {
        if (autenticacao == null) return service.listar(); // Testes isolados sem filtros de segurança
        return service.listarPara(autenticacao.getName(), podeGerenciar(autenticacao));
    }

    @GetMapping("/{id}/auditoria")
    public List<ReservaAuditoria> auditoria(@PathVariable Long id, Authentication autenticacao) {
        if (autenticacao == null) return service.listarAuditoria(id); // Testes isolados
        return service.listarAuditoriaPara(id, autenticacao.getName(), podeGerenciar(autenticacao));
    }

    private static boolean podeGerenciar(Authentication autenticacao) {
        return autenticacao.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RESPONSAVEL")
                        || a.getAuthority().equals("ROLE_ADMINISTRADOR"));
    }



    @PutMapping("/{id}")
    public Reserva alterar(@PathVariable Long id, @Valid @RequestBody AlteracaoReserva dados,
                           Authentication autenticacao) {
        boolean privilegiado = autenticacao.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RESPONSAVEL")
                        || a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        return service.alterar(id, dados.recursoId(), dados.professorId(), dados.inicio(),
                dados.fim(), autenticacao.getName(), privilegiado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, Authentication autenticacao) {
        boolean privilegiado = autenticacao.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RESPONSAVEL")
                        || a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        service.cancelar(id, autenticacao.getName(), privilegiado);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public Reserva alterarEstado(@PathVariable Long id, @Valid @RequestBody AlteracaoEstado req,
                                  Authentication autenticacao) {
        boolean administrador = autenticacao != null && autenticacao.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        if (!administrador) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Somente o administrador pode alterar o estado de reservas");
        }
        return service.alterarEstado(id, req.estado(), autenticacao.getName(), true);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> criar(@Valid @RequestBody NovaReserva req, Authentication autenticacao) {
        String solicitante = autenticacao == null ? req.solicitante() : autenticacao.getName();
        Reserva r = service.criar(req.recursoId(), req.professorId(), req.materialIds(), solicitante, req.inicio(), req.fim());
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
