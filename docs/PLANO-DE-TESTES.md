# Plano de testes

## Estratégia

| Camada | Ferramenta | Objetivo | Evidência |
|---|---|---|---|
| Domínio | JUnit 5 | Períodos, estados e invariantes | `PeriodoTest`, `EstadoReservaTest`, `ReservaTest`, `RecursoTest` |
| Parametrizados | JUnit 5 `@ParameterizedTest` | Combinações de sobreposição, transições e payloads inválidos | `PeriodoTest`, `EstadoReservaTest`, `ReservaControllerTest` |
| Integração | Spring Boot Test + Testcontainers PostgreSQL | Persistência, locks, auditoria, notificações, relatórios e movimentação | `ReservaServiceIntegracaoTest`, `ReservaConcorrenciaTest` |
| Segurança/API | MockMvc + Spring Security Test | 401, 403, papéis e autenticação Basic persistida | `AutorizacaoApiTest`, `ReservaControllerTest` |
| Ponta a ponta HTTP | TestRestTemplate + Testcontainers | Fluxo real com aplicação iniciada, autenticação, banco e múltiplos endpoints | `FluxoCompletoE2ETest` |
| Integração externa | WireMock | Sucesso e falha do webhook de notificação | `NotificacaoServiceWireMockTest` |
| Concorrência | ExecutorService + PostgreSQL | Uma única reserva aceita para mesmo recurso/período | `ReservaConcorrenciaTest` |
| Carga | Apache JMeter | Disparar 20 solicitações simultâneas e analisar latência/status | `jmeter/reservas-concorrentes.jmx` |

## Execução local

```bash
mvn clean verify
```

Docker deve estar em execução para os testes com Testcontainers. O relatório JaCoCo fica em `target/site/jacoco/index.html`.

## Critérios de aceitação

1. Todos os testes automatizados passam.
2. Um único pedido simultâneo para mesmo recurso e período recebe 201; os demais recebem 409.
3. Solicitante recebe 401 sem credenciais, 403 ao tentar ação administrativa e só altera/cancela sua própria reserva.
4. Apenas RESPONSAVEL aprova reserva com recurso principal ou professor restrito.
5. Auditoria e notificações são persistidas na transação do evento.
6. Retirada só é permitida após aprovação e devolução conclui a reserva.
7. Relatório calcula minutos alocados no intervalo solicitado e inclui conflitos evitados persistidos.
8. Webhook externo é isolado por WireMock; sem URL, canal SIMULADO.
9. Metas de cobertura exigidas pelo docente: 80% de linhas e 70% de branches.
10. E2E de navegador, teste JMeter executado e análise SonarCloud devem ser anexados como evidências, não apenas descritos.

## Resultado da execução

O resultado oficial deve ser preenchido após o último GitHub Actions. Não declarar aprovação com base apenas na existência dos testes.
