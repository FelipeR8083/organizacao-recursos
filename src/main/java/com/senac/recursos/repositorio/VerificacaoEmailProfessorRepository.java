package com.senac.recursos.repositorio;

import com.senac.recursos.dominio.VerificacaoEmailProfessor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificacaoEmailProfessorRepository extends JpaRepository<VerificacaoEmailProfessor, Long> {
    Optional<VerificacaoEmailProfessor> findByTokenHash(String tokenHash);
}
