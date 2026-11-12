package com.senac.recursos.config;

import com.senac.recursos.api.CsrfController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.senac.recursos.repositorio.UsuarioRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Exercita o filtro CSRF em perfil de producao sem exigir PostgreSQL. */
@WebMvcTest(controllers = CsrfController.class)
@Import(SegurancaConfig.class)
@ActiveProfiles("prod")
class CsrfProducaoTest {
    @Autowired MockMvc mvc;
    @MockBean UsuarioRepository usuarios;

    @Test
    void endpointDisponibilizaToken() throws Exception {
        mvc.perform(get("/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void requisicaoSemTokenEhBloqueada() throws Exception {
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void requisicaoComTokenUltrapassaFiltroCsrf() throws Exception {
        var resposta = mvc.perform(get("/csrf")).andReturn().getResponse();
        String token = new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(resposta.getContentAsString()).get("token").asText();
        mvc.perform(post("/usuarios").cookie(resposta.getCookies())
                .header("X-XSRF-TOKEN", token)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertNotEquals(403, result.getResponse().getStatus()));
    }
}
