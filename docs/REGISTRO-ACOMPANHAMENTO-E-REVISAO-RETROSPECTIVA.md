# Acompanhamento técnico do desenvolvimento e revisão retrospectiva por pares

**Projeto:** Gestão de Recursos Acadêmicos — Qualidade de Software (Senac TADS)  
**Data de preparação deste registro:** 12/11/2026  
**Origem:** reconstrução documental a partir dos pull requests, diffs, relatórios e execuções do GitHub.  
**Situação da revisão independente:** solicitada a `pietrolaureano` no [PR #8](https://github.com/FelipeR8083/organizacao-recursos/pull/8), **ainda não concluída** na data da preparação deste documento.

> **Transparência acadêmica:** o professor solicitou revisão por pares **durante** o desenvolvimento. A equipe não registrou uma revisão formal por outro integrante naquele período. O material abaixo comprova avaliações e correções técnicas realizadas ao longo do trabalho, mas **não deve ser apresentado como se Pietro tivesse revisado ou aprovado essas alterações na época**. Este documento é uma reconstrução retrospectiva, preparada com apoio de IA, para subsidiar uma revisão humana real agora e solicitar, se cabível, avaliação parcial da evidência existente.

## 1. Linha do tempo verificável de controle de qualidade

O histórico começa **antes da etapa final**: o PR #1 foi aberto em **09/11/2026** e o PR #2 em **11/11/2026**. Os PRs #3–#7 registram consolidação e correções posteriores, em **12/11/2026**. O PR #2 foi encerrado **sem merge próprio**; o PR #3 informa que incorporou mudanças das branches de correção. As datas e autorias são preservadas. Esta reconstrução não equivale a parecer de Pietro durante o desenvolvimento.

| Etapa e evidência | Problema ou objetivo | Alteração documentada | Verificação e limitação |
| --- | --- | --- | --- |
| [PR #1 — Requisitos e implementação inicial (aberto em 09/11)](https://github.com/FelipeR8083/organizacao-recursos/pull/1) | Implementar os 14 requisitos funcionais, incluindo autenticação, cadastros, disponibilidade, reservas, concorrência, auditoria, notificações, relatórios e interface. | Código e documentos: [RTM](../RTM.md), [plano de testes](PLANO-DE-TESTES.md), [BDD](EVIDENCIA-BDD.md) e diagramas de arquitetura; testes automatizados e de carga. | [CI 42708687473](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42708687473): **115 testes**, JaCoCo **87,81% de linhas / 71,64% de branches** segundo o PR. [JMeter 42711044633](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42711044633): 1 HTTP 201 e 19 HTTP 409 **antes do CSRF obrigatório**. Integrado em 12/11; não há aprovação por pares documentada nesse PR. |
| [PR #2 — CSRF local e migração V8 (aberto em 11/11)](https://github.com/FelipeR8083/organizacao-recursos/pull/2) | Resolver inconsistência no token de confirmação, falha de inicialização em banco restaurado e erro HTTP 500 do endpoint `/csrf` em perfil padrão. | Proposta de migração V8 e tratamento de token/interface no perfil então configurado. | **Fechado sem merge direto.** O PR #3 informa que consolidou as mudanças de V8/V9. O CSRF foi posteriormente revisado no PR #4; não tratar solução intermediária como estado final. |
| [PR #3 — Integração de funcionalidades (12/11)](https://github.com/FelipeR8083/organizacao-recursos/pull/3) | Unificar requisitos e correções de perfis, reservas com múltiplos materiais, Flyway V9, auditoria e relatórios. | Separação dos papéis; bloqueio de autoaprovação; retirada/devolução individual; bloqueio de conclusão com material pendente. | [CI 43279334663](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43279334663): **184 testes**, JaCoCo **91,91% de linhas / 77,88% de branches**, build Docker, conforme o PR. A migração V9 foi homologada sobre banco novo, **não** sobre V8 populado. |
| [PR #4 — Segurança e CSRF](https://github.com/FelipeR8083/organizacao-recursos/pull/4) | Alertas de CSRF e segurança das operações com sessão. | Habilitação de CSRF em todos os perfis; token de sessão em `GET /csrf`; adaptação dos formulários, chamadas `fetch`, testes de autenticação e plano JMeter. | [CI 43296735058](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43296735058): 185 testes aprovados segundo o registro do PR; a nova evidência de carga ainda era pendente naquele momento. |
| [PR #5 — Confiabilidade e acessibilidade](https://github.com/FelipeR8083/organizacao-recursos/pull/5) | Alertas SonarCloud sobre regex e `autofocus`. | Substituição de regex por validação/varredura iterativa; remoção de `autofocus` no login; testes de regressão acrescentados. | CI registrado com 188 testes e cobertura JaCoCo de 91,85% linhas / 78,37% branches. A recomendação sobre `CHAR(64)` na migração Flyway V6 permaneceu para triagem, não foi apresentada como resolvida. |
| [PR #6 — Segurança dos workflows](https://github.com/FelipeR8083/organizacao-recursos/pull/6) | Quatro alertas novos de segurança do CI após a ativação do scanner por GitHub Actions. | GitHub Actions fixadas por SHA completo; senhas efêmeras mascaradas em vez de credenciais literais; downloads do JMeter restritos a HTTPS, inclusive nos redirecionamentos. | [CI da main 43313349056](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43313349056) e SonarCloud: Quality Gate aprovado, com questão residual registrada no relatório. |
| [PR #7 — Carga pós-CSRF](https://github.com/FelipeR8083/organizacao-recursos/pull/7) | Repetir o teste de concorrência após a adoção obrigatória de CSRF; corrigir cálculo do P95. | Workflow de carga na `main`; cálculo por posto mais próximo, distinguindo P95 e máximo. | [JMeter na main 43316844059](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844059): 20 GET /csrf (HTTP 200), 20 POST de reservas, **1 HTTP 201 + 19 HTTP 409**; média 38,5 ms, P95 117 ms, máximo 122 ms. [CI 43316844286](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844286) aprovado. |

**Evolução de qualidade durante a implementação inicial:** o documento [BDD do RF-03](EVIDENCIA-BDD.md) registra teste de disponibilidade que falhou no [CI 42704827362](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42704827362), correção no commit [c5b2fe1](https://github.com/FelipeR8083/organizacao-recursos/commit/c5b2fe1) e aprovação posterior no [CI 42705420817](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42705420817). Essa sequência é evidência real de identificação e correção de defeito, **não** de revisão humana por pares nem prova de que todo o projeto usou TDD.

**Distinção essencial:** os PRs, testes e alertas do SonarCloud são evidências de **acompanhamento técnico e correção de problemas**; não provam que houve **revisão por pares humana** enquanto essas alterações eram desenvolvidas. Tampouco um Quality Gate aprovado atesta ausência absoluta de vulnerabilidades.

## 2. Duas análises técnicas preparatórias (não assinadas)

Foram criados **dois documentos diferentes** com observações de código, achados preliminares e recomendações concretas, para conferência e eventual manifestação de cada integrante:

- **[Análise para Felipe — reservas, materiais, concorrência e persistência](REVISAO-PARA-FELIPE-RASCUNHO.md)**: aponta, entre outros itens, risco a testar na concorrência do reagendamento, transições antecipadas, migração V8→V9 e custo do filtro em memória.
- **[Análise para Pietro — segurança, APIs, testes e CI](REVISAO-PARA-PIETRO-RASCUNHO.md)**: documenta controles verificados e melhorias/pontos em aberto no contrato de criação de reservas, publicação de Swagger, confirmação real de e-mail, triagem Sonar e evidências visuais.

**Status de ambos: RASCUNHOS ELABORADOS COM APOIO DE IA.** Os nomes indicam somente o **destinatário** de cada roteiro, **não a autoria** da análise por aquele aluno. Nenhum rascunho foi aprovado ou enviado pela conta de Pietro ou de Felipe como parecer pessoal.

A autoria de cada trecho implementado precisa ser confirmada antes de chamar o trabalho de revisão por pares: **não é revisão independente avaliar o próprio código**. Além disso, Felipe é autor do **PR #8**, portanto não pode aprovar formalmente esse próprio PR. Pode comentar seu parecer ali; para ser revisor formal no GitHub precisa analisar um PR aberto por outra pessoa.

## 3. Checklist de revisão independente — a realizar por Pietro

O revisor deve examinar o código e os diffs históricos (além dos resumos), consultar as evidências e registrar observações originais no [PR #8](https://github.com/FelipeR8083/organizacao-recursos/pull/8). **Nenhuma caixa abaixo foi marcada antecipadamente.**

### Desenvolvimento inicial e funcionalidades principais (PRs #1–#3)

- [ ] **Requisitos e RTM (PR #1):** conferir RF-01 a RF-14 na [matriz de rastreabilidade](../RTM.md) e apontar possíveis diferenças entre requisitos, código e testes.
- [ ] **Arquitetura e persistência (PRs #1–#3):** examinar decisões arquiteturais, dados de recursos/professores/reservas, migrações Flyway e compatibilidade de banco.
- [ ] **Identidade e papéis (PRs #1–#3):** verificar autenticação, separação de SOLICITANTE/PROFESSOR/RESPONSAVEL/ADMINISTRADOR, ativação de professor, titularidade e autorização nas APIs.
- [ ] **Regras centrais de reservas (PRs #1–#3):** verificar sobreposição de recursos/professores, reagendamento, bloqueio em manutenção, concorrência e aprovação de recursos restritos.
- [ ] **Materiais, auditoria e relatórios (PR #3):** conferir retirada e devolução de vários materiais, bloqueio de conclusão prematura, rastreabilidade de mudanças, notificações e métricas.
- [ ] **Testes, BDD e interface (PR #1):** examinar [plano de testes](PLANO-DE-TESTES.md), [BDD](EVIDENCIA-BDD.md), API/OpenAPI e limitações da evidência de responsividade. Não confundir teste E2E HTTP com teste visual de navegador.
- [ ] **Correções intermediárias (PR #2):** conferir a causa do problema de banco e CSRF e identificar no PR #3/#4 qual foi a solução final integrada.

### Segurança, qualidade e carga no fechamento (PRs #4–#7)

- [ ] **Segurança e autorização (PR #4):** verificar `SegurancaConfig.java`, `CsrfController.java`, fluxos de login/cadastro/logout e testes de CSRF e permissões. Registrar possíveis riscos ou justificativa técnica.
- [ ] **Confiabilidade e acessibilidade (PR #5):** conferir formatos válidos/inválidos de e-mail institucional, casos extremos dos novos validadores e impacto da remoção de `autofocus`.
- [ ] **CI e cadeia de suprimentos (PR #6):** conferir SHA das Actions, segredo não versionado, restrição HTTPS e integridade do pacote JMeter.
- [ ] **Concorrência e métricas (PR #7):** conferir JMX, workflow e critério de 1 criação (201) para 19 conflitos (409), além do cálculo de P95 sobre o JTL.
- [ ] **Questões em aberto:** avaliar o alerta de `CHAR(64)` no Flyway V6, a migração V8→V9 ainda não validada com base populada, transições temporais antecipadas e ausência de homologação SMTP real.

### 4. Registro da decisão do revisor (não preenchido)

| Campo | A preencher pelo revisor depois da análise |
| --- | --- |
| Revisor | Pietro — conta GitHub `pietrolaureano` |
| Data efetiva da análise | **Pendente** |
| Trechos examinados | **Pendente** |
| Achados, sugestões ou solicitações de mudança | **Pendente** |
| Decisão (Comment / Request changes / Approve) | **Pendente** |
| Link da revisão no GitHub | **Pendente** |

A decisão deve ser registrada por **Pietro na própria conta**, por `Files changed → Review changes` no PR #8. Ele pode usar o navegador do celular, se conseguir acessar a interface de revisão. A data será a da submissão real. **Uma aprovação não deve ser escolhida antes da análise.**

## 5. Texto de apresentação honesta da evidência ao professor

> Durante o desenvolvimento, realizamos implementação e correções progressivas documentadas nos PRs #1 a #7, com testes, rastreabilidade, BDD, análise de segurança, SonarCloud e JMeter. Reconhecemos que a revisão formal por outro integrante, exigida durante o desenvolvimento, não foi registrada naquele momento. Por isso, organizamos a trilha histórica verificável e solicitamos agora uma revisão retrospectiva independente, registrada no PR #8 com data real. Pedimos que as evidências de controle contínuo de qualidade sejam consideradas para eventual crédito parcial, sem apresentá-las como substituição integral da revisão por pares exigida.

## 6. Referências adicionais

- [Relatório de qualidade e ressalvas](RELATORIO-QUALIDADE.md)
- [Registro de defeitos](REGISTRO-DEFEITOS.md)
- [Plano de teste de carga](PLANO-TESTE-CARGA.md)
- [PR #8: pedido de revisão formal](https://github.com/FelipeR8083/organizacao-recursos/pull/8)

**Dúvida sobre quem deve revisar:** a [especificação oficial](https://afonsolelis.github.io/aulas_senac/pages/qualidade2/especificacao-projeto.html) exige “repositório com histórico de trabalho e revisão por pares”, mas não define explicitamente se apenas um integrante da dupla deve revisar ou se os dois devem fazê-lo. Para atender com mais robustez, sugerimos revisões recíprocas de partes realmente desenvolvidas pelo outro, **sem presumir autoria ou aprovação**, e confirmar a quantidade exigida com o professor.

**Estado final deste documento:** cronologia técnica ampliada para o desenvolvimento **inteiro** e roteiro pronto; **parecer independente de Pietro ainda pendente**.
