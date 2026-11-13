package com.senac.recursos.api;

import com.senac.recursos.dominio.TipoRecurso;
import com.senac.recursos.repositorio.RecursoRepository;
import com.senac.recursos.repositorio.ReservaRepository;
import com.senac.recursos.repositorio.ReservaAuditoriaRepository;
import com.senac.recursos.repositorio.UsuarioRepository;
import com.senac.recursos.dominio.Usuario;
import com.senac.recursos.dominio.PerfilAcesso;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockHttpSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    @Autowired ReservaRepository reservas;
    @Autowired ReservaAuditoriaRepository auditoria;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper mapper;

    @BeforeEach
    void limpar() { auditoria.deleteAll(); reservas.deleteAll(); recursos.deleteAll(); usuarios.deleteAll(); }

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
        mvc.perform(post("/recursos").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"tipo":"SALA","nome":"Sala bloqueada","restrito":false,"emManutencao":false}
                            """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMINISTRADOR")
    void administradorPodeCadastrarRecursos() throws Exception {
        mvc.perform(post("/recursos").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"tipo":"SALA","nome":"Sala autorizada","restrito":false,"emManutencao":false}
                            """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "solicitante", roles = "SOLICITANTE")
    void solicitanteNaoPodeAlterarEstadoDaReserva() throws Exception {
        mvc.perform(patch("/reservas/1/estado").with(csrf()).contentType(MediaType.APPLICATION_JSON)
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

    @Test
    void loginVisualAbreSemAutenticacao() throws Exception {
        mvc.perform(get("/login.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Acesse sua conta")));
        mvc.perform(get("/").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login.html"));
    }

    @Test
    void professorFazLoginNaTelaEConsultaSomenteSuaArea() throws Exception {
        usuarios.save(new Usuario("prof.maria", encoder.encode("senha-professora-123"), PerfilAcesso.PROFESSOR));
        MvcResult login = mvc.perform(post("/login").with(csrf())
                        .param("username", "prof.maria")
                        .param("password", "senha-professora-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        org.junit.jupiter.api.Assertions.assertNotNull(session);
        mvc.perform(get("/sessao").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("PROFESSOR"))
                .andExpect(jsonPath("$.login").value("prof.maria"));
        mvc.perform(get("/reservas").session(session))
                .andExpect(status().isOk());
        mvc.perform(post("/recursos").with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"SALA\",\"nome\":\"Sala proibida\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/reservas/1/estado").with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"APROVADA\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorNaoAprovaReservaRestritaEResponsavelAprova() throws Exception {
        usuarios.save(new Usuario("gestor", encoder.encode("senha-gestor-123"), PerfilAcesso.ADMINISTRADOR));
        MvcResult login = mvc.perform(post("/login").with(csrf())
                        .param("username", "gestor")
                        .param("password", "senha-gestor-123"))
                .andExpect(status().is3xxRedirection()).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        MvcResult recurso = mvc.perform(post("/recursos").with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"SALA\",\"nome\":\"Sala restrita\",\"restrito\":true}"))
                .andExpect(status().isCreated()).andReturn();
        long recursoId = mapper.readTree(recurso.getResponse().getContentAsString()).get("id").asLong();

        usuarios.save(new Usuario("professor1", encoder.encode("senha-prof-123456"), PerfilAcesso.PROFESSOR));
        MvcResult loginProfessor = mvc.perform(post("/login").with(csrf())
                        .param("username", "professor1").param("password", "senha-prof-123456"))
                .andExpect(status().is3xxRedirection()).andReturn();
        MockHttpSession sessaoProfessor = (MockHttpSession) loginProfessor.getRequest().getSession(false);
        Instant inicio = Instant.now().plusSeconds(24 * 3600);
        Instant fim = inicio.plusSeconds(3600);
        String novaReserva = mapper.writeValueAsString(java.util.Map.of(
                "recursoId", recursoId, "solicitante", "professor1",
                "inicio", inicio.toString(), "fim", fim.toString()));
        MvcResult criada = mvc.perform(post("/reservas").with(csrf()).session(sessaoProfessor)
                        .contentType(MediaType.APPLICATION_JSON).content(novaReserva))
                .andExpect(status().isCreated()).andReturn();
        long reservaId = mapper.readTree(criada.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(patch("/reservas/" + reservaId + "/estado").with(csrf()).session(sessaoProfessor)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"APROVADA\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/reservas").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].solicitante").value("professor1"));
        mvc.perform(patch("/reservas/" + reservaId + "/estado").with(csrf()).session(session)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"APROVADA\"}"))
                .andExpect(status().isForbidden());

        usuarios.save(new Usuario("responsavel1", encoder.encode("senha-responsavel-123"), PerfilAcesso.RESPONSAVEL));
        mvc.perform(patch("/reservas/" + reservaId + "/estado").with(csrf())
                        .with(httpBasic("responsavel1", "senha-responsavel-123"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"APROVADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROVADA"));
    }

    @Test
    void confirmacaoEmailPublicaNaoExigeLoginMasRejeitaTokenInvalido() throws Exception {
        mvc.perform(post("/confirmacao-email-professor").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"invalido"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void cadastroPublicoDeProfessorExigeAprovacaoAdmin() throws Exception {
        mvc.perform(get("/cadastro.html")).andExpect(status().isOk());
        mvc.perform(post("/cadastro-professor").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"maria@sp.senac.br\",\"senha\":\"senha-segura-123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensagem").exists());

        Usuario pendente = usuarios.findByLoginIgnoreCase("maria@sp.senac.br").orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(PerfilAcesso.PROFESSOR, pendente.getPerfil());
        org.junit.jupiter.api.Assertions.assertFalse(pendente.isAtivo());

        mvc.perform(get("/recursos").with(httpBasic("maria@sp.senac.br", "senha-segura-123")))
                .andExpect(status().isUnauthorized());

        mvc.perform(patch("/usuarios/" + pendente.getId() + "/ativo").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":true}"))
                .andExpect(status().isUnauthorized());

        mvc.perform(patch("/usuarios/" + pendente.getId() + "/ativo").with(csrf())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMINISTRADOR"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":true}"))
                .andExpect(status().isOk());

        mvc.perform(get("/recursos").with(httpBasic("maria@sp.senac.br", "senha-segura-123")))
                .andExpect(status().isOk());
    }

    @Test
    void cadastroPublicoNaoPermiteContaDeAdministrador() throws Exception {
        mvc.perform(post("/cadastro-professor").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@gmail.com\",\"senha\":\"senha-segura-123\",\"perfil\":\"ADMINISTRADOR\"}"))
                .andExpect(status().isUnprocessableEntity());
        org.junit.jupiter.api.Assertions.assertEquals(0, usuarios.count());
    }

    @Test
    void professorPendenteNaoConsegueLoginVisualAteAprovacao() throws Exception {
        Usuario professor = new Usuario("joao@sp.senac.br",
                encoder.encode("senha-segura-123"), PerfilAcesso.PROFESSOR);
        professor.setAtivo(false);
        professor = usuarios.save(professor);

        mvc.perform(post("/login").with(csrf()).param("username", "joao@sp.senac.br")
                        .param("password", "senha-segura-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html?erro"));

        mvc.perform(patch("/usuarios/" + professor.getId() + "/ativo").with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("admin").roles("ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":true}"))
                .andExpect(status().isOk());

        mvc.perform(post("/login").with(csrf()).param("username", "joao@sp.senac.br")
                        .param("password", "senha-segura-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockUser(username = "administrador", roles = "ADMINISTRADOR")
    void administradorNaoPodeMudarEstadoNemMovimentarMateriais() throws Exception {
        mvc.perform(patch("/reservas/999/estado").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"APROVADA\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/reservas/999/estado").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"NAO_COMPARECEU\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/reservas/999/retirada").with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/reservas/999/devolucao").with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/reservas/999/materiais/888/retirada").with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/reservas/999/materiais/888/devolucao").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "responsavel", roles = "RESPONSAVEL")
    void responsavelPodeAcessarAprovacaoDeReservas() throws Exception {
        // 404 indica que a autorização passou e a reserva solicitada não existe.
        mvc.perform(patch("/reservas/999999/estado").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"APROVADA\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "professor", roles = "PROFESSOR")
    void professorNaoPodeRegistrarMovimentacoesNemConsultarHistoricoDeRetiradas() throws Exception {
        mvc.perform(get("/reservas/movimentacoes"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/reservas/999/materiais/888/retirada").with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/reservas/999/materiais/888/devolucao").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "responsavel", roles = "RESPONSAVEL")
    void responsavelPodeConsultarMovimentacoes() throws Exception {
        mvc.perform(get("/reservas/movimentacoes"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "responsavel", roles = "RESPONSAVEL")
    void responsavelNaoPodeAtivarProfessor() throws Exception {
        mvc.perform(patch("/usuarios/999999/ativo").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ativo\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void professoresNaoVeemNemAlteramReservasDeOutrosProfessores() throws Exception {
        usuarios.save(new Usuario("prof.a", encoder.encode("senha-segura-123"), PerfilAcesso.PROFESSOR));
        usuarios.save(new Usuario("prof.b", encoder.encode("senha-segura-123"), PerfilAcesso.PROFESSOR));

        MvcResult recurso = mvc.perform(post("/recursos").with(csrf())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                .user("admin").roles("ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"SALA\",\"nome\":\"Sala de isolamento\"}"))
                .andExpect(status().isCreated()).andReturn();
        long recursoId = mapper.readTree(recurso.getResponse().getContentAsString()).get("id").asLong();
        Instant inicio = Instant.now().plusSeconds(172800);
        Instant fim = inicio.plusSeconds(3600);
        String pedido = mapper.writeValueAsString(java.util.Map.of(
                "recursoId", recursoId, "solicitante", "prof.a",
                "inicio", inicio.toString(), "fim", fim.toString()));
        MvcResult criada = mvc.perform(post("/reservas").with(csrf())
                        .with(httpBasic("prof.a", "senha-segura-123"))
                        .contentType(MediaType.APPLICATION_JSON).content(pedido))
                .andExpect(status().isCreated()).andReturn();
        long reservaId = mapper.readTree(criada.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/reservas").with(httpBasic("prof.b", "senha-segura-123")))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
        mvc.perform(get("/reservas/" + reservaId + "/auditoria")
                        .with(httpBasic("prof.b", "senha-segura-123")))
                .andExpect(status().isNotFound());
        String alteracao = mapper.writeValueAsString(java.util.Map.of(
                "recursoId", recursoId, "inicio", inicio.toString(), "fim", fim.toString()));
        mvc.perform(put("/reservas/" + reservaId).with(csrf())
                        .with(httpBasic("prof.b", "senha-segura-123"))
                        .contentType(MediaType.APPLICATION_JSON).content(alteracao))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/reservas/" + reservaId).with(csrf())
                        .with(httpBasic("prof.b", "senha-segura-123")))
                .andExpect(status().isForbidden());
    }


    @Test
    void desativacaoBloqueiaReservasMesmoComSessaoAberta() throws Exception {
        Usuario professor = usuarios.save(new Usuario("prof.desativado",
                encoder.encode("senha-professor-123"), PerfilAcesso.PROFESSOR));
        usuarios.save(new Usuario("admin.seg", encoder.encode("senha-admin-123456"),
                PerfilAcesso.ADMINISTRADOR));

        MvcResult loginProfessor = mvc.perform(post("/login").with(csrf())
                        .param("username", "prof.desativado").param("password", "senha-professor-123"))
                .andExpect(status().is3xxRedirection()).andReturn();
        MockHttpSession sessaoProfessor = (MockHttpSession) loginProfessor.getRequest().getSession(false);
        org.junit.jupiter.api.Assertions.assertNotNull(sessaoProfessor);

        MvcResult loginAdmin = mvc.perform(post("/login").with(csrf())
                        .param("username", "admin.seg").param("password", "senha-admin-123456"))
                .andExpect(status().is3xxRedirection()).andReturn();
        MockHttpSession sessaoAdmin = (MockHttpSession) loginAdmin.getRequest().getSession(false);

        mvc.perform(patch("/usuarios/" + professor.getId() + "/ativo").with(csrf())
                        .session(sessaoAdmin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ativo":false}
                                """))
                .andExpect(status().isOk());

        mvc.perform(post("/reservas").with(csrf()).session(sessaoProfessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recursoId":99999,"solicitante":"prof.desativado",
                                 "inicio":"2026-11-25T10:00:00Z","fim":"2026-11-25T11:00:00Z"}
                                """))
                .andExpect(status().isUnauthorized());
        org.junit.jupiter.api.Assertions.assertEquals(0, reservas.count());
    }

    @Test
    void atualizarPaginaDesconectaContaDesativada() throws Exception {
        Usuario professor = usuarios.save(new Usuario("prof.sessao",
                encoder.encode("senha-professor-123"), PerfilAcesso.PROFESSOR));
        MvcResult login = mvc.perform(post("/login").with(csrf())
                        .param("username", "prof.sessao").param("password", "senha-professor-123"))
                .andExpect(status().is3xxRedirection()).andReturn();
        MockHttpSession sessao = (MockHttpSession) login.getRequest().getSession(false);
        org.junit.jupiter.api.Assertions.assertNotNull(sessao);

        professor.setAtivo(false);
        usuarios.saveAndFlush(professor);

        mvc.perform(get("/").accept(MediaType.TEXT_HTML).session(sessao))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html?desativado"));
        org.junit.jupiter.api.Assertions.assertTrue(sessao.isInvalid());
    }

}
