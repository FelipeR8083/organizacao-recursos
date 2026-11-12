package com.senac.recursos.servico;

import com.senac.recursos.dominio.Usuario;
import com.senac.recursos.dominio.VerificacaoEmailProfessor;
import com.senac.recursos.repositorio.VerificacaoEmailProfessorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
public class VerificacaoEmailProfessorService {
    private final VerificacaoEmailProfessorRepository tokens;
    private final SecureRandom aleatorio = new SecureRandom();

    public VerificacaoEmailProfessorService(VerificacaoEmailProfessorRepository tokens) {
        this.tokens = tokens;
    }

    /** Devolve o segredo somente ao chamador que deve entrega-lo por e-mail. Nunca persiste o segredo. */
    @Transactional
    public String emitir(Usuario professor) {
        if (!professor.isVerificacaoEmailObrigatoria() || professor.getEmailVerificadoEm() != null) {
            throw new IllegalStateException("A conta nao esta aguardando verificacao de e-mail");
        }
        byte[] segredo = new byte[32];
        aleatorio.nextBytes(segredo);
        String token = HexFormat.of().formatHex(segredo);
        Instant agora = Instant.now();
        tokens.save(new VerificacaoEmailProfessor(professor, hash(token), agora,
                agora.plus(24, ChronoUnit.HOURS)));
        return token;
    }

    @Transactional
    public void confirmar(String token) {
        if (token == null || !token.matches("[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException("Token de verificacao invalido");
        }
        VerificacaoEmailProfessor registro = tokens.findByTokenHash(hash(token.toLowerCase(java.util.Locale.ROOT)))
                .orElseThrow(() -> new IllegalArgumentException("Token de verificacao invalido"));
        Instant agora = Instant.now();
        if (registro.getConsumidoEm() != null || !agora.isBefore(registro.getExpiraEm())) {
            throw new IllegalArgumentException("Token expirado ou ja utilizado");
        }
        Usuario professor = registro.getUsuario();
        if (!professor.isVerificacaoEmailObrigatoria() || professor.getEmailVerificadoEm() != null) {
            throw new IllegalArgumentException("E-mail ja confirmado ou verificacao nao exigida");
        }
        professor.setEmailVerificadoEm(agora);
        registro.consumir(agora);
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }
}
