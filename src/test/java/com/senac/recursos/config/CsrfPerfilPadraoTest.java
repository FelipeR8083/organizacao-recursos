package com.senac.recursos.config;

import com.senac.recursos.api.CsrfController;
import com.senac.recursos.repositorio.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** No perfil padrao, o token CSRF tambem e obrigatorio (nao apenas em producao). */
@WebMvcTest(controllers = CsrfController.class)
@Import(SegurancaConfig.class)
class CsrfPerfilPadraoTest {
    @Autowired MockMvc mvc;
    @MockBean UsuarioRepository usuarios;

    @Test
    void endpointRetornaTokenCsrfMesmoSemPerfilProducao() throws Exception {
        mvc.perform(get("/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.headerName").value("X-CSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMINISTRADOR")
    void postSemTokenEhRejeitadoTambemNoPerfilPadrao() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/usuarios").contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }
}
