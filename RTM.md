# RTM — Matriz de Rastreabilidade de Requisitos

Fonte: enunciado oficial "Especificação do Projeto — Qualidade 2026.2", seção "Requisitos funcionais obrigatórios" (itens 1 a 14).

**Regra de status:** "Implementado" exige código e evidência verificável; "Parcial" significa que apenas parte do critério está coberta; "Pendente" significa que não foi encontrada implementação/evidência suficiente nesta branch. A aprovação final depende também de demonstração e dos critérios de qualidade do enunciado.

## Requisitos funcionais

| ID | Requisito oficial (síntese fiel) | Status observado | Evidência atual / lacuna |
|---|---|---|---|
| RF-01 | Autenticação e autorização por perfil | Parcial | Spring Security HTTP Basic, usuários persistidos, BCrypt, perfis SOLICITANTE/RESPONSAVEL/ADMINISTRADOR, bootstrap do primeiro administrador por variáveis de ambiente e regras por endpoint. Testes MockMvc cobrem 401/403 e autenticação persistida. Falta revisar autorização por propriedade da reserva e validar o fluxo operacional da interface. |
| RF-02 | Cadastro e consulta de salas, professores e materiais | Parcial | `TipoRecurso` contempla SALA, PROFESSOR e MATERIAL; `RecursoController` permite listar, cadastrar e atualizar. Foram adicionados capacidade, localização e competências. Ainda faltam gestão de usuários e regras específicas por categoria. |
| RF-03 | Pesquisa por tipo, capacidade, localização, competência e disponibilidade | Implementado no código/testes | `GET /recursos` aceita filtros por tipo, capacidade, localização, competência e disponibilidade. `ReservaServiceIntegracaoTest` cobre metadados; `FluxoCompletoE2ETest` testa filtros e consulta HTTP de horários livres, ocupados e adjacentes. Formulário de pesquisa disponível na interface. Validação visual manual permanece pendente no RF-13. |
| RF-04 | Criação, alteração e cancelamento de reservas | Implementado no código/testes | Criação, reagendamento de reserva SOLICITADA e cancelamento verificam titularidade/papel, revalidam conflitos e registram auditoria. Testes de integração cobrem reagendamento, conflito e titularidade. |
| RF-05 | Sobreposição de horários, inclusive na agenda do professor | Implementado no código/testes | Reserva pode vincular professor; o serviço bloqueia recurso principal e professor em ordem de ID e consulta conflitos em ambos. Teste de integração cobre duas salas tentando usar o mesmo professor no mesmo horário. |
| RF-06 | Proteção contra dupla reserva em solicitações simultâneas | Implementado no código/testes | Bloqueio pessimista na linha do recurso antes da consulta de conflitos; teste de concorrência com Testcontainers. Revalidar no CI da revisão final. |
| RF-07 | Aprovação obrigatória para recursos restritos | Implementado no código/testes | Reservas iniciam SOLICITADA; a transição para APROVADA verifica recurso principal e professor. Se algum for restrito, somente o perfil RESPONSAVEL pode aprovar. Teste de integração cobre administrador bloqueado e responsável autorizado. |
| RF-08 | Bloqueio de recursos em manutenção | Implementado no código/testes | Serviço rejeita nova reserva quando `emManutencao` está ativo; há teste de integração/concorrência. |
| RF-09 | Registro de retirada e devolução de materiais/equipamentos | Implementado no código/testes | Entidade `MovimentacaoMaterial`, endpoints POST de retirada/devolução e serviço transacional; retirada exige reserva aprovada, devolução conclui reserva. Testes de integração com PostgreSQL verificam o ciclo e a proibição de retirada pendente. |
| RF-10 | Histórico auditável de mudanças | Parcial | Auditoria registra criação, transições, cancelamento e reagendamento, com login autenticado e detalhes do evento. Retirada/devolução também gera transições auditadas. Falta auditar alterações cadastrais de recursos/usuários. |
| RF-11 | Notificação simulada ou integração com API externa | Implementado como simulação | Eventos de solicitação, mudança de estado e cancelamento geram registros persistidos em `notificacoes_simuladas`; consulta é restrita a responsável/administrador. Teste de integração cobre solicitação e transição. Não há integração externa; WireMock fica não aplicável enquanto o canal for simulado. |
| RF-12 | Relatório de utilização, carga horária e conflitos evitados | Implementado no código/testes | `GET /relatorios/utilizacao` calcula reservas e minutos alocados por recurso no intervalo; `GET /relatorios/conflitos` consulta tentativas recusadas persistidas em transação independente. Testes de integração cobrem minutos alocados e persistência do conflito. |
| RF-13 | Interface responsiva e mensagens compreensíveis | Parcial | Há interface web e tratamento de erros HTTP com mensagens em português. Responsividade precisa ser validada em diferentes larguras e documentada com evidência. |
| RF-14 | Documentação de API ou fluxos públicos | Parcial | README e documentação técnica descrevem endpoints/fluxos. Foi adicionada dependência Springdoc OpenAPI; validar Swagger UI em `/swagger-ui/index.html` e complementar exemplos/descrições dos endpoints. |


