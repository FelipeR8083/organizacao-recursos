package com.senac.recursos.api;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.RecursoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Locale;

@RestController
@RequestMapping("/recursos")
public class RecursoController {
    public record Dados(@NotNull TipoRecurso tipo, @NotBlank String nome, boolean restrito, boolean emManutencao) {}
    public record Alteracao(@NotBlank String nome, boolean restrito, boolean emManutencao) {}
    private final RecursoRepository repository;
    public RecursoController(RecursoRepository repository) { this.repository = repository; }
    @GetMapping public List<Recurso> listar() { return repository.findAll(); }
    @PostMapping public ResponseEntity<Recurso> criar(@Valid @RequestBody Dados dados) {
        validarNome(dados.tipo(), dados.nome(), null);
        Recurso recurso = new Recurso(dados.tipo(), dados.nome().trim(), dados.restrito());
        recurso.setEmManutencao(dados.emManutencao());
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(recurso));
    }
    @PutMapping("/{id}") public Recurso atualizar(@PathVariable Long id, @Valid @RequestBody Alteracao dados) {
        Recurso recurso = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Recurso não encontrado"));
        validarNome(recurso.getTipo(), dados.nome(), id);
        recurso.setNome(dados.nome().trim()); recurso.setRestrito(dados.restrito());
        recurso.setEmManutencao(dados.emManutencao());
        return repository.save(recurso);
    }
    private void validarNome(TipoRecurso tipo, String nome, Long id) {
        if (repository.existeNomeNormalizado(tipo, nome.trim().toLowerCase(Locale.ROOT), id)) {
            throw new NomeRecursoDuplicadoException("Já existe um recurso do tipo " + tipo + " com esse nome. Escolha outro nome.");
        }
    }
    @ExceptionHandler(NomeRecursoDuplicadoException.class)
    ResponseEntity<Map<String,String>> duplicado(NomeRecursoDuplicadoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String,String>> restricao(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "Já existe um recurso com esse nome e tipo, ou há conflito com uma restrição do banco."));
    }
    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String,String>> ausente(NoSuchElementException e) { return ResponseEntity.status(404).body(Map.of("erro",e.getMessage())); }
}
