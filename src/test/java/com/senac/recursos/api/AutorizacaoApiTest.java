package com.senac.recursos.api;

import com.senac.recursos.dominio.TipoRecurso;
import com.senac.recursos.repositorio.RecursoRepository;
import com.senac.recursos.repositorio.UsuarioRepository;
import com.senac.recursos.dominio.Usuario;
import com.senac.recursos.dominio.PerfilAcesso;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
class AutorizacaoApiTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired MockMvc mvc;
    @Autowired RecursoRepository recursos;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void limpar() { recursos.deleteAll(); usuarios.deleteAll(); }

    @Test
    void apiExigeAutenticacao() throws Exception {
        mvc.perform(get("/recursos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "solicitante", roles = "SOLICITANTE")
    void solicitantePodeConsultarRecursos() throws Exception {
        mvc.perform(get("/recursos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "solicitante", roles = "SOLICITANTE")
    void solicitanteNaoPodeCadastrarRecursos() throws Exception {
        mvc.perform(post("/recursos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"tipo":"SALA","nome":"Sala bloqueada","restrito":false,"emManutencao":false}
                            """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMINISTRADOR")
    void administradorPodeCadastrarRecursos() throws Exception {
        mvc.perform(post("/recursos").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"tipo":"SALA","nome":"Sala autorizada","restrito":false,"emManutencao":false}
                            """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "solicitante", roles = "SOLICITANTE")
    void solicitanteNaoPodeAlterarEstadoDaReserva() throws Exception {
        mvc.perform(patch("/reservas/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"CANCELADA\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void autenticaUsuarioPersistidoComSenhaCriptografada() throws Exception {
        usuarios.save(new Usuario("ana", encoder.encode("senha-segura-123"), PerfilAcesso.SOLICITANTE));
        mvc.perform(get("/recursos").with(httpBasic("ana", "senha-segura-123")))
                .andExpect(status().isOk());
        mvc.perform(get("/recursos").with(httpBasic("ana", "senha-incorreta")))
                .andExpect(status().isUnauthorized());
    }

}