## Rastreabilidade requisito → risco → teste → evidência

| Requisito | Risco principal | Teste automatizado / manual | Evidência versionada |
|---|---|---|---|
| RF-01 | Acesso indevido e escalonamento de privilégio | `AutorizacaoApiTest`, `FluxoCompletoE2ETest`; testar login real e bloqueio de endpoints | `src/main/java/com/senac/recursos/config/SegurancaConfig.java`, `docs/ADR-001-SEGURANCA.md` |
| RF-02 | Cadastro inconsistente e recurso duplicado | `RecursoControllerDuplicidadeTest`, `ReservaServiceIntegracaoTest`; cadastrar SALA/PROFESSOR/MATERIAL | `Recurso.java`, `RecursoController.java` |
| RF-03 | Oferecer recurso indisponível ou incompatível | `ReservaServiceIntegracaoTest.pesquisaRecursosPorTipoCapacidadeLocalizacaoECompetencia` e `FluxoCompletoE2ETest` (livre, ocupado, adjacente) | `RecursoRepository.java`, formulário de pesquisa em `static/index.html`, GitHub Actions |
| RF-04 | Alteração/cancelamento causar sobreposição ou violar titularidade | `ReservaServiceIntegracaoTest.titularPodeAlterarPeriodoEAlteracaoGeraAuditoria`, `reagendamentoTambemVerificaConflitoDeHorario`, testes de cancelamento | `ReservaService.java`, `ReservaController.java` |
| RF-05 | Professor alocado em duas salas no mesmo período | `ReservaServiceIntegracaoTest.professorVinculadoNaoPodeSerAlocadoEmDuasSalasNoMesmoHorario` | `Reserva.java`, `ReservaRepository.java` |
| RF-06 | Dupla reserva sob concorrência | `ReservaConcorrenciaTest.solicitacoesSimultaneasProduzemUmaUnicaReserva`; JMeter planejado | `ReservaService.java`, `jmeter/reservas-concorrentes.jmx` |
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
| Testes parametrizados | Pendente de comprovação | Não marcar como atendido até localizar testes parametrizados explícitos e úteis. |
| Testes de integração | Implementado no código | Testcontainers com PostgreSQL real; validar execução final. |
| Testes de API em caixa-preta | Implementado para API | `FluxoCompletoE2ETest` usa TestRestTemplate contra servidor iniciado e PostgreSQL real; `AutorizacaoApiTest` cobre 401/403. Não substitui teste de navegador. |
| Testes end-to-end | Implementado para fluxo API | `FluxoCompletoE2ETest` percorre criação de recursos/usuários, reserva, autorização, aprovação, auditoria, relatório e notificação com aplicação iniciada e PostgreSQL Testcontainers. Ainda não é teste de navegador. |
| Testcontainers | Implementado no código | PostgreSQL containerizado nos testes de integração/concorrência. |
| WireMock | Implementado | Webhook de notificação opcional é testado com WireMock; sem URL configurada, o sistema registra notificação simulada. |
| Teste de concorrência | Implementado no código/testes | Teste automatizado para solicitações simultâneas do mesmo recurso. |
| TDD/BDD com evidências | Pendente de comprovação | Histórico de commits isoladamente não comprova ciclo red-green-refactor nem cenário BDD documentado. |
| GitHub Actions em pull requests | Implementado | Workflow executa em pull_request e push nas branches configuradas. |
| JaCoCo | Parcial | Relatório é gerado e publicado como artefato; metas mínimas de 80% linhas e 70% branches ainda precisam ser configuradas e comprovadas. |
| SonarCloud | Parcial | Workflow e `sonar-project.properties` estão preparados; falta cadastrar SONAR_TOKEN, SONAR_ORGANIZATION e SONAR_PROJECT_KEY e executar análise real. |
| JMeter | Parcial | Plano e script concorrente em `docs/PLANO-TESTE-CARGA.md` e `jmeter/reservas-concorrentes.jmx`; falta executar em ambiente controlado e anexar JTL/relatório com métricas. |
| Autorização, validação e erros seguros | Parcial | HTTP Basic com BCrypt e autorização por papel estão implementados; validação de entrada e mapeamento de erros existentes. Falta teste completo de autorização por propriedade/fluxo e revisão de segurança independente. |

## Metas mensuráveis do enunciado

- [ ] Cobertura mínima de 80% de linhas.
- [ ] Cobertura mínima de 70% de branches.
- [ ] 100% dos requisitos críticos rastreados na RTM.
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
- [ ] Plano e resultado do teste de carga JMeter.
- [x] Registro priorizado de defeitos e riscos.
- [ ] Relatório final de qualidade (há relatório candidato; finalizar após evidências reais).
- [x] Roteiro de apresentação e demonstração (slides finais/capturas devem ser montados).

## Critério para declarar pronto

Não declarar o projeto concluído enquanto RF-01, RF-02, RF-10, RF-13 e RF-14 permanecerem parciais, as metas de qualidade não forem demonstradas e os entregáveis obrigatórios não estiverem reunidos. O enunciado oficial é a fonte de verdade para a aceitação.
