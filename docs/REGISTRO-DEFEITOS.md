# Registro de defeitos e riscos

Este registro consolida problemas encontrados durante a revisão do código. **As linhas mais antigas são históricas**; as informações de status mais recentes estão na seção de atualização de 12/11/2026 abaixo. O CI da `main` no commit `60862cd8` foi aprovado ([run 43304807488](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43304807488)).

| ID | Prioridade | Problema observado | Correção nesta branch | Estado |
|---|---|---|---|---|
| DEF-01 | Crítica | API sem autenticação/autorização | Spring Security, BCrypt, papéis e bootstrap administrativo; testes de 401/403 e HTTP Basic | Corrigido no código; CI pendente |
| DEF-02 | Alta | Reserva de sala não vinculava professor, permitindo conflito cruzado na agenda docente | Professor vinculado à reserva, bloqueio ordenado de ambos os recursos e teste de concorrência cruzada | Corrigido no código; CI pendente |
| DEF-03 | Alta | Auditoria registrava apenas canal técnico e não distinguia alteração de reserva | Login autenticado como origem; tipo/detalhes de evento para reagendamento; auditoria em transições e movimentações | Parcial; auditar CRUD de recursos/usuários |
| DEF-04 | Alta | Schema dependia de Hibernate ddl-auto update, sem histórico versionado | Flyway V1–V4 e Hibernate ddl-auto validate | Corrigido no código; validar migração em banco existente com dados |
| DEF-05 | Alta | Não havia registro de retirada/devolução de material | Entidade, serviço, endpoints e testes com Testcontainers | Corrigido no código; CI pendente |
| DEF-06 | Média | Ausência de notificação e relatório de utilização/conflitos | Notificação simulada/webhook opcional com teste WireMock; relatórios e testes de integração | Corrigido no código; CI pendente |
| DEF-07 | Alta | Não havia reagendamento com nova checagem de conflitos | PUT de reserva SOLICITADA com verificação de titularidade, conflito, auditoria e teste | Corrigido no código; CI pendente |
| DEF-08 | Média | Pesquisa de recursos não tinha metadados/filtros | Capacidade, localização, competências e disponibilidade | Parcial; validar filtros na interface e HTTP |
| RSK-01 | Alta | Metas 80% linhas / 70% branches no commit final | CI 43107077159 comprovou 91,26% e 75,61%; exigir nova rodada após mudanças | Atingido na versão anterior; rever no SHA final |
| RSK-02 | Alta | SonarCloud depende de token e variáveis da equipe | Etapa condicional no workflow | Aberto até configurar SONAR_TOKEN, SONAR_ORGANIZATION e SONAR_PROJECT_KEY |
| RSK-03 | Média | Teste de carga deve ser comprovado | GitHub Actions 42711044633: 20 requisições, 1 HTTP 201 e 19 HTTP 409; artefatos JTL/HTML | Evidência disponível; anexar na entrega |
| RSK-04 | Média | Responsividade ainda não possui evidência em tamanhos/dispositivos | CSS responsivo existente | Aberto até executar checklist visual |
| RSK-05 | Média | Teste E2E cobre fluxo HTTP completo, não navegação real do navegador | TestRestTemplate + PostgreSQL Testcontainers | Parcial; E2E de navegador não implementado |
| DEF-09 | Crítica | Administrador conseguia aprovar recurso restrito, enquanto RESPONSAVEL recebia 403 | Corrigidas as permissões em SecurityConfig, controlador e interface; professor, sala e materiais restritos exigem RESPONSAVEL | Corrigido no código; validar CI e manual |
| DEF-10 | Alta | Reserva de sala com vários materiais não tinha retirada/devolução individual | Movimentação por (reserva, material), API e botões por material; migração Flyway V9 | Corrigido no código; validar migração sobre banco restaurado e CI |
| DEF-11 | Média | Relatório ignorava materiais adicionais da sala | RelatorioService considera cada material vinculado; teste de regressão | Corrigido; confirmado manualmente na homologação 8084 (1 reserva/60 minutos) |
| DEF-12 | Baixa | Professor via apenas "Sim" no campo manutenção | Etiqueta visível "Indisponível — em manutenção" sem expor ações administrativas | Corrigido no código; validação visual pendente |

