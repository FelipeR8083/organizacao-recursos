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
        service.criar(professor.getId(), "coord.ana", hora(10), hora(12));
        assertThrows(ConflitoHorarioException.class,
                () -> service.criar(professor.getId(), "coord.bia", hora(11), hora(13)));
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
        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA);
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
        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA);
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
        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA);

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
    void impedeRetiradaDeReservaQueNaoEstejaAprovada() {
        Recurso projetor = recursos.save(new Recurso(TipoRecurso.MATERIAL, "Projetor bloqueado", false));
        Reserva reserva = service.criar(projetor.getId(), "prof.ana", hora(10), hora(11));
        assertThrows(IllegalStateException.class,
                () -> movimentacaoService.retirar(reserva.getId(), "responsavel"));
        assertTrue(movimentacoes.findByReserva_Id(reserva.getId()).isEmpty());
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
        service.alterarEstado(reserva.getId(), EstadoReserva.APROVADA);
        assertThrows(IllegalStateException.class,
                () -> service.alterar(reserva.getId(), sala.getId(), null,
                        hora(12), hora(13), "prof.ana", false));
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

}
