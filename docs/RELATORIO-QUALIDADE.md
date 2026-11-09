# Relatório de qualidade — versão candidata

## Identificação

- Projeto: Organização de Recursos Acadêmicos
- Curso: Senac TADS — Qualidade de Software
- Stack: Java 21, Spring Boot 3.3, PostgreSQL 16, JUnit 5, Testcontainers, JaCoCo, WireMock, JMeter e SonarCloud (condicional).
- Branch avaliada: `melhorias/qualidade-e-requisitos`
- Base: `main`

## Resumo executivo

A versão candidata contém os 14 requisitos funcionais do enunciado implementados no código, com testes associados e matriz em `RTM.md`. Foram acrescentados controle de titularidade nas consultas e histórico, auditoria de recursos/usuários, interface com telas por perfil, relatórios e notificações.

**Status: funcionalidades implementadas; entrega acadêmica ainda não homologada integralmente.** A execução [42708687473](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42708687473) foi aprovada com 115 testes, 87,81% de cobertura de linhas e 71,64% de branches. O `pom.xml` foi atualizado posteriormente para exigir 80%/70% no build; ainda é necessário validar esse check no SHA final. SonarCloud não executou por falta de configuração, JMeter foi executado e aprovado no [CI 42711044633](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42711044633): 20 tentativas, uma reserva criada e 19 conflitos; a interface ainda necessita de aceitação visual.

## Evidências automatizadas versionadas

- Testes de domínio parametrizados: período e máquina de estados.
- Integração com PostgreSQL Testcontainers: reserva, conflitos, auditoria, pesquisa, relatórios, movimentação e notificações.
- Concorrência: múltiplas solicitações para o mesmo recurso.
- Segurança/API: autenticação Basic, 401/403 e papéis.
- Fluxo ponta a ponta HTTP: cadastro, solicitação, aprovação, auditoria, relatório e notificação.
- WireMock: webhook de notificação com resposta aceita e falha simulada.

Consulte [PLANO-DE-TESTES.md](PLANO-DE-TESTES.md) e [REGISTRO-DEFEITOS.md](REGISTRO-DEFEITOS.md).

## Indicadores de qualidade

| Indicador | Meta/critério | Resultado atual |
|---|---|---|
| Build Maven / testes | 100% verdes no commit final | 115 testes aprovados na execução 42708687473; conferir novamente o SHA final |
| Cobertura de linhas | >= 80% | 87,81% (526/599) — meta atingida no CI 42708687473 |
| Cobertura de branches | >= 70% | 71,64% (192/268) — meta atingida no CI 42708687473 |
| SonarCloud | Sem bugs/vulnerabilidades críticas | Integração condicional; falta configurar token/identificadores e executar |
| Teste de carga | 20 threads; uma criação e demais conflitos para mesmo slot | **Aprovado** no CI 42711044633: 1 HTTP 201, 19 HTTP 409, média 106,9 ms e P95 209 ms. JTL e HTML disponíveis como artefatos. |
| E2E de navegador | Fluxo visual completo | Não implementado; E2E HTTP cobre integração entre camadas |
| Responsividade | 360px, 768px e desktop | Checklist manual pendente |

## Riscos residuais

1. Migração em banco legado pode falhar se houver recursos duplicados normalizados; fazer backup e executar limpeza antes de migrar.
2. HTTP Basic exige HTTPS fora de localhost.
3. O relatório de qualidade só pode ser finalizado depois dos resultados reais dos indicadores.
4. O enunciado exige evidência verificável, não apenas configuração de ferramentas.

## Conclusão

A implementação funcional foi ampliada substancialmente, mas esta versão permanece candidata. O responsável pela entrega deve confirmar o CI no SHA final com a regra JaCoCo, configurar/rodar SonarCloud, preservar os artefatos JMeter/JaCoCo e concluir a validação visual/manual e revisão por pares antes de integrar à `main`.
