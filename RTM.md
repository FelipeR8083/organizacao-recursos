# RTM — Matriz de Rastreabilidade de Requisitos

Fonte: enunciado oficial "Especificação do Projeto — Qualidade 2026.2", seção "Requisitos funcionais obrigatórios" (itens 1 a 14).

**Regra de status:** "Implementado" exige código e evidência verificável; "Parcial" significa que apenas parte do critério está coberta; "Pendente" significa que não foi encontrada implementação/evidência suficiente nesta branch. A aprovação final depende também de demonstração e dos critérios de qualidade do enunciado.

## Requisitos funcionais

| ID | Requisito oficial (síntese fiel) | Status observado | Evidência atual / lacuna |
|---|---|---|---|
| RF-01 | Autenticação e autorização por perfil | Implementado no código/testes | HTTP Basic, BCrypt, perfis SOLICITANTE/RESPONSAVEL/ADMINISTRADOR, gestão de usuários e endpoint de sessão. Testes MockMvc/E2E cobrem 401/403, autenticação persistida, isolamento de reservas e histórico por titularidade. A interface mostra ações de acordo com o perfil. |
| RF-02 | Cadastro e consulta de salas, professores e materiais | Implementado no código/testes | `TipoRecurso` cobre SALA/PROFESSOR/MATERIAL; `RecursoController` cadastra, lista e altera recursos com nome, restrição, manutenção, capacidade, localização e competências. Testes cobrem duplicação e edição; usuário ADMINISTRADOR controla cadastros e usuários. |
| RF-03 | Pesquisa por tipo, capacidade, localização, competência e disponibilidade | Implementado no código/testes | `GET /recursos` aceita filtros por tipo, capacidade, localização, competência e disponibilidade. `ReservaServiceIntegracaoTest` cobre metadados; `FluxoCompletoE2ETest` testa filtros e consulta HTTP de horários livres, ocupados e adjacentes. Formulário de pesquisa disponível na interface. Validação visual manual permanece pendente no RF-13. |
| RF-04 | Criação, alteração e cancelamento de reservas | Implementado no código/testes | Criação, reagendamento de reserva SOLICITADA e cancelamento verificam titularidade/papel, revalidam conflitos e registram auditoria. Testes de integração cobrem reagendamento, conflito e titularidade. |
| RF-05 | Sobreposição de horários, inclusive na agenda do professor | Implementado no código/testes | Reserva pode vincular professor; o serviço bloqueia recurso principal e professor em ordem de ID e consulta conflitos em ambos. Teste de integração cobre duas salas tentando usar o mesmo professor no mesmo horário. |
| RF-06 | Proteção contra dupla reserva em solicitações simultâneas | Implementado no código/testes | Bloqueio pessimista na linha do recurso antes da consulta de conflitos; teste de concorrência com Testcontainers. Revalidar no CI da revisão final. |
| RF-07 | Aprovação obrigatória para recursos restritos | Implementado no código/testes | Reservas iniciam SOLICITADA; a transição para APROVADA verifica recurso principal e professor. Se algum for restrito, somente o perfil RESPONSAVEL pode aprovar. Teste de integração cobre administrador bloqueado e responsável autorizado. |
| RF-08 | Bloqueio de recursos em manutenção | Implementado no código/testes | Serviço rejeita nova reserva quando `emManutencao` está ativo; há teste de integração/concorrência. |
| RF-09 | Registro de retirada e devolução de materiais/equipamentos | Implementado no código/testes | Entidade `MovimentacaoMaterial`, endpoints POST de retirada/devolução e serviço transacional; retirada exige reserva aprovada, devolução conclui reserva. Testes de integração com PostgreSQL verificam o ciclo e a proibição de retirada pendente. |
| RF-10 | Histórico auditável de mudanças | Implementado no código/testes | ReservaAuditoria registra criação, mudanças de estado, cancelamento e alteração, incluindo login; retirada/devolução gera transições auditadas. `AuditoriaAdministrativa` + Flyway V5 registram cadastro/edição de recursos e criação/ativação de usuários sem senhas. E2E verifica acesso somente de ADMINISTRADOR. |
| RF-11 | Notificação simulada ou integração com API externa | Implementado no código/testes | Notificações são persistidas como SIMULADO; webhook HTTP opcional com variável `NOTIFICACAO_URL` e testes WireMock de sucesso/falha. Solicitação, transição e cancelamento geram eventos consultáveis por responsáveis/administradores. |
| RF-12 | Relatório de utilização, carga horária e conflitos evitados | Implementado no código/testes | `GET /relatorios/utilizacao` calcula reservas e minutos alocados por recurso no intervalo; `GET /relatorios/conflitos` consulta tentativas recusadas persistidas em transação independente. Testes de integração cobrem minutos alocados e persistência do conflito. |
| RF-13 | Interface responsiva e mensagens compreensíveis | Implementado no código; validação visual pendente | Interface HTML/CSS adapta formulários ao celular, oferece rolagem de tabelas, indicadores de foco, tamanhos de toque e tratamento legível de erros 401/403/404/409/422. Há telas de recursos, reservas, usuários, relatórios e notificações. E2E verifica entrega do HTML; falta executar checklist visual `docs/TESTE-RESPONSIVIDADE.md`. |
| RF-14 | Documentação de API ou fluxos públicos | Implementado no código/testes | OpenAPI JSON em `/v3/api-docs`, Swagger UI em `/swagger-ui/index.html`, README, `docs/EXEMPLOS-API.md` e diagramas de fluxos. Teste E2E confirma resposta de contrato e caminhos documentados; validação visual do Swagger fica para demonstração. |


