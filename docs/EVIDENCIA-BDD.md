# BDD — cenários de aceitação e evidências

Técnica: critérios de aceitação Dado / Quando / Então associados a testes que executam a aplicação real. A evolução do RF-03 possui uma falha registrada seguida de correção e nova validação.

## RF-03 — disponibilidade

**Dado** que uma sala esteja livre das 10h às 11h, **quando** o solicitante pesquisar disponibilidade, **então** ela aparece na lista.

**Dado** que uma reserva ocupe o mesmo período, **quando** repetir a pesquisa, **então** a sala não aparece.

**Dado** que a reserva termine às 11h, **quando** pesquisar das 11h às 12h, **então** a sala aparece porque períodos adjacentes não conflitam.

Teste: `FluxoCompletoE2ETest.percorreCadastroSolicitacaoAprovacaoAuditoriaRelatorioENotificacao`, com HTTP Basic e PostgreSQL Testcontainers.

### Falha → correção → aprovação

- Novo teste HTTP de disponibilidade incluído no commit `981bc607`.
- GitHub Actions detectou falha no cenário de localização (esperado 1, recebido 0): https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42704827362
- Correção de envio do filtro de localização no commit `c5b2fe1`.
- Execução posterior com Maven e testes aprovados: https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42705420817

Essa sequência comprova uso de testes para descobrir e corrigir um defeito; não comprova que toda a funcionalidade foi criada originalmente por TDD puro.

## RF-01 — privacidade

**Dado** que Ana e Bia tenham reservas, **quando** Ana consultar GET /reservas, **então** verá apenas as próprias reservas.

**Dado** uma reserva pertencente a Bia, **quando** Ana consultar GET /reservas/{id}/auditoria, **então** receberá HTTP 404.

**Dado** perfil RESPONSAVEL ou ADMINISTRADOR, **quando** consultar a listagem, **então** poderá ver todas as reservas.

Testes: `ReservaServiceIntegracaoTest.solicitanteVisualizaSomenteSuasReservasEHistoricos` e `FluxoCompletoE2ETest`.

## RF-10 — auditoria administrativa

**Dado** um administrador autenticado, **quando** cadastrar ou alterar recurso e cadastrar usuário, **então** serão registrados entidade, ação, identificador, data e login.

**Dado** um solicitante, **quando** consultar GET /auditoria-administrativa, **então** a API responderá HTTP 403.

**Dado** uma alteração de usuário, **quando** consultar o registro, **então** o evento não conterá senha ou hash.

Teste: `FluxoCompletoE2ETest`. Evidência de persistência: `AuditoriaAdministrativa`, migração Flyway V5.

## Aceitação

A execução de CI no SHA final precisa passar. As evidências de falha/correção e mapeamento de testes são parte do relatório de qualidade, mas não substituem demonstração e revisão da equipe.
