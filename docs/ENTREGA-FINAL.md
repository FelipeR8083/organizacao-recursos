# Pacote de entrega acadêmica — Organização de Recursos (2026.2)

**Equipe:** Felipe (`@FelipeR8083`) e Pietro (`@pietrolaureano`)  
**Disciplina:** Qualidade de Software — Senac TADS | **Registro de organização:** 12/11/2026  
**Especificação oficial:** [Projeto Organização de Recursos — Qualidade 2026.2](https://afonsolelis.github.io/aulas_senac/pages/qualidade2/especificacao-projeto.html)  
**Repositório:** [FelipeR8083/organizacao-recursos](https://github.com/FelipeR8083/organizacao-recursos)

> **Decisão de entrega:** candidata à apresentação acadêmica **com ressalvas documentadas**, não certificação de produção e não afirmação de cumprimento de todas as verificações manuais. O docente avalia o atendimento à exigência de revisão por pares durante o desenvolvimento; as revisões abaixo ocorreram **retrospectivamente**. Não alterar datas ou autorias.

## 1. Acesso rápido para o avaliador

| Item exigido | Link e situação documentada |
| --- | --- |
| Código-fonte, instalação e execução | [README principal](../README.md) e [instruções de atualização segura](../README-NOVA-VERSAO.md) |
| Matriz de rastreabilidade dos **14 RFs** | [RTM — requisito → risco → teste → evidência](../RTM.md); RF-13 tem validação visual manual pendente |
| Arquitetura e ADRs | [Arquitetura/C4](ARQUITETURA-E-OPERACAO.md), [componentes e diagramas de sequência](DIAGRAMAS-SEQUENCIA.md), [ADR segurança](ADR-001-SEGURANCA.md), [ADR aprovações/materiais](ADR-002-APROVACAO-RESTRITA-E-MOVIMENTACAO.md), [ADR papéis/aprovação em par](ADR-003-PAPEIS-E-APROVACAO-EM-PAR.md) |
| Estratégia, suíte e resultado de testes | [Plano de testes](PLANO-DE-TESTES.md), [BDD — falha/correção/sucesso](EVIDENCIA-BDD.md), [relatório de qualidade](RELATORIO-QUALIDADE.md) |
| Pipeline e JaCoCo | [GitHub Actions — CI validado na main](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844286): **188 testes, 0 falhas**, **91,85% linhas / 78,37% branches** (metas: 80%/70%); [workflow versionado](../.github/workflows/ci.yml) |
| Segurança / análise estática | [Relatório de qualidade](RELATORIO-QUALIDADE.md); [SonarQube Cloud](https://sonarcloud.io/dashboard?id=FelipeR8083_organizacao-recursos); Quality Gate aprovado. Não afirmar zero vulnerabilidades globais sem triagem completa |
| Carga e concorrência | [Plano JMeter](PLANO-TESTE-CARGA.md); [workflow de carga executado na main](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844059); [script JMX](../jmeter/reservas-concorrentes.jmx) |
| Defeitos e riscos | [Registro de defeitos](REGISTRO-DEFEITOS.md); [homologação e ressalvas](HOMOLOGACAO-FINAL-2026-11-12.md) |
| Revisão por pares — **retrospectiva** | [PR #8](https://github.com/FelipeR8083/organizacao-recursos/pull/8), [parecer de Pietro](https://github.com/FelipeR8083/organizacao-recursos/pull/8#pullrequestreview-5883682561), [manifestação de Felipe](https://github.com/FelipeR8083/organizacao-recursos/pull/8#issuecomment-6839223791), [duas análises técnicas](REGISTRO-ACOMPANHAMENTO-E-REVISAO-RETROSPECTIVA.md) |
| Apresentação e demonstração | [Roteiro de 10 slides](APRESENTACAO.md) e [roteiro de demonstração](#4-demonstração-sugerida-para-a-prova-oral). A apresentação ao vivo ainda precisa acontecer |

## 2. Resultados quantitativos com evidências

| Indicador | Resultado documentado | Fonte |
| --- | --- | --- |
| Suíte automatizada | **188 testes; 0 falhas/erros** | [CI final do código na main](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844286) |
| Cobertura JaCoCo — linhas | **91,85%**, meta 80% | [Relatório de qualidade](RELATORIO-QUALIDADE.md) |
| Cobertura JaCoCo — branches | **78,37%**, meta 70% | [Relatório de qualidade](RELATORIO-QUALIDADE.md) |
| Teste JMeter, pós-CSRF | **20 GET /csrf HTTP 200 + 20 POST concorrentes; 1 HTTP 201 e 19 HTTP 409** | [Execução real e artefatos JTL/HTML](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844059) |
| Latência dos POST do JMeter | Média **38,5 ms**, P95 **117 ms**, máximo **122 ms** | [Execução JMeter](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844059) |
| SonarCloud | **Quality Gate passed**, com ressalva de questão SQL residual para triagem | [Relatório](RELATORIO-QUALIDADE.md) e [SonarCloud](https://sonarcloud.io/dashboard?id=FelipeR8083_organizacao-recursos) |
| CI do PR documental #8 | **Build e SonarCloud aprovados** no último SHA verificado antes da edição final deste guia; conferir novamente após integração | [Checks do PR #8](https://github.com/FelipeR8083/organizacao-recursos/pull/8/checks) |

**Escopo:** os números acima se referem às execuções citadas, não a testes executados durante a redação desta página. O CI de código passou na `main`; o PR #8 acrescenta **documentação**, sem modificar a lógica da aplicação.

## 3. Revisões e histórico de desenvolvimento

- **Implementação e correções verificáveis:** [PRs #1 a #7](https://github.com/FelipeR8083/organizacao-recursos/pulls?q=is%3Apr+is%3Aclosed), inclusive versão intermediária #2 encerrada sem merge direto. Consultar [cronologia e checklist](REGISTRO-ACOMPANHAMENTO-E-REVISAO-RETROSPECTIVA.md).
- **Pietro:** submeteu pelo próprio login a [review do PR #8](https://github.com/FelipeR8083/organizacao-recursos/pull/8#pullrequestreview-5883682561) em **12/11/2026 às 20h31 (Brasília)** com estado **COMMENTED**, não APPROVED. Declarou concordar com a manutenção de PI-REV-01 a PI-REV-05 como melhorias/ressalvas.
- **Felipe:** publicou pela própria conta a [manifestação detalhada sobre reservas, concorrência, materiais e persistência](https://github.com/FelipeR8083/organizacao-recursos/pull/8#issuecomment-6839223791) em **12/11/2026 às 20h40 (Brasília)**. Por ser autor do PR #8, esse registro é comentário, não aprovação do próprio PR.
- **Transparência:** ambos os textos foram preparados a partir de análise assistida por IA e avaliação da documentação existente. **Não há registro comprovando que houve revisão por pares por essas contas no momento das alterações dos PRs #1–#7**; o docente informou exigir revisão durante o desenvolvimento. Não declarar cumprimento retroativo dessa parte.

## 4. Demonstração sugerida para a prova oral

1. Apresentar problema, equipe e [RTM](../RTM.md) (14 requisitos; citar limites reais da validação visual).
2. Expor [arquitetura C4](ARQUITETURA-E-OPERACAO.md), API Spring Boot e PostgreSQL com migrações Flyway.
3. Em **ambiente seguro de demonstração**, entrar como administrador e mostrar cadastro/manutenção de sala e materiais.
4. Trocar para perfil solicitante/professor, fazer solicitação de reserva e tentar conflito no mesmo horário.
5. Com responsável **distinto do solicitante**, aprovar a reserva e mostrar a restrição de permissão dos demais perfis.
6. Retirar e devolver cada material associado; conferir conclusão apenas após devolução de todos.
7. Abrir auditoria, notificações simuladas e relatórios; mostrar mensagens HTTP de autorização/conflito.
8. Mostrar [Actions](https://github.com/FelipeR8083/organizacao-recursos/actions), [SonarCloud](https://sonarcloud.io/dashboard?id=FelipeR8083_organizacao-recursos) e [JMeter](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844059).
9. Apresentar [revisões retrospectivas](https://github.com/FelipeR8083/organizacao-recursos/pull/8) e mencionar ressalvas sem atribuir revisão a data anterior.
10. Concluir com resultados, riscos remanescentes e prioridades de melhoria.

**Importante para a demonstração:** se existir uma instância local original com dados, **não atualizar a base em uso sem backup**. Para demonstração limpa, seguir o [README](../README.md) com ambiente isolado e credenciais novas; não expor senhas em slides ou gravações.

## 5. Pendências que não devem ser ocultadas

| Ponto | Situação atual e ação |
| --- | --- |
| **Teste visual de responsividade** | [Checklist RF-13](TESTE-RESPONSIVIDADE.md) ainda **não executado/documentado** integralmente (360, 390, 768 e 1366 px). Capturar evidências após teste real; não marcar como aprovado sem execução |
| **SMTP de e-mail institucional** | Fluxo automatizado/simulado coberto; envio/posse de e-mail real **não homologado** |
| **Migração V8→V9 de banco existente** | Verificada em banco novo; **não validada em cópia populada**. Testar apenas com backup e isolamento |
| **Regra temporal de início/conclusão** | Permitida transição antecipada em homologação; definir decisão/restrição com equipe/docente |
| **SonarCloud histórico** | Quality Gate passou, mas há alerta SQL residual que precisa de triagem formal/contexto |
| **Revisão exigida durante o desenvolvimento** | Os pareceres foram feitos retrospectivamente em 12/11; avaliação de eventual crédito parcial cabe ao professor |
| **Prova oral e submissão** | Roteiro pronto; apresentação e submissão pelo canal indicado pelo professor **ainda dependem da dupla** |

## 6. Orientação de envio

**Link principal para entregar ao docente:** https://github.com/FelipeR8083/organizacao-recursos

No texto de submissão, apontar este guia de entrega, a RTM, o relatório de qualidade, o PR #8, o CI e o JMeter. Se a plataforma exigir arquivos ou formulário além do GitHub, preencher o canal definido pelo professor. **Esta página não envia o projeto automaticamente à plataforma acadêmica.**

**Não declarar:** revisão por pares feita durante o desenvolvimento; zero vulnerabilidades críticas globais sem confirmação; testes manuais ou SMTP realizados sem evidência; migração V8→V9 populada validada.