## Política de triagem

- Crítica: bloqueia autenticação, integridade ou dupla reserva; corrigir antes de qualquer demonstração pública.
- Alta: impede requisito funcional ou pode gerar alocação incorreta; corrigir antes da entrega.
- Média: afeta evidência, observabilidade, usabilidade ou meta de qualidade; registrar plano e resultado.
- Não marcar risco como fechado apenas por existir um commit: exigir teste verde e evidência de execução.

## Atualização de homologação — 12/11/2026

| ID | Prioridade | Evidência observada | Decisão / estado |
|---|---|---|---|
| DEF-13 | Alta | O botão CONCLUIDA era exibido durante movimentações de materiais ainda não devolvidos; o endpoint manual de conclusão não conferia os movimentos. | **Corrigido em código e testes automatizados:** `ReservaService.alterarEstado` bloqueia conclusão com materiais pendentes; interface oculta botão, testes para reserva conjunta e material principal; CI [43279334663](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43279334663) verde. Revalidar na instalação manual após rebuild. |
| RSK-05 | Média | Foi possível iniciar e encerrar no dia 12/11 uma reserva marcada para 13/11. | **Risco registrado**, sem correção nesta versão. Requer definição da regra temporal com o docente/equipe antes de alterar comportamento. |
| RSK-06 | Alta | Flyway V9 executou em banco novo, ainda não testado em cópia de V8 com dados. | **Em aberto** até ensaio de migração com backup; não atualizar produção sem plano. |
| RSK-07 | Alta | SonarCloud não executou; relatório final de segurança e Quality Gate indisponíveis. | **Em aberto**; análise estática não substituída pelo resultado dos testes/JaCoCo. |

**Relatório atualizado:** `docs/RELATORIO-QUALIDADE.md`. A homologação na porta 8085 validou as movimentações e manutenção, sem alterar a porta 8080 original.

## Atualização SonarCloud e CSRF — 12/11/2026 (após merge PR #4)

| ID | Prioridade | Evidência | Estado |
|---|---|---|---|
| DEF-14 | Alta | SonarCloud inicialmente identificou duas instâncias de `java:S4502`: CSRF desabilitado no perfil padrão e ignorado em rotas de login/cadastro/confirmação. PR #4 removeu desativação e exceções, ativando CSRF universal e ajustando cliente e testes. | **Corrigido e integrado à `main`**; 185 testes aprovados e login/logout na homologação :8085. Nova análise SonarCloud aprovada no Quality Gate; triagem global dos alertas pendente. |
| DEF-15 | Baixa | SonarCloud alertou o uso de cookie CSRF sem `HttpOnly`. PR #4 substituiu o repositório de token em cookie pelo repositório padrão da sessão. | **Corrigido no código**; nova análise da `main` teve 0 Security Hotspots **no código novo**, não sendo prova de 0 hotspots no histórico inteiro. |
| RSK-02 / RSK-07 | Alta | Check `SonarCloud Code Analysis` no merge `60862cd8`: Quality Gate **aprovado**, **6 questões novas**, **0,0% cobertura do código novo no SonarCloud**. A etapa SonarCloud do GitHub Actions permanece `skipped`. | **Análise estática executada**, mas **triagem das 6 questões** e **integração JaCoCo** ainda pendentes. Separadamente, JaCoCo do CI: 91,73% linhas, 77,55% branches. |
| RSK-08 | Média | JMeter anterior demonstrou 20 threads com 1 HTTP 201 e 19 HTTP 409, mas era anterior ao CSRF obrigatório. Plano JMX atualizado exige token e cookie por thread. | **Reexecução de carga pós-CSRF pendente**; não atribuir métricas antigas à implementação nova. |
| RSK-09 | Média | Login, logout e `GET /csrf` foram validados em :8085; um POST sem token e sem autenticação retornou 401. | **Não registrar o 401 como prova de CSRF**. Testes automatizados com usuários autenticados validam HTTP 403 para mutações sem token; sem novas provas manuais. |

## Triagem das 6 questões do SonarCloud — 12/11/2026

