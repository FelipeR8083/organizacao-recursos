package com.senac.recursos.api;

import com.senac.recursos.servico.ConflitoHorarioException;
import com.senac.recursos.servico.RecursoIndisponivelException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String,String>> validacao(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Verifique os campos obrigatórios"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String,String>> json(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Dados enviados inválidos"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String,String>> naoEncontrado(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(ConflitoHorarioException.class)
    ResponseEntity<Map<String,String>> conflito(ConflitoHorarioException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String,String>> estadoInvalido(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(RecursoIndisponivelException.class)
    ResponseEntity<Map<String,String>> indisponivel(RecursoIndisponivelException e) {
        return ResponseEntity.unprocessableEntity().body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,String>> argumentoInvalido(IllegalArgumentException e) {
        return ResponseEntity.unprocessableEntity().body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String,String>> integridade(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", "A operação viola uma restrição de integridade dos dados"));
    }
}
