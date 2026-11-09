package com.senac.recursos.servico;

import com.senac.recursos.dominio.ConflitoEvitado;
import com.senac.recursos.repositorio.ConflitoEvitadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class ConflitoEvitadoService {
    private final ConflitoEvitadoRepository repository;
    public ConflitoEvitadoService(ConflitoEvitadoRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long recursoId, Instant inicio, Instant fim, String motivo) {
        repository.save(new ConflitoEvitado(recursoId, Instant.now(), inicio, fim, motivo));
    }
}