| ID | Local | Diagnóstico | Ação |
|---|---|---|---|
| SONAR-01 | `EmailConfirmacaoProfessorService.java:39` | Regex para remover barras finais da URL pode ter custo superlinear | **Corrigido na branch `correcao/sonar-confiabilidade-20261112`** por varredura linear |
| SONAR-02 | `UsuarioService.java:63` | Regex da parte local do e-mail pode causar estouro de pilha com repetição | **Corrigido na branch** por validação iterativa |
| SONAR-03 | `UsuarioService.java:65` | Regex de domínio pode causar estouro de pilha com repetição | **Corrigido na branch** por validação iterativa |
| SONAR-04 | `UsuarioService.java:65` | Mesmo regex de domínio pode ter backtracking superlinear | **Corrigido na branch** por validação iterativa |
| SONAR-05 | `V6__verificacao_email_professor.sql:10` | Recomenda usar `VARCHAR2` no lugar de `CHAR`. `VARCHAR2` é tipo específico de Oracle, mas a aplicação usa PostgreSQL 16; `CHAR(64)` representa hash hexadecimal fixo SHA-256 | **Não alterar migração Flyway V6 já aplicada**: modificar arquivo antigo alteraria checksum e pode inviabilizar a atualização de bancos existentes. Revisar classificação de regra SQL/false positive no Sonar com justificativa. |
| SONAR-06 | `login.html:54` | `autofocus` pode prejudicar navegação por leitores de tela | **Corrigido na branch**, atributo removido |

**Limite desta triagem:** a indicação de correção só se torna evidência concluída após CI verde, merge e nova análise SonarCloud. Não classificar a questão de SQL como resolvida sem revisão pelo responsável do projeto. Não alterar a porta :8080 nem qualquer banco para corrigir alertas estáticos.


## Verificação de segurança após SonarCloud via CI — 12/11/2026

| ID | Prioridade | Diagnóstico no SonarCloud | Correção / evidência |
|---|---|---|---|
| SONAR-07 | Alta | Dependência de GitHub Action sem SHA completo em `.github/workflows/ci.yml:71`. | [PR #6](https://github.com/FelipeR8083/organizacao-recursos/pull/6) fixa o scanner SonarSource e as demais ações por SHA verificado. |
| SONAR-08 | Média | `DB_PASSWORD` e `APP_ADMIN_PASSWORD` possuíam valores literais no workflow de carga. | [PR #6](https://github.com/FelipeR8083/organizacao-recursos/pull/6) usa senhas aleatórias efêmeras criadas no runner e mascaradas no log. |
| SONAR-09 | Média | Primeiro download JMeter com `curl -L` poderia redirecionar para HTTP. | Opções `--proto '=https'` e `--proto-redir '=https'`, mantendo verificação SHA512. |
| SONAR-10 | Média | Segundo download do checksum SHA512 com redirecionamento possivelmente inseguro. | Mesma restrição a HTTPS para URL e redirecionamentos. |
| RSK-02 / RSK-07 | Alta (histórico) | O scanner de CI inicialmente encontrou 4 alertas de segurança no código novo e Quality Gate reprovado, embora o CI Maven executasse. | **Resolvido quanto ao Quality Gate** após PR #6: [CI na `main` 43313349056](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43313349056), check SonarCloud **success**, 1 nova questão e 0 hotspots **no código novo**; JaCoCo XML importado. Não equivale a zero vulnerabilidades em todo o histórico. |
| RSK-08 | Média | O JMX atualizado inclui preflight `GET /csrf` e cookie por thread; o workflow agora também cria recurso com CSRF e separa as amostras POST/GET. | **Plano e CI corrigidos, mas execução JMeter pós-CSRF ainda não comprovada.** Não apresentar métricas históricas como resultado da nova versão. |

**Métricas do CI validado**: 188 testes, 0 falhas, JaCoCo 91,85% linhas e 78,37% branches, Docker build e análise SonarCloud executados com sucesso. A issue de `CHAR(64)` na migração V6 continua com justificativa técnica; não alterar migração antiga já aplicada apenas para remover aviso do SonarCloud. A revisão final deve diferenciar problemas de *código novo* e problemas de *código geral*.
