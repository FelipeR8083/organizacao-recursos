# Relatório de qualidade — versão integrada na `main` (12/11/2026)

## Identificação e decisão

- Projeto: **Gestão de Recursos Acadêmicos** — Senac TADS / Qualidade de Software.
- Equipe: **Felipe e Pietro** (ver autoria e créditos no repositório).
- Stack: Java 21, Spring Boot 3.3, PostgreSQL 16, JUnit 5, Testcontainers, WireMock, JaCoCo e JMeter.
- Versão verificada: `main`, após [PR #4](https://github.com/FelipeR8083/organizacao-recursos/pull/4) e [PR #5](https://github.com/FelipeR8083/organizacao-recursos/pull/5), commit de integração `219b4abdd664a4e7aff2d03a96b32ffe6c8f1516`.
- Base destinada à entrega: `main`, já integrada por pull request; revisão formal de outro integrante ainda não foi registrada.
- **Decisão:** versão funcional candidata à entrega acadêmica **com ressalvas explicitadas**. GitHub Actions e SonarQube Cloud executaram na `main`, com **Quality Gate aprovado**. O SonarCloud passou de **6 para 1 questão nova** após PR #5; a questão residual é de recomendação de tipo SQL `VARCHAR2` incompatível com o contexto PostgreSQL/migração Flyway histórica. Permanecem importação de cobertura JaCoCo, ensaio de carga pós-CSRF e revisão por pares. Não afirmar ausência global de vulnerabilidades críticas sem examinar a classificação das questões.

## Resultado automatizado (evidência verificada)

| Indicador | Meta | Resultado |
|---|---|---|
| CI / Maven / suíte de testes | Aprovação | **188 testes, 0 falhas, 0 erros, 0 ignorados** |
| Cobertura de linhas (JaCoCo) | ≥ 80% | **91,85% (789/859)** |
| Cobertura de branches (JaCoCo) | ≥ 70% | **78,37% (384/490)** |
| Build Docker no CI | Concluir | **Sucesso** |
| Banco real / integração | Testcontainers | Testes de integração PostgreSQL concluídos |
| Notificações webhook | WireMock | Testes automatizados concluídos |
| SonarQube Cloud / SonarCloud | Quality Gate / triagem de segurança | **Quality Gate aprovado no GitHub App, 1 questão nova após PR #5; 0 hotspots no código novo; cobertura do código novo no Sonar = 0,0% (integração JaCoCo pendente)** |

Fonte: [GitHub Actions 43306929799 — PR #5, 188 testes e Docker build aprovados](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43306929799). O merge do PR #5 iniciou a [execução adicional da `main` 43307450221](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43307450221). JaCoCo e resultados da suíte estão disponíveis nos logs e artefatos da execução. A etapa **"Analisar no SonarCloud"** deste workflow ainda é `skipped` porque as variáveis/segredo não estão configurados, **mas a análise automática do GitHub App SonarCloud executou separadamente e aprovou o Quality Gate**.

**Carga concorrente (evidência histórica anterior à proteção CSRF obrigatória):** [GitHub Actions 42711044633](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42711044633), JMeter com 20 pedidos simultâneos ao mesmo recurso: **1 HTTP 201 e 19 HTTP 409**, média de 106,9 ms, P95 209 ms. Plano `jmeter/reservas-concorrentes.jmx`; arquivar os relatórios JTL/HTML junto da entrega.

**BDD/TDD:** `docs/EVIDENCIA-BDD.md` documenta cenários e evidências de evolução do RF-03. Não se afirma que todas as funcionalidades foram feitas com TDD.

## Homologação funcional relatada pela equipe — 12/11/2026

Ambiente **exclusivamente isolado**: `http://localhost:8085`, banco `recursos_teste_v9` e PostgreSQL em porta 5433. **A instalação original na 8080 e o banco `recursos` não foram alterados.**

1. Aplicação iniciada com Java 21/Spring Boot; Flyway aplicou com sucesso as migrações **V1–V9 sobre banco vazio**, Hibernate iniciou com esquema validado.
2. Administrador `admin-homolog` cadastrou Sala 101, Projetor, Notebook e usuários responsáveis; interface oculta operações administrativas de usuários não autorizados.
3. Responsável Ana solicitou reserva #1; Bruno aprovou, iniciou e concluiu. Auditoria registrou quatro eventos com solicitante e autor da transição identificados.
4. Responsável Bruno solicitou reserva #2 com **projetor e notebook vinculados à mesma sala**; Ana aprovou. Retiradas e devoluções individuais foram registradas e a reserva concluiu automaticamente após a **última devolução**.
5. Cadastro público de professor aceitou formato `@teste.senac.br`, criou conta inativa; administrador a ativou; professor autenticou, consultou recursos e solicitou a reserva #3. **Envio/posse real do e-mail não foi testado**, pois SMTP estava desabilitado no ambiente de homologação.
6. Responsável Ana aprovou reserva de professor; tentativa de sobreposição da Sala 101 foi recusada e **1 conflito evitado** apareceu no relatório.
7. Relatórios exibiram **Sala 101: 3 reservas/180 minutos**, projetor: 1/60, notebook: 1/60; foram apresentadas **10 notificações simuladas**.
8. Administrador marcou Notebook em manutenção; opção sumiu do formulário. Tentativa direta HTTP de reserva com o material em manutenção retornou **HTTP 422**, corpo `Material em manutenção: 3`.

**Escopo da evidência manual:** os resultados acima foram transmitidos pela equipe a partir do navegador/PowerShell. Capturas e logs precisam ser guardados fora da conversa para documentação formal. O processo de homologação não substitui teste E2E visual automatizado.

## Defeitos tratados no fechamento

- **Separação de perfis:** administrador gerencia recursos, usuários e manutenção; apenas responsável avalia solicitações e movimenta materiais; responsável não aprova/rejeita a própria reserva. Cobertura por testes de API, serviço e E2E.
- **Flyway V9 / RF-09:** permite múltiplas movimentações para a mesma reserva, com identificação individual por recurso.
- **Conclusão prematura:** a interface agora oculta `CONCLUIDA` enquanto há material pendente; o servidor impede a transição manual nesse caso. Foram incluídos testes para material principal e reserva com dois materiais. **CI verde e nova imagem corrigida já iniciou na porta 8085, com login/logout testados; a transição manual de conclusão não foi repetida nessa imagem**.
- **Bloqueio por manutenção:** validado na interface e diretamente na API (HTTP 422).

## Riscos e pendências reais

1. **SonarCloud:** o GitHub App concluiu a análise em `main` e **aprovou o Quality Gate**; resultado inicial exibiu **6 questões novas**; após PR #5 a nova análise indicou **1 questão nova**, **0 hotspots no código novo**, **0,0% de cobertura no código novo** (sem JaCoCo importado). A questão residual de `CHAR(64)` está documentada como recomendação inadequada para migração Flyway PostgreSQL; falta revisão formal da classificação no Sonar e configuração via CI para importar cobertura. A etapa `Analisar no SonarCloud` do GitHub Actions continua `skipped`. A meta de zero bugs/vulnerabilidades críticas **não pode ser declarada integralmente comprovada** pelo resumo do check. Guia: `docs/CONFIGURAR-SONARCLOUD.md`.
2. **Revisão por pares:** realizar revisão real pelo Pietro no pull request final; registrar a decisão/comentários.
3. **Evidências/apresentação:** guardar captura do CI final e indicadores JaCoCo, JMeter JTL/HTML, telas desktop/celular (360px e 768px), diagramas/RTM; roteiro em `docs/APRESENTACAO.md`.
4. **Atualização de base legada:** Flyway V9 foi observado em **banco novo**, não sobre uma cópia de banco V8 com dados reais; preservar backup antes de uma futura atualização de produção.
5. **Regra temporal:** o sistema permitiu antecipar `APROVADA → EM_USO` e concluir reserva antes do horário agendado. Registrar como risco/decisão pendente; não afirmar que essa restrição está implementada.
6. **Execução final:** CI do PR #5 confirmado com sucesso ([run 43306929799](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43306929799)); nova `main` integrada no SHA `219b4abd` e [run 43307450221](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43307450221). Preservar logs/artefatos do run final.
7. **Conta professor:** domínio Senac é validado; confirmação real de e-mail requer SMTP/configuração de produção e não foi verificada na homologação.

## Segurança / SonarQube Cloud — verificação após PR #4 (12/11/2026)

A análise automática de código do GitHub App [SonarCloud Code Analysis](https://sonarcloud.io/dashboard?id=FelipeR8083_organizacao-recursos&branch=main) foi concluída no merge `60862cd8` com **Quality Gate passed**. O GitHub check apresentou **6 new issues**, **0 Security Hotspots no código novo**, **0% de coverage no código novo** e **0% de duplicação no código novo**. **Importante:** são métricas do *código novo*, não certificação de 0 questões ou 0 hotspots em todo o histórico.

O SonarCloud analisa automaticamente o repositório via GitHub App, mas ainda **não recebe o arquivo `target/site/jacoco/jacoco.xml`**. O CI Java, separadamente, comprovou 91,73% de linhas e 77,55% de branches via JaCoCo. O workflow já contém etapa condicional de análise com relatório JaCoCo, atualmente ignorada pela ausência de `SONAR_TOKEN`, `SONAR_ORGANIZATION` e `SONAR_PROJECT_KEY`; migrar para análise via GitHub Actions exige alinhar as configurações no Sonar para evitar análises duplicadas.

A correção de CSRF do [PR #4](https://github.com/FelipeR8083/organizacao-recursos/pull/4) liga a proteção em todos os perfis e foi testada por 185 testes automatizados, login, logout e consulta `/csrf` em homologação isolada :8085. Uma chamada anônima de teste sem token retornou **401**, o que **não comprova isoladamente rejeição por CSRF**; a verificação de HTTP 403 sem token foi feita pela suíte automatizada com usuário autenticado. Reexecutar JMeter para números de carga da nova versão.


## Revisão de confiabilidade e acessibilidade — PR #5 (12/11/2026)

A equipe examinou **6 issues de gravidade média/baixa**: quatro envolvendo padrões regex suscetíveis a backtracking/estouro de pilha, uma de acessibilidade por `autofocus` e uma de recomendação de `VARCHAR2` para uma coluna PostgreSQL `CHAR(64)`.

O [PR #5](https://github.com/FelipeR8083/organizacao-recursos/pull/5) corrigiu as **4 regex** com validações/varreduras lineares e removeu o `autofocus` do login. O CI do PR passou com **188 testes**, nenhuma falha, **91,85% de linhas** e **78,37% de branches**. A análise automática do SonarCloud após o merge `219b4abd` registrou **Quality Gate passed**, reduzindo **6 para 1 questão nova**, com zero hotspots **no código novo**.

A questão remanescente está na migração histórica `V6__verificacao_email_professor.sql`, que usa `CHAR(64)` para armazenar um hash hexadecimal de comprimento fixo. **Não** substituímos por `VARCHAR2` (tipo de Oracle, incompatível com PostgreSQL) nem alteramos uma migração Flyway já aplicada, cujo checksum seria modificado. A equipe deve considerar a classificação como falso positivo/aceitação fundamentada no SonarCloud após revisão; **não há aceite formal registrado**. A cobertura do SonarCloud permanece pendente de importação, independentemente da cobertura JaCoCo do CI.


## Referências

- [Especificação do professor](https://afonsolelis.github.io/aulas_senac/pages/qualidade2/especificacao-projeto.html)
- [RTM](../RTM.md)
- [Plano de testes](PLANO-DE-TESTES.md)
- [Registro de defeitos](REGISTRO-DEFEITOS.md)
- [Homologação e checklist](HOMOLOGACAO-FINAL-2026-11-12.md)
- [Roteiro de apresentação](APRESENTACAO.md)

**Conclusão:** a equipe demonstrou funcionamento de grande parte do escopo exigido, atingiu as metas automatizadas de cobertura e obteve **Quality Gate aprovado** no SonarCloud para a `main`. A entrega como versão candidata é tecnicamente justificável **com riscos explicitados**; a cobertura ainda não foi importada ao Sonar, as seis questões iniciais foram triadas, cinco corrigidas e uma justificada tecnicamente; não há nova execução JMeter da versão com CSRF. O relatório não certifica cumprimento de 100% dos requisitos de evidência nem qualidade de produção.
