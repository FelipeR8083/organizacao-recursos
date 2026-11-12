package com.senac.recursos.servico;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.Instant;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

/** Regras de reserva verificadas contra um PostgreSQL real (requer Docker). */
@Testcontainers
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class ReservaServiceIntegracaoTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final Instant DIA = Instant.parse("2026-11-10T00:00:00Z");

    @Autowired ReservaService service;
    @Autowired RecursoRepository recursos;
    @Autowired ReservaRepository reservas;
    @Autowired ReservaAuditoriaRepository auditoria;
    @Autowired ConflitoEvitadoRepository conflitos;
    @Autowired RelatorioService relatorios;
    @Autowired NotificacaoSimuladaRepository notificacoes;
    @Autowired MovimentacaoMaterialRepository movimentacoes;
    @Autowired MovimentacaoMaterialService movimentacaoService;

    private Recurso sala;

    private static Instant hora(int h) { return DIA.plusSeconds(h * 3600L); }

    @BeforeEach
    void limpar() {
        auditoria.deleteAll();
        conflitos.deleteAll();
        notificacoes.deleteAll();
        movimentacoes.deleteAll();
        reservas.deleteAll();
        recursos.deleteAll();
        sala = recursos.save(new Recurso(TipoRecurso.SALA, "Sala 101", false));
    }

    @Test
    void criaReservaSolicitadaQuandoHorarioEstaLivre() {
        Reserva r = service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        assertNotNull(r.getId());
        assertEquals(EstadoReserva.SOLICITADA, r.getEstado());
        assertEquals(1, reservas.count());
    }

    @Test
    void reservasQueApenasSeTocamSaoAceitas() {
        service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        assertDoesNotThrow(() -> service.criar(sala.getId(), "prof.bia", hora(11), hora(12)));
        assertDoesNotThrow(() -> service.criar(sala.getId(), "prof.caio", hora(9), hora(10)));
        assertEquals(3, reservas.count());
    }

    @Test
    void sobreposicaoParcialEhRejeitada() {
        service.criar(sala.getId(), "prof.ana", hora(10), hora(12));
        assertThrows(ConflitoHorarioException.class,
                () -> service.criar(sala.getId(), "prof.bia", hora(11), hora(13)));
        assertEquals(1, reservas.count());
    }

    @Test
    void reservaContidaEmOutraEhRejeitada() {
        service.criar(sala.getId(), "prof.ana", hora(8), hora(12));
        assertThrows(ConflitoHorarioException.class,
                () -> service.criar(sala.getId(), "prof.bia", hora(9), hora(10)));
    }

    @Test
    void mesmoHorarioEmRecursosDiferentesEhPermitido() {
        Recurso outraSala = recursos.save(new Recurso(TipoRecurso.SALA, "Sala 102", false));
        service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        assertDoesNotThrow(() -> service.criar(outraSala.getId(), "prof.bia", hora(10), hora(11)));
    }

    @Test
    void professorTambemNaoPodeTerHorariosSobrepostos() {
        Recurso professor = recursos.save(new Recurso(TipoRecurso.PROFESSOR, "Prof. Silva", false));
        Recurso outraSala = recursos.save(new Recurso(TipoRecurso.SALA, "Sala 102", false));
        service.criar(sala.getId(), professor.getId(), "coord.ana", hora(10), hora(12));
        assertThrows(ConflitoHorarioException.class,
                () -> service.criar(outraSala.getId(), professor.getId(), "coord.bia", hora(11), hora(13)));
    }

    @Test
    void naoPermiteProfessorComoRecursoPrincipal() {
        Recurso professor = recursos.save(new Recurso(TipoRecurso.PROFESSOR, "Prof. Silva", false));
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(professor.getId(), "coord.ana", hora(10), hora(11)));
        assertEquals(0, reservas.count());
    }

    @Test
    void reservaSalaComDoisMateriaisEImpedeConflitos() {
        Recurso projetor = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Projetor", false));
        Recurso notebook = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Notebook", false));
        Recurso outraSala = recursos.save(new Recurso(TipoRecurso.SALA, "Sala 102", false));

        Reserva criada = service.criar(sala.getId(), null,
                java.util.List.of(projetor.getId(), notebook.getId()),
                "prof.ana", hora(10), hora(12));
        assertEquals(2, criada.getMateriais().size());
        assertThrows(ConflitoHorarioException.class,
                () -> service.criar(outraSala.getId(), null,
                        java.util.List.of(projetor.getId()),
                        "prof.bia", hora(11), hora(13)));
        assertThrows(ConflitoHorarioException.class,
                () -> service.criar(notebook.getId(), "prof.caio", hora(11), hora(12)));
    }

    @Test
    void aprovacaoPorResponsavelPreservaMateriaisDaReservaConjunta() {
        Recurso projetor = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Projetor", false));
        Recurso notebook = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Notebook", false));
        Reserva solicitada = service.criar(sala.getId(), null,
                java.util.List.of(projetor.getId(), notebook.getId()),
                "prof.ana", hora(10), hora(12));

        Reserva aprovada = service.alterarEstado(solicitada.getId(),
                EstadoReserva.APROVADA, "responsavel", true);

        assertEquals(EstadoReserva.APROVADA, aprovada.getEstado());
        assertEquals(2, reservas.findById(aprovada.getId()).orElseThrow().getMateriais().size());
        assertThrows(ConflitoHorarioException.class,
                () -> service.criar(projetor.getId(), "prof.bia", hora(11), hora(12)));
    }

    @Test
    void cancelarReservaConjuntaLiberaMaterialParaOutraSala() {
        Recurso projetor = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Projetor", false));
        Recurso outraSala = recursos.save(new Recurso(TipoRecurso.SALA, "Sala 102", false));
        Reserva primeira = service.criar(sala.getId(), null, java.util.List.of(projetor.getId()),
                "prof.ana", hora(10), hora(12));
        service.cancelar(primeira.getId(), "prof.ana", false);
        assertDoesNotThrow(() -> service.criar(outraSala.getId(), null,
                java.util.List.of(projetor.getId()), "prof.bia", hora(11), hora(12)));
    }

    @Test
    void reagendamentoDeReservaConjuntaVerificaConflitoDoMaterial() {
        Recurso projetor = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Projetor", false));
        Recurso outraSala = recursos.save(new Recurso(TipoRecurso.SALA, "Sala 102", false));
        Reserva primeira = service.criar(sala.getId(), null, java.util.List.of(projetor.getId()),
                "prof.ana", hora(10), hora(11));
        service.criar(outraSala.getId(), null, java.util.List.of(projetor.getId()),
                "prof.bia", hora(12), hora(13));
        assertThrows(ConflitoHorarioException.class,
                () -> service.alterar(primeira.getId(), sala.getId(), null,
                        hora(12), hora(13), "prof.ana", false));
    }

    @Test
    void reservaConjuntaRejeitaMateriaisDuplicadosOuEmManutencao() {
        Recurso material = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Projetor", false));
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(sala.getId(), null,
                        java.util.List.of(material.getId(), material.getId()),
                        "prof.ana", hora(10), hora(11)));
        Recurso indisponivel = new Recurso(TipoRecurso.MATERIAL, "Notebook manutenção", false);
        indisponivel.setEmManutencao(true);
        indisponivel = recursos.save(indisponivel);
        Long indisponivelId = indisponivel.getId();
        assertThrows(RecursoIndisponivelException.class,
                () -> service.criar(sala.getId(), null,
                        java.util.List.of(indisponivelId),
                        "prof.ana", hora(10), hora(11)));
        assertEquals(0, reservas.count());
    }

    @Test
    void reservaCanceladaLiberaOHorario() {
        Reserva primeira = service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        primeira.transicionarPara(EstadoReserva.CANCELADA);
        reservas.save(primeira);

        assertDoesNotThrow(() -> service.criar(sala.getId(), "prof.bia", hora(10), hora(11)));
    }

    @Test
    void recursoInexistenteLancaNoSuchElement() {
        assertThrows(NoSuchElementException.class,
                () -> service.criar(999_999L, "prof.ana", hora(10), hora(11)));
    }

    @Test
    void periodoInvalidoEhRejeitadoSemGravarNada() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(sala.getId(), "prof.ana", hora(11), hora(10)));
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(sala.getId(), "prof.ana", hora(10), hora(10)));
        assertEquals(0, reservas.count());
    }
    @Test
    void solicitanteVazioOuComEspacosEhRejeitado() {
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(sala.getId(), "   ", hora(10), hora(11)));
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(sala.getId(), null, hora(10), hora(11)));
        assertEquals(0, reservas.count());
    }

    @Test
    void criaAuditoriaNaCriacaoEEmCadaMudancaDeEstado() {
        Reserva reserva = service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "responsavel", true);
        service.alterarEstado(reserva.getId(), EstadoReserva.EM_USO);

        var eventos = service.listarAuditoria(reserva.getId());
        assertEquals(3, eventos.size());
        assertNull(eventos.get(0).getEstadoAnterior());
        assertEquals(EstadoReserva.SOLICITADA, eventos.get(0).getEstadoNovo());
        assertEquals(EstadoReserva.SOLICITADA, eventos.get(1).getEstadoAnterior());
        assertEquals(EstadoReserva.APROVADA, eventos.get(1).getEstadoNovo());
        assertEquals(EstadoReserva.APROVADA, eventos.get(2).getEstadoAnterior());
        assertEquals(EstadoReserva.EM_USO, eventos.get(2).getEstadoNovo());
        assertEquals("API", eventos.get(2).getOrigem());
    }

    @Test
    void transicaoInvalidaNaoGeraEventoDeAuditoria() {
        Reserva reserva = service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        assertThrows(IllegalStateException.class,
                () -> service.alterarEstado(reserva.getId(), EstadoReserva.CONCLUIDA));
        assertEquals(1, service.listarAuditoria(reserva.getId()).size());
    }

    @Test
    void solicitanteEhArmazenadoSemEspacosNasExtremidades() {
        Reserva reserva = service.criar(sala.getId(), "  prof.ana  ", hora(10), hora(11));
        assertEquals("prof.ana", reserva.getSolicitante());
    }

    @Test
    void pesquisaRecursosPorTipoCapacidadeLocalizacaoECompetencia() {
        Recurso laboratorio = new Recurso(TipoRecurso.SALA, "Laboratorio 3", false);
        laboratorio.atualizarDetalhes(30, "Bloco B", "Java, redes e laboratorio");
        recursos.save(laboratorio);

        var encontrados = recursos.pesquisar(TipoRecurso.SALA, 25, "bloco b", "REDES");

        assertEquals(1, encontrados.size());
        assertEquals("Laboratorio 3", encontrados.get(0).getNome());
        assertEquals(30, encontrados.get(0).getCapacidade());
    }

    @Test
    void pesquisaNaoRetornaRecursoComCapacidadeInsuficiente() {
        Recurso pequena = new Recurso(TipoRecurso.SALA, "Sala pequena", false);
        pequena.atualizarDetalhes(10, "Bloco A", null);
        recursos.save(pequena);

        assertTrue(recursos.pesquisar(TipoRecurso.SALA, 20, null, null).isEmpty());
    }


    @Test
    void solicitantePodeCancelarSuaReservaComAuditoria() {
        Reserva reserva = service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        service.cancelar(reserva.getId(), "prof.ana", false);
        assertEquals(EstadoReserva.CANCELADA, reservas.findById(reserva.getId()).orElseThrow().getEstado());
        assertEquals(2, service.listarAuditoria(reserva.getId()).size());
    }

    @Test
    void solicitanteNaoPodeCancelarReservaDeOutraPessoa() {
        Reserva reserva = service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.cancelar(reserva.getId(), "prof.bia", false));
        assertEquals(EstadoReserva.SOLICITADA, reservas.findById(reserva.getId()).orElseThrow().getEstado());
        assertEquals(1, service.listarAuditoria(reserva.getId()).size());
    }


    @Test
    void professorVinculadoNaoPodeSerAlocadoEmDuasSalasNoMesmoHorario() {
        Recurso sala2 = recursos.save(new Recurso(TipoRecurso.SALA, "Sala 102", false));
        Recurso professor = recursos.save(new Recurso(TipoRecurso.PROFESSOR, "Prof. Silva", false));

        service.criar(sala.getId(), professor.getId(), "coord.ana", hora(10), hora(12));

        assertThrows(ConflitoHorarioException.class,
                () -> service.criar(sala2.getId(), professor.getId(), "coord.bia", hora(11), hora(13)));
    }

    @Test
    void professorVinculadoDeveSerDoTipoProfessor() {
        Recurso sala2 = recursos.save(new Recurso(TipoRecurso.SALA, "Sala 103", false));
        assertThrows(IllegalArgumentException.class,
                () -> service.criar(sala.getId(), sala2.getId(), "coord.ana", hora(10), hora(11)));
    }


    @Test
    void relatorioCalculaMinutosAlocadosNoIntervaloSolicitado() {
        service.criar(sala.getId(), "prof.ana", hora(10), hora(12));
        var linha = relatorios.utilizacao(hora(10).plusSeconds(1800), hora(11).plusSeconds(1800))
                .stream().filter(x -> x.recursoId().equals(sala.getId())).findFirst().orElseThrow();
        assertEquals(1, linha.reservas());
        assertEquals(60, linha.minutosAlocados());
    }

    @Test
    void conflitoRecusadoFicaRegistradoMesmoComRollbackDaReserva() {
        service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        assertThrows(ConflitoHorarioException.class,
                () -> service.criar(sala.getId(), "prof.bia", hora(10), hora(11)));
        assertEquals(1, conflitos.count());
        assertEquals(1, relatorios.conflitos(Instant.now().minusSeconds(86400), Instant.now().plusSeconds(86400)).total());
    }


    @Test
    void notificacoesSimuladasSaoGeradasNaSolicitacaoETransicao() {
        Reserva reserva = service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "responsavel", true);
        var enviadas = notificacoes.findByDestinatarioIgnoreCaseOrderByOcorridoEmDesc("prof.ana");
        assertEquals(2, enviadas.size());
        assertTrue(enviadas.stream().anyMatch(n -> n.getAssunto().equals("Reserva solicitada")));
        assertTrue(enviadas.stream().anyMatch(n -> n.getAssunto().equals("Reserva atualizada")));
        assertTrue(enviadas.stream().allMatch(n -> n.getCanal().equals("SIMULADO")));
    }


    @Test
    void registraRetiradaEDevolucaoDeMaterialComHistoricoDeEstados() {
        Recurso projetor = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Projetor E2E", false));
        Reserva reserva = service.criar(projetor.getId(), "prof.ana", hora(10), hora(11));
        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "responsavel", true);

        var retirada = movimentacaoService.retirar(reserva.getId(), "responsavel");
        assertNotNull(retirada.getRetiradaEm());
        assertEquals("responsavel", retirada.getRetiradaPor());
        assertEquals(EstadoReserva.EM_USO, reservas.findById(reserva.getId()).orElseThrow().getEstado());

        var devolucao = movimentacaoService.devolver(reserva.getId(), "responsavel");
        assertNotNull(devolucao.getDevolucaoEm());
        assertEquals("responsavel", devolucao.getDevolucaoPor());
        assertEquals(EstadoReserva.CONCLUIDA, reservas.findById(reserva.getId()).orElseThrow().getEstado());
        assertEquals(4, service.listarAuditoria(reserva.getId()).size());
    }

    @Test
    void retiradaEDevolucaoDeDoisMateriaisDaMesmaSalaSaoIndependentes() {
        Recurso projetor = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Projetor conjunto", false));
        Recurso notebook = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Notebook conjunto", false));
        Recurso naoReservado = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Item externo", false));
        Reserva reserva = service.criar(sala.getId(), null,
                java.util.List.of(projetor.getId(), notebook.getId()), "prof.ana", hora(10), hora(11));
        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "responsavel", true);

        assertThrows(IllegalArgumentException.class,
                () -> movimentacaoService.retirar(reserva.getId(), naoReservado.getId(), "responsavel"));
        assertThrows(IllegalArgumentException.class,
                () -> movimentacaoService.retirar(reserva.getId(), "responsavel"));

        var primeira = movimentacaoService.retirar(reserva.getId(), projetor.getId(), "responsavel");
        assertEquals(projetor.getId(), primeira.getMaterialId());
        assertEquals(EstadoReserva.EM_USO, reservas.findById(reserva.getId()).orElseThrow().getEstado());

        var segunda = movimentacaoService.retirar(reserva.getId(), notebook.getId(), "responsavel");
        assertEquals(notebook.getId(), segunda.getMaterialId());
        assertThrows(IllegalStateException.class,
                () -> movimentacaoService.retirar(reserva.getId(), projetor.getId(), "responsavel"));
        assertEquals(2, movimentacoes.findAllByReserva_Id(reserva.getId()).size());

        movimentacaoService.devolver(reserva.getId(), projetor.getId(), "responsavel");
        assertEquals(EstadoReserva.EM_USO, reservas.findById(reserva.getId()).orElseThrow().getEstado());

        movimentacaoService.devolver(reserva.getId(), notebook.getId(), "responsavel");
        assertEquals(EstadoReserva.CONCLUIDA, reservas.findById(reserva.getId()).orElseThrow().getEstado());
        assertTrue(movimentacoes.findAllByReserva_Id(reserva.getId()).stream()
                .allMatch(mov -> mov.getDevolucaoEm() != null));
        assertEquals(4, service.listarAuditoria(reserva.getId()).size());
    }

    @Test
    void impedeRetiradaDeReservaQueNaoEstejaAprovada() {
        Recurso projetor = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Projetor bloqueado", false));
        Reserva reserva = service.criar(projetor.getId(), "prof.ana", hora(10), hora(11));
        assertThrows(IllegalStateException.class,
                () -> movimentacaoService.retirar(reserva.getId(), "responsavel"));
        assertTrue(movimentacoes.findAllByReserva_Id(reserva.getId()).isEmpty());
    }


    @Test
    void titularPodeAlterarPeriodoEAlteracaoGeraAuditoria() {
        Reserva reserva = service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        Reserva alterada = service.alterar(reserva.getId(), sala.getId(), null,
                hora(12), hora(13), "prof.ana", false);
        assertEquals(hora(12), alterada.getInicio());
        assertEquals(hora(13), alterada.getFim());
        var eventos = service.listarAuditoria(reserva.getId());
        assertEquals(2, eventos.size());
        assertEquals("ALTERACAO", eventos.get(1).getTipoEvento());
    }

    @Test
    void titularNaoPodeAlterarReservaDeOutraPessoa() {
        Reserva reserva = service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.alterar(reserva.getId(), sala.getId(), null,
                        hora(12), hora(13), "prof.bia", false));
    }

    @Test
    void reagendamentoTambemVerificaConflitoDeHorario() {
        Recurso sala2 = recursos.save(new Recurso(TipoRecurso.SALA, "Sala 105", false));
        service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        Reserva ocupada = service.criar(sala2.getId(), "prof.bia", hora(12), hora(13));
        assertThrows(ConflitoHorarioException.class,
                () -> service.alterar(ocupada.getId(), sala.getId(), null,
                        hora(10), hora(11), "prof.bia", false));
    }

    @Test
    void reservaAprovadaNaoPodeSerReagendada() {
        Reserva reserva = service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "responsavel", true);
        assertThrows(IllegalStateException.class,
                () -> service.alterar(reserva.getId(), sala.getId(), null,
                        hora(12), hora(13), "prof.ana", false));
    }


    @Test
    void conclusaoManualNaoIgnoraEquipamentosPendentesEmReservaConjunta() {
        Recurso projetor = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Projetor", false));
        Recurso notebook = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Notebook", false));
        Reserva reserva = service.criar(sala.getId(), null,
                java.util.List.of(projetor.getId(), notebook.getId()),
                "prof.ana", hora(10), hora(11));
        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "responsavel", true);
        movimentacaoService.retirar(reserva.getId(), projetor.getId(), "responsavel");
        movimentacaoService.retirar(reserva.getId(), notebook.getId(), "responsavel");

        assertThrows(IllegalStateException.class,
                () -> service.alterarEstado(reserva.getId(), EstadoReserva.CONCLUIDA, "responsavel", true));
        assertEquals(EstadoReserva.EM_USO, reservas.findById(reserva.getId()).orElseThrow().getEstado());
        assertEquals(3, service.listarAuditoria(reserva.getId()).size());

        movimentacaoService.devolver(reserva.getId(), projetor.getId(), "responsavel");
        assertThrows(IllegalStateException.class,
                () -> service.alterarEstado(reserva.getId(), EstadoReserva.CONCLUIDA, "responsavel", true));
        assertEquals(EstadoReserva.EM_USO, reservas.findById(reserva.getId()).orElseThrow().getEstado());

        movimentacaoService.devolver(reserva.getId(), notebook.getId(), "responsavel");
        assertEquals(EstadoReserva.CONCLUIDA, reservas.findById(reserva.getId()).orElseThrow().getEstado());
        assertEquals(4, service.listarAuditoria(reserva.getId()).size());
    }

    @Test
    void conclusaoManualSemRetiradaDeMaterialPrincipalEhRecusada() {
        Recurso projetor = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Projetor principal", false));
        Reserva reserva = service.criar(projetor.getId(), "prof.ana", hora(10), hora(11));
        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "responsavel", true);
        service.alterarEstado(reserva.getId(), EstadoReserva.EM_USO, "responsavel", true);

        assertThrows(IllegalStateException.class,
                () -> service.alterarEstado(reserva.getId(), EstadoReserva.CONCLUIDA, "responsavel", true));
        assertEquals(EstadoReserva.EM_USO, reservas.findById(reserva.getId()).orElseThrow().getEstado());
        assertEquals(3, service.listarAuditoria(reserva.getId()).size());

        movimentacaoService.retirar(reserva.getId(), projetor.getId(), "responsavel");
        movimentacaoService.devolver(reserva.getId(), projetor.getId(), "responsavel");
        assertEquals(EstadoReserva.CONCLUIDA, reservas.findById(reserva.getId()).orElseThrow().getEstado());
    }

    @Test
    void somenteResponsavelPodeAprovarReservaComMaterialAdicionalRestrito() {
        Recurso projetorRestrito = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Projetor restrito", true));
        Reserva reserva = service.criar(sala.getId(), null, java.util.List.of(projetorRestrito.getId()),
                "prof.ana", hora(10), hora(11));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "admin", false));
        assertEquals(EstadoReserva.SOLICITADA, reservas.findById(reserva.getId()).orElseThrow().getEstado());

        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "responsavel", true);
        assertEquals(EstadoReserva.APROVADA, reservas.findById(reserva.getId()).orElseThrow().getEstado());
    }

    @Test
    void responsavelNaoPodeAprovarNemRejeitarPropriaReserva() {
        Reserva reserva = service.criar(sala.getId(), "resp.ana", hora(10), hora(11));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "RESP.ANA", true));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.alterarEstado(reserva.getId(), EstadoReserva.REJEITADA, "resp.ana", true));
        assertEquals(EstadoReserva.SOLICITADA, reservas.findById(reserva.getId()).orElseThrow().getEstado());
        assertEquals(1, service.listarAuditoria(reserva.getId()).size());

        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "resp.bia", true);
        assertEquals(EstadoReserva.APROVADA, reservas.findById(reserva.getId()).orElseThrow().getEstado());
        assertEquals(2, service.listarAuditoria(reserva.getId()).size());
    }

    @Test
    void administradorNaoAprovaNemMesmoRecursoComum() {
        Reserva reserva = service.criar(sala.getId(), "prof.ana", hora(10), hora(11));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "admin", false));
        assertEquals(EstadoReserva.SOLICITADA, reservas.findById(reserva.getId()).orElseThrow().getEstado());
    }

    @Test
    void somenteResponsavelPodeAprovarReservaDeRecursoRestrito() {
        Recurso salaRestrita = recursos.save(new Recurso(TipoRecurso.SALA, "Sala restrita", true));
        Reserva reserva = service.criar(salaRestrita.getId(), "prof.ana", hora(10), hora(11));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "admin", false));
        assertEquals(EstadoReserva.SOLICITADA, reservas.findById(reserva.getId()).orElseThrow().getEstado());

        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA, "responsavel", true);
        assertEquals(EstadoReserva.APROVADA, reservas.findById(reserva.getId()).orElseThrow().getEstado());
    }

    @Test
    void solicitanteVisualizaSomenteSuasReservasEHistoricos() {
        Reserva deAna = service.criar(sala.getId(), "ana", hora(10), hora(11));
        Reserva deBia = service.criar(sala.getId(), "bia", hora(11), hora(12));

        assertEquals(1, service.listarPara("ANA", false).size());
        assertEquals(deAna.getId(), service.listarPara("ANA", false).get(0).getId());
        assertEquals(2, service.listarPara("responsavel", true).size());

        assertEquals(1, service.listarAuditoriaPara(deAna.getId(), "ana", false).size());
        assertThrows(NoSuchElementException.class,
                () -> service.listarAuditoriaPara(deBia.getId(), "ana", false));
        assertEquals(1, service.listarAuditoriaPara(deBia.getId(), "responsavel", true).size());
    }

    @Test
    void solicitanteSemIdentidadeNaoPodeConsultarReservas() {
        service.criar(sala.getId(), "ana", hora(10), hora(11));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.listarPara(null, false));
    }

}
