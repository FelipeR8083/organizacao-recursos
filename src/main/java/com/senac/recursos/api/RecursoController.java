package com.senac.recursos.api;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.RecursoRepository;
import com.senac.recursos.repositorio.ReservaRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import com.senac.recursos.servico.AuditoriaAdministrativaService;
import java.time.Instant;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Locale;

@RestController
@RequestMapping("/recursos")
public class RecursoController {
    public record Dados(@NotNull TipoRecurso tipo, @NotBlank String nome, boolean restrito,
                        boolean emManutencao, @Positive Integer capacidade,
                        String localizacao, String competencias) {
        public Dados(TipoRecurso tipo, String nome, boolean restrito, boolean emManutencao) {
            this(tipo, nome, restrito, emManutencao, null, null, null);
        }
    }
    public record Alteracao(@NotBlank String nome, boolean restrito, boolean emManutencao,
                            @Positive Integer capacidade, String localizacao, String competencias) {
        public Alteracao(String nome, boolean restrito, boolean emManutencao) {
            this(nome, restrito, emManutencao, null, null, null);
        }
    }
    private final RecursoRepository repository;
    private final ReservaRepository reservas;
    private final AuditoriaAdministrativaService auditoriaAdministrativa;

    public RecursoController(RecursoRepository repository) { this(repository, null, null); }

    public RecursoController(RecursoRepository repository, ReservaRepository reservas) {
        this(repository, reservas, null);
    }

    @Autowired
    public RecursoController(RecursoRepository repository, ReservaRepository reservas,
                             AuditoriaAdministrativaService auditoriaAdministrativa) {
        this.repository = repository;
        this.reservas = reservas;
        this.auditoriaAdministrativa = auditoriaAdministrativa;
    }

    @GetMapping
    public List<Recurso> listar(@RequestParam(required = false) TipoRecurso tipo,
                                @RequestParam(required = false) @Positive Integer capacidadeMinima,
                                @RequestParam(required = false) String localizacao,
                                @RequestParam(required = false) String competencia,
                                @RequestParam(required = false) Instant inicio,
                                @RequestParam(required = false) Instant fim) {
        if ((inicio == null) != (fim == null)) {
            throw new IllegalArgumentException("Informe início e fim para pesquisar disponibilidade");
        }
        if (inicio != null && !fim.isAfter(inicio)) {
            throw new IllegalArgumentException("Fim deve ser posterior ao início");
        }
        List<Recurso> encontrados = repository.pesquisar(tipo, capacidadeMinima,
                normalizarFiltro(localizacao), normalizarFiltro(competencia));
        if (inicio == null) return encontrados;
        if (reservas == null) throw new IllegalStateException("Consulta de disponibilidade indisponível neste contexto");
        return encontrados.stream()
                .filter(r -> !r.isEmManutencao())
                .filter(r -> !reservas.existeConflito(r.getId(), EstadoReserva.ativos(), inicio, fim))
                .toList();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Recurso> criar(@Valid @RequestBody Dados dados) {
        validarNome(dados.tipo(), dados.nome(), null);
        Recurso recurso = new Recurso(dados.tipo(), dados.nome().trim(), dados.restrito());
        recurso.setEmManutencao(dados.emManutencao());
        recurso.atualizarDetalhes(dados.capacidade(), dados.localizacao(), dados.competencias());
        Recurso salvo = repository.save(recurso);
        if (auditoriaAdministrativa != null) {
            auditoriaAdministrativa.registrar("RECURSO", salvo.getId(), "CRIACAO",
                    "tipo=" + salvo.getTipo() + ", nome=" + salvo.getNome());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id}")
    @Transactional
    public Recurso atualizar(@PathVariable Long id, @Valid @RequestBody Alteracao dados) {
        Recurso recurso = repository.findById(id).orElseThrow(() -> new NoSuchElementException("Recurso não encontrado"));
        validarNome(recurso.getTipo(), dados.nome(), id);
        String anterior = "nome=" + recurso.getNome() + ", restrito=" + recurso.isRestrito()
                + ", manutencao=" + recurso.isEmManutencao();
        recurso.setNome(dados.nome().trim());
        recurso.setRestrito(dados.restrito());
        recurso.setEmManutencao(dados.emManutencao());
        recurso.atualizarDetalhes(dados.capacidade(), dados.localizacao(), dados.competencias());
        Recurso salvo = repository.save(recurso);
        if (auditoriaAdministrativa != null) {
            auditoriaAdministrativa.registrar("RECURSO", salvo.getId(), "ALTERACAO",
                    anterior + " -> nome=" + salvo.getNome() + ", restrito=" + salvo.isRestrito()
                            + ", manutencao=" + salvo.isEmManutencao());
        }
        return salvo;
    }

    private static String normalizarFiltro(String valor) {
        return valor == null || valor.isBlank() ? "" : valor.trim().toLowerCase(Locale.ROOT);
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
    ResponseEntity<Map<String,String>> ausente(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,String>> invalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }
}