## Rastreabilidade requisito → risco → teste → evidência

| Requisito | Risco principal | Teste automatizado / manual | Evidência versionada |
|---|---|---|---|
| RF-01 | Acesso indevido e escalonamento de privilégio | `AutorizacaoApiTest`, `FluxoCompletoE2ETest`; testar login real e bloqueio de endpoints | `src/main/java/com/senac/recursos/config/SegurancaConfig.java`, `docs/ADR-001-SEGURANCA.md` |
| RF-02 | Cadastro inconsistente e recurso duplicado | `RecursoControllerDuplicidadeTest`, `ReservaServiceIntegracaoTest`; cadastrar SALA/PROFESSOR/MATERIAL | `Recurso.java`, `RecursoController.java` |
| RF-03 | Oferecer recurso indisponível ou incompatível | `ReservaServiceIntegracaoTest.pesquisaRecursosPorTipoCapacidadeLocalizacaoECompetencia` e `FluxoCompletoE2ETest` (livre, ocupado, adjacente) | `RecursoRepository.java`, formulário de pesquisa em `static/index.html`, GitHub Actions |
| RF-04 | Alteração/cancelamento causar sobreposição ou violar titularidade | `ReservaServiceIntegracaoTest.titularPodeAlterarPeriodoEAlteracaoGeraAuditoria`, `reagendamentoTambemVerificaConflitoDeHorario`, testes de cancelamento | `ReservaService.java`, `ReservaController.java` |
| RF-05 | Professor alocado em duas salas no mesmo período | `ReservaServiceIntegracaoTest.professorVinculadoNaoPodeSerAlocadoEmDuasSalasNoMesmoHorario` | `Reserva.java`, `ReservaRepository.java` |
| RF-06 | Dupla reserva sob concorrência | `ReservaConcorrenciaTest.solicitacoesSimultaneasProduzemUmaUnicaReserva`; JMeter executado com 20 threads (1 resposta 201, 19 respostas 409) no CI 42711044633 | `ReservaService.java`, `jmeter/reservas-concorrentes.jmx` |
| RF-07 | Recurso restrito utilizado sem autorização | `ReservaServiceIntegracaoTest.somenteResponsavelPodeAprovarReservaDeRecursoRestrito`; validar papel RESPONSAVEL na UI/API | `ReservaService.java`, `SegurancaConfig.java` |
| RF-08 | Reserva durante manutenção | `ReservaConcorrenciaTest.naoPermiteReservarRecursoEmManutencao` | `ReservaService.java` |
| RF-09 | Material não devolvido ou retirada sem aprovação | `ReservaServiceIntegracaoTest.registraRetiradaEDevolucaoDeMaterialComHistoricoDeEstados`, `impedeRetiradaDeReservaQueNaoEstejaAprovada` | `MovimentacaoMaterialService.java`, migração V3 |
| RF-10 | Alteração sem trilha de auditoria | Testes de auditoria de criação, transição, cancelamento e reagendamento | `ReservaAuditoria.java`, migração V4 |
| RF-11 | Falha de notificação não percebida | `ReservaServiceIntegracaoTest.notificacoesSimuladasSaoGeradasNaSolicitacaoETransicao`, `NotificacaoServiceWireMockTest` | `NotificacaoService.java`, `NotificacaoSimulada.java` |
| RF-12 | Indicadores de utilização/conflitos incorretos | `ReservaServiceIntegracaoTest.relatorioCalculaMinutosAlocadosNoIntervaloSolicitado`, `conflitoRecusadoFicaRegistradoMesmoComRollbackDaReserva` | `RelatorioService.java`, `ConflitoEvitadoService.java` |
| RF-13 | Interface difícil de usar em celular ou erro incompreensível | Checklist manual em viewport 360px, 768px e desktop; executar fluxo de erro | `src/main/resources/static/index.html` |
| RF-14 | Contrato público ambíguo | Abrir Swagger UI e validar schemas/exemplos | `OpenApiConfig.java`, Springdoc e README |

