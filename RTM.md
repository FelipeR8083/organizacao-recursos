# RTM — Matriz de Rastreabilidade de Requisitos

Fonte: enunciado oficial "Especificação do Projeto — Qualidade 2026.2", seção "Requisitos funcionais obrigatórios" (itens 1 a 14).

**Regra de status:** "Implementado" exige código e evidência verificável; "Parcial" significa que apenas parte do critério está coberta; "Pendente" significa que não foi encontrada implementação/evidência suficiente nesta branch. A aprovação final depende também de demonstração e dos critérios de qualidade do enunciado.

## Requisitos funcionais

| ID | Requisito oficial (síntese fiel) | Status observado | Evidência atual / lacuna |
|---|---|---|---|
| RF-01 | Autenticação e autorização por perfil | Pendente | Não há Spring Security, login ou papéis Solicitante/Responsável/Administrador. Não expor a aplicação publicamente. |
| RF-02 | Cadastro e consulta de salas, professores e materiais | Parcial | `TipoRecurso` contempla SALA, PROFESSOR e MATERIAL; `RecursoController` permite listar, cadastrar e atualizar recursos. Faltam regras/campos específicos e gestão de usuários/bloqueios. |
| RF-03 | Pesquisa por tipo, capacidade, localização, competência e disponibilidade | Pendente | Não foram encontrados filtros/campos de capacidade, localização ou competência nem pesquisa de disponibilidade. |
| RF-04 | Criação, alteração e cancelamento de reservas | Parcial | Há criação e transição de estado; não há operação completa para alterar período/recurso nem fluxo de cancelamento com regras de negócio dedicado. |
| RF-05 | Sobreposição de horários, inclusive na agenda do professor | Parcial | A consulta de conflito verifica sobreposição para o mesmo recurso. Não há evidência de validação cruzada entre professor e sala vinculados à mesma reserva. |
| RF-06 | Proteção contra dupla reserva em solicitações simultâneas | Implementado no código/testes | Bloqueio pessimista na linha do recurso antes da consulta de conflitos; teste de concorrência com Testcontainers. Revalidar no CI da revisão final. |
| RF-07 | Aprovação obrigatória para recursos restritos | Pendente | O atributo `restrito` existe, mas não há autorização por perfil nem regra que exija aprovação de Responsável antes de confirmar a reserva. |
| RF-08 | Bloqueio de recursos em manutenção | Implementado no código/testes | Serviço rejeita nova reserva quando `emManutencao` está ativo; há teste de integração/concorrência. |
| RF-09 | Registro de retirada e devolução de materiais/equipamentos | Pendente | Não foram encontrados entidades, endpoints ou fluxo de retirada/devolução. |
| RF-10 | Histórico auditável de mudanças | Parcial | Auditoria persistente de criação/transições e consulta por API/interface. A origem registrada é técnica (`API`); falta identificar o usuário real após autenticação e ampliar eventos para demais operações. |
| RF-11 | Notificação simulada ou integração com API externa | Pendente | Não foram encontrados adaptador de notificação, simulação ou testes WireMock. |
| RF-12 | Relatório de utilização, carga horária e conflitos evitados | Pendente | Não foram encontrados endpoints/visões de relatórios ou agregações correspondentes. |
| RF-13 | Interface responsiva e mensagens compreensíveis | Parcial | Há interface web e tratamento de erros HTTP com mensagens em português. Responsividade precisa ser validada em diferentes larguras e documentada com evidência. |
| RF-14 | Documentação de API ou fluxos públicos | Parcial | README e documentação técnica descrevem endpoints/fluxos. Falta contrato formal OpenAPI/Swagger e exemplos completos de request/response. |

## Estratégia obrigatória de qualidade

| Critério do enunciado | Status observado | Evidência / ação necessária |
|---|---|---|
| Testes unitários JUnit 5 | Parcial/implementado | Existem testes de domínio e serviço; revisar cobertura e cenários de borda. |
| Testes parametrizados | Pendente de comprovação | Não marcar como atendido até localizar testes parametrizados explícitos e úteis. |
| Testes de integração | Implementado no código | Testcontainers com PostgreSQL real; validar execução final. |
| Testes de API em caixa-preta | Parcial | MockMvc testa contrato HTTP dentro da aplicação; falta evidência de suíte black-box externa contra aplicação iniciada. |
| Testes end-to-end | Pendente | Não foi encontrada suíte E2E de navegador/fluxo completo. |
| Testcontainers | Implementado no código | PostgreSQL containerizado nos testes de integração/concorrência. |
| WireMock | Pendente | Não há integração externa implementada nem testes WireMock. |
| Teste de concorrência | Implementado no código/testes | Teste automatizado para solicitações simultâneas do mesmo recurso. |
| TDD/BDD com evidências | Pendente de comprovação | Histórico de commits isoladamente não comprova ciclo red-green-refactor nem cenário BDD documentado. |
| GitHub Actions em pull requests | Implementado | Workflow executa em pull_request e push nas branches configuradas. |
| JaCoCo | Parcial | Relatório é gerado e publicado como artefato; metas mínimas de 80% linhas e 70% branches ainda precisam ser configuradas e comprovadas. |
| SonarCloud | Pendente | Não foi encontrada configuração/projeto integrado nem relatório. |
| JMeter | Pendente | Não há plano, script ou resultado de teste de carga. |
| Autorização, validação e erros seguros | Parcial | Há validação de entrada e mapeamento de erros; autorização por perfil está ausente. |

## Metas mensuráveis do enunciado

- [ ] Cobertura mínima de 80% de linhas.
- [ ] Cobertura mínima de 70% de branches.
- [ ] 100% dos requisitos críticos rastreados na RTM.
- [ ] Zero bugs ou vulnerabilidades críticas, comprovados por análise e triagem.

## Entregáveis do enunciado

- [x] Repositório Git com histórico de trabalho.
- [x] Aplicação e instruções iniciais de instalação (validar demonstração em ambiente limpo).
- [x] README e documentação técnica inicial.
- [ ] RTM completa com requisito, risco, teste e evidência (esta matriz é a base; completar risco/teste/evidência por requisito).
- [x] Diagramas de contexto e contêineres; faltam diagramas de sequência das operações críticas.
- [ ] Plano, suíte automatizada e relatório final de testes.
- [x] Pipeline de integração contínua.
- [ ] Relatórios SonarCloud e JaCoCo com metas atingidas.
- [ ] Plano e resultado do teste de carga JMeter.
- [ ] Registro priorizado de defeitos encontrados e corrigidos.
- [ ] Relatório final de qualidade.
- [ ] Apresentação e roteiro de demonstração.

## Critério para declarar pronto

Não declarar o projeto concluído enquanto RF-01, RF-03, RF-04, RF-05, RF-07, RF-09, RF-11 e RF-12 permanecerem pendentes/parciais, as metas de qualidade não forem demonstradas e os entregáveis obrigatórios não estiverem reunidos. O enunciado oficial é a fonte de verdade para a aceitação.
