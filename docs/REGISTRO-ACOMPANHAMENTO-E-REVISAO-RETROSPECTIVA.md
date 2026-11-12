# Acompanhamento técnico do desenvolvimento e revisão retrospectiva por pares

**Projeto:** Gestão de Recursos Acadêmicos — Qualidade de Software (Senac TADS)  
**Data de preparação deste registro:** 12/11/2026  
**Origem:** reconstrução documental a partir dos pull requests, diffs, relatórios e execuções do GitHub.  
**Situação da revisão independente:** solicitada a `pietrolaureano` no [PR #8](https://github.com/FelipeR8083/organizacao-recursos/pull/8), **ainda não concluída** na data da preparação deste documento.

> **Transparência acadêmica:** o professor solicitou revisão por pares **durante** o desenvolvimento. A equipe não registrou uma revisão formal por outro integrante naquele período. O material abaixo comprova avaliações e correções técnicas realizadas ao longo do trabalho, mas **não deve ser apresentado como se Pietro tivesse revisado ou aprovado essas alterações na época**. Este documento é uma reconstrução retrospectiva, preparada com apoio de IA, para subsidiar uma revisão humana real agora e solicitar, se cabível, avaliação parcial da evidência existente.

## 1. Linha do tempo verificável de controle de qualidade

Todos os PRs abaixo foram criados e integrados em **12/11/2026**, conforme o histórico do GitHub. As datas de suas atividades não foram alteradas nem retroativamente atribuídas a outros autores.

| Etapa e evidência | Problema ou objetivo | Alteração documentada | Verificação e limitação |
| --- | --- | --- | --- |
| [PR #4 — Segurança e CSRF](https://github.com/FelipeR8083/organizacao-recursos/pull/4) | Alertas de CSRF e segurança das operações com sessão. | Habilitação de CSRF em todos os perfis; token de sessão em `GET /csrf`; adaptação dos formulários, chamadas `fetch`, testes de autenticação e plano JMeter. | [CI 43296735058](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43296735058): 185 testes aprovados segundo o registro do PR; a nova evidência de carga ainda era pendente naquele momento. |
| [PR #5 — Confiabilidade e acessibilidade](https://github.com/FelipeR8083/organizacao-recursos/pull/5) | Alertas SonarCloud sobre regex e `autofocus`. | Substituição de regex por validação/varredura iterativa; remoção de `autofocus` no login; testes de regressão acrescentados. | CI registrado com 188 testes e cobertura JaCoCo de 91,85% linhas / 78,37% branches. A recomendação sobre `CHAR(64)` na migração Flyway V6 permaneceu para triagem, não foi apresentada como resolvida. |
| [PR #6 — Segurança dos workflows](https://github.com/FelipeR8083/organizacao-recursos/pull/6) | Quatro alertas novos de segurança do CI após a ativação do scanner por GitHub Actions. | GitHub Actions fixadas por SHA completo; senhas efêmeras mascaradas em vez de credenciais literais; downloads do JMeter restritos a HTTPS, inclusive nos redirecionamentos. | [CI da main 43313349056](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43313349056) e SonarCloud: Quality Gate aprovado, com questão residual registrada no relatório. |
| [PR #7 — Carga pós-CSRF](https://github.com/FelipeR8083/organizacao-recursos/pull/7) | Repetir o teste de concorrência após a adoção obrigatória de CSRF; corrigir cálculo do P95. | Workflow de carga na `main`; cálculo por posto mais próximo, distinguindo P95 e máximo. | [JMeter na main 43316844059](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844059): 20 GET /csrf (HTTP 200), 20 POST de reservas, **1 HTTP 201 + 19 HTTP 409**; média 38,5 ms, P95 117 ms, máximo 122 ms. [CI 43316844286](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844286) aprovado. |

**Distinção essencial:** os PRs, testes e alertas do SonarCloud são evidências de **acompanhamento técnico e correção de problemas**; não provam que houve **revisão por pares humana** enquanto essas alterações eram desenvolvidas. Tampouco um Quality Gate aprovado atesta ausência absoluta de vulnerabilidades.

## 2. Checklist de revisão independente — a realizar por Pietro

O revisor deve examinar o código e os diffs históricos (além dos resumos), consultar as evidências e registrar observações originais no [PR #8](https://github.com/FelipeR8083/organizacao-recursos/pull/8). **Nenhuma caixa abaixo foi marcada antecipadamente.**

- [ ] **Segurança e autorização (PR #4):** verificar `SegurancaConfig.java`, `CsrfController.java`, fluxos de login/cadastro/logout e testes de CSRF e permissões. Registrar possíveis riscos ou justificativa técnica.
- [ ] **Confiabilidade e acessibilidade (PR #5):** conferir formatos válidos/inválidos de e-mail institucional, casos extremos dos novos validadores e impacto da remoção de `autofocus`.
- [ ] **CI e cadeia de suprimentos (PR #6):** conferir SHA das Actions, segredo não versionado, restrição HTTPS e integridade do pacote JMeter.
- [ ] **Concorrência e métricas (PR #7):** conferir JMX, workflow e critério de 1 criação (201) para 19 conflitos (409), além do cálculo de P95 sobre o JTL.
- [ ] **Questões em aberto:** avaliar o alerta de `CHAR(64)` no Flyway V6, a migração V8→V9 ainda não validada com base populada, transições temporais antecipadas e ausência de homologação SMTP real.

### 3. Registro da decisão do revisor (não preenchido)

| Campo | A preencher pelo revisor depois da análise |
| --- | --- |
| Revisor | Pietro — conta GitHub `pietrolaureano` |
| Data efetiva da análise | **Pendente** |
| Trechos examinados | **Pendente** |
| Achados, sugestões ou solicitações de mudança | **Pendente** |
| Decisão (Comment / Request changes / Approve) | **Pendente** |
| Link da revisão no GitHub | **Pendente** |

A decisão deve ser registrada por **Pietro na própria conta**, por `Files changed → Review changes` no PR #8. Ele pode usar o navegador do celular, se conseguir acessar a interface de revisão. A data será a da submissão real. **Uma aprovação não deve ser escolhida antes da análise.**

## 4. Texto de apresentação honesta da evidência ao professor

> Durante o desenvolvimento, realizamos avaliações automáticas e correções sucessivas documentadas nos PRs #4 a #7, com testes, SonarCloud, análise de segurança e JMeter. Reconhecemos que a revisão formal por outro integrante, exigida durante o desenvolvimento, não foi registrada naquele momento. Por isso, organizamos a trilha histórica verificável e solicitamos agora uma revisão retrospectiva independente, registrada no PR #8 com data real. Pedimos que as evidências de controle contínuo de qualidade sejam consideradas para eventual crédito parcial, sem apresentá-las como substituição integral da revisão por pares exigida.

## 5. Referências adicionais

- [Relatório de qualidade e ressalvas](RELATORIO-QUALIDADE.md)
- [Registro de defeitos](REGISTRO-DEFEITOS.md)
- [Plano de teste de carga](PLANO-TESTE-CARGA.md)
- [PR #8: pedido de revisão formal](https://github.com/FelipeR8083/organizacao-recursos/pull/8)

**Estado final deste documento:** cronologia técnica reconstruída e roteiro pronto; **parecer independente de Pietro ainda pendente**.