## Estratégia obrigatória de qualidade

| Critério do enunciado | Status observado | Evidência / ação necessária |
|---|---|---|
| Testes unitários JUnit 5 | Parcial/implementado | Existem testes de domínio e serviço; revisar cobertura e cenários de borda. |
| Testes parametrizados | Implementado | `ReservaControllerTest` parametriza entradas HTTP inválidas; `UsuarioServiceTest` cobre diferentes entradas de cadastro com JUnit 5 `@ParameterizedTest`. |
| Testes de integração | Implementado no código | Testcontainers com PostgreSQL real; validar execução final. |
| Testes de API em caixa-preta | Implementado para API | `FluxoCompletoE2ETest` usa TestRestTemplate contra servidor iniciado e PostgreSQL real; `AutorizacaoApiTest` cobre 401/403. Não substitui teste de navegador. |
| Testes end-to-end | Implementado para fluxo API | `FluxoCompletoE2ETest` percorre criação de recursos/usuários, reserva, autorização, aprovação, auditoria, relatório e notificação com aplicação iniciada e PostgreSQL Testcontainers. Ainda não é teste de navegador. |
| Testcontainers | Implementado no código | PostgreSQL containerizado nos testes de integração/concorrência. |
| WireMock | Implementado | Webhook de notificação opcional é testado com WireMock; sem URL configurada, o sistema registra notificação simulada. |
| Teste de concorrência | Implementado no código/testes | Teste automatizado para solicitações simultâneas do mesmo recurso. |
| TDD/BDD com evidências | Documentado | `docs/EVIDENCIA-BDD.md` reúne critérios Dado/Quando/Então, mapeamento a testes E2E e links de falha/correção/sucesso no CI do RF-03. Não afirma que todo o projeto foi construído por TDD. |
| GitHub Actions em pull requests | Implementado | Workflow executa em pull_request e push nas branches configuradas. |
| JaCoCo | Metas atingidas | GitHub Actions [42708687473](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42708687473) mediu 87,81% de linhas e 71,64% de branches, com 115 testes aprovados. O `pom.xml` contém regra de check para bloquear regressões abaixo de 80%/70%; validar no SHA final. |
| SonarCloud | Parcial | Workflow e `sonar-project.properties` estão preparados; falta cadastrar SONAR_TOKEN, SONAR_ORGANIZATION e SONAR_PROJECT_KEY e executar análise real. |
| JMeter | Evidência aprovada | GitHub Actions [42711044633](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42711044633): 20 solicitações concorrentes, 1 HTTP 201, 19 HTTP 409, latência média 106,9 ms, P95 209 ms. JTL, HTML e resumo publicados como artefatos. |
| Autorização, validação e erros seguros | Implementado no código/testes | HTTP Basic + BCrypt + RBAC e autorização por titularidade testada em serviço e E2E; mensagens HTTP compreensíveis. Falta revisão externa de segurança para garantir a meta de zero vulnerabilidades críticas. |

## Metas mensuráveis do enunciado

- [x] Cobertura mínima de 80% de linhas — 87,81% no CI 42708687473.
- [x] Cobertura mínima de 70% de branches — 71,64% no CI 42708687473.
- [x] Todos os 14 requisitos funcionais rastreados nesta RTM, com risco, teste e evidência.
- [ ] Zero bugs ou vulnerabilidades críticas, comprovados por análise e triagem.

## Entregáveis do enunciado

- [x] Repositório Git com histórico de trabalho.
- [x] Aplicação e instruções iniciais de instalação (validar demonstração em ambiente limpo).
- [x] README e documentação técnica inicial.
- [x] RTM com requisito, risco, teste e evidência (itens parciais e pendentes estão identificados).
- [x] Diagramas de contexto, contêineres, componentes e sequência das operações críticas.
- [x] Plano e suíte automatizada; relatório final depende dos resultados de CI/cobertura/JMeter/SonarCloud.
- [x] Pipeline de integração contínua.
- [ ] Relatórios SonarCloud e JaCoCo com metas atingidas.
- [x] Plano e resultado do teste de carga JMeter (execução 42711044633; evidências anexadas).
- [x] Registro priorizado de defeitos e riscos.
- [ ] Relatório final de qualidade (há relatório candidato; finalizar após evidências reais).
- [x] Roteiro de apresentação e demonstração (slides finais/capturas devem ser montados).

## Critério para declarar pronto

Os 14 requisitos funcionais têm implementação no código e testes associados; RF-13 e RF-14 ainda exigem validação visual na apresentação. Não declarar a entrega acadêmica integralmente concluída até SonarCloud, revisão de segurança, evidências visuais finais e apresentação estarem conferidos. O enunciado oficial é a fonte de verdade para a aceitação.
