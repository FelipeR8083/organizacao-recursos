# Relatório de qualidade — versão candidata

## Identificação

- Projeto: Organização de Recursos Acadêmicos
- Curso: Senac TADS — Qualidade de Software
- Stack: Java 21, Spring Boot 3.3, PostgreSQL 16, JUnit 5, Testcontainers, JaCoCo, WireMock, JMeter e SonarCloud (condicional).
- Branch candidata avaliada: `correcao/teste-csrf-migracao-v8` (ainda não integrada à `main`)
- Base: `main`

## Resumo executivo

A versão candidata contém os 14 requisitos funcionais do enunciado implementados no código, com testes associados e matriz em `RTM.md`. Foram acrescentados controle de titularidade nas consultas e histórico, auditoria de recursos/usuários, interface com telas por perfil, relatórios e notificações.

**Status: versão candidata, ainda não homologada integralmente.** O CI [43107077159](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43107077159) de 11/11/2026 executou **174 testes sem falhas**, com **91,26%** de cobertura de linhas e **75,61%** de branches, ambas acima das metas. Esses indicadores correspondem à revisão anterior às correções de autorização e movimentação por material; executar novamente o CI no SHA final. SonarCloud não executou por falta de configuração, JMeter foi executado e aprovado no [CI 42711044633](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42711044633): 20 tentativas, uma reserva criada e 19 conflitos; a interface ainda necessita de aceitação visual.

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
| Build Maven / testes | 100% verdes no commit final | 174 testes aprovados na execução 43107077159; revalidar após as mudanças de 12/11 |
| Cobertura de linhas | >= 80% | 91,26% (731/801) — meta atingida no CI 43107077159 |
| Cobertura de branches | >= 70% | 75,61% (310/410) — meta atingida no CI 43107077159 |
| SonarCloud | Sem bugs/vulnerabilidades críticas | Integração condicional; falta configurar token/identificadores e executar |
| Teste de carga | 20 threads; uma criação e demais conflitos para mesmo slot | **Aprovado** no CI 42711044633: 1 HTTP 201, 19 HTTP 409, média 106,9 ms e P95 209 ms. JTL e HTML disponíveis como artefatos. |
| E2E de navegador | Fluxo visual completo | Não implementado; E2E HTTP cobre integração entre camadas |
| Responsividade | 360px, 768px e desktop | Checklist manual pendente |

## Correções adicionais de homologação — 12/11/2026

- **RF-07:** separação entre administrador e responsável nas aprovações; só RESPONSAVEL aprova reserva com sala, professor ou material adicional restrito. Testes de API e integração adicionados; execução CI desta revisão pendente.
- **RF-09:** retirada e devolução individual de materiais adicionais, com conclusão da reserva somente após todas as devoluções; Flyway V9 preserva os registros existentes. Testes de integração adicionados; resta verificar a migração real no banco de homologação.
- **RF-12:** relatório passou a contabilizar materiais adicionais; validado manualmente na porta 8084 (projetor: 1 reserva/60 minutos).
- **RF-13:** etiqueta clara de indisponibilidade em manutenção; a validação no navegador ainda está pendente.
- **Entrega:** nenhuma dessas alterações foi aplicada à aplicação original da porta 8080. Ainda faltam integração no branch final, revisão por pares, SonarCloud executado, testes visuais de celular/tablet e evidências finais.

## Riscos residuais

1. Migração em banco legado pode falhar se houver recursos duplicados normalizados; fazer backup e executar limpeza antes de migrar.
2. HTTP Basic exige HTTPS fora de localhost.
3. O relatório de qualidade só pode ser finalizado depois dos resultados reais dos indicadores.
4. O enunciado exige evidência verificável, não apenas configuração de ferramentas.

## Conclusão

A implementação funcional foi ampliada substancialmente, mas esta versão permanece candidata. O responsável pela entrega deve confirmar o CI no SHA final com a regra JaCoCo, configurar/rodar SonarCloud, preservar os artefatos JMeter/JaCoCo e concluir a validação visual/manual e revisão por pares antes de integrar à `main`.
