package com.senac.recursos.servico;

import com.senac.recursos.dominio.*;
import com.senac.recursos.repositorio.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

/** Requer Docker em execução (Testcontainers). */
@Testcontainers
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class ReservaConcorrenciaTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired ReservaService service;
    @Autowired RecursoRepository recursos;
    @Autowired ReservaRepository reservas;

    @Test
    void solicitacoesSimultaneasProduzemUmaUnicaReserva() throws Exception {
        Recurso sala = recursos.save(new Recurso(TipoRecurso.SALA, "Sala 101", false));
        Instant inicio = Instant.parse("2026-11-10T10:00:00Z");
        Instant fim = inicio.plusSeconds(3600);

        int tentativas = 10;
        ExecutorService pool = Executors.newFixedThreadPool(tentativas);
        CountDownLatch largada = new CountDownLatch(1);
        AtomicInteger aceitas = new AtomicInteger();
        AtomicInteger conflitos = new AtomicInteger();
        List<Future<?>> futuros = new ArrayList<>();

        for (int i = 0; i < tentativas; i++) {
            String quem = "prof" + i;
            futuros.add(pool.submit(() -> {
                largada.await();
                try {
                    service.criar(sala.getId(), quem, inicio, fim);
                    aceitas.incrementAndGet();
                } catch (ConflitoHorarioException e) {
                    conflitos.incrementAndGet();
                }
                return null;
            }));
        }
        largada.countDown();
        for (Future<?> f : futuros) f.get(30, TimeUnit.SECONDS);
        pool.shutdown();

        assertEquals(1, aceitas.get(), "Somente uma reserva pode ser aceita");
        assertEquals(tentativas - 1, conflitos.get());
        assertEquals(1, reservas.count());
    }

    @Test
    void naoPermiteReservarRecursoEmManutencao() {
        Recurso lab = new Recurso(TipoRecurso.SALA, "Lab 2", false);
        lab.setEmManutencao(true);
        Recurso salvo = recursos.save(lab);
        Instant inicio = Instant.parse("2026-11-11T10:00:00Z");
        assertThrows(RecursoIndisponivelException.class,
                () -> service.criar(salvo.getId(), "prof", inicio, inicio.plusSeconds(3600)));
    }
}
