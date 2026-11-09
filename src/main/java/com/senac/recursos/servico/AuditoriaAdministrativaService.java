package com.senac.recursos.servico;

import com.senac.recursos.dominio.AuditoriaAdministrativa;
import com.senac.recursos.repositorio.AuditoriaAdministrativaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class AuditoriaAdministrativaService {
    private final AuditoriaAdministrativaRepository repository;

    public AuditoriaAdministrativaService(AuditoriaAdministrativaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registrar(String entidade, Long id, String acao, String detalhes) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String ator = auth == null || !auth.isAuthenticated()
                ? "SISTEMA" : auth.getName();
        repository.save(new AuditoriaAdministrativa(
                entidade, id, acao, ator, detalhes, Instant.now()));
    }

    @Transactional(readOnly = true)
    public List<AuditoriaAdministrativa> listar() {
        return repository.findAllByOrderByOcorridoEmDescIdDesc();
    }
}
