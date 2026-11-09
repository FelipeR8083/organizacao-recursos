# Relatório de qualidade — versão candidata

## Identificação

- Projeto: Organização de Recursos Acadêmicos
- Curso: Senac TADS — Qualidade de Software
- Stack: Java 21, Spring Boot 3.3, PostgreSQL 16, JUnit 5, Testcontainers, JaCoCo, WireMock, JMeter e SonarCloud (condicional).
- Branch avaliada: `melhorias/qualidade-e-requisitos`
- Base: `main`

## Resumo executivo

A versão candidata contém autenticação por perfil, gestão de recursos, reserva de sala com professor vinculado, prevenção de conflitos, reagendamento/cancelamento, aprovação restrita, retirada/devolução de materiais, auditoria, notificações simuladas/webhook e relatórios. A documentação de requisitos está em `RTM.md`.

**Status: NÃO APROVADO COMO ENTREGA FINAL AINDA.** A última execução de CI que foi inspecionada falhou na compilação do teste WireMock porque o construtor do servidor usava uma chamada ambígua a `options()`. A chamada foi corrigida em commit posterior; é obrigatório aguardar a execução do novo commit antes de declarar o build verde. Também faltam resultados reais de cobertura, SonarCloud e JMeter.

## Evidências automatizadas versionadas

- Testes de domínio parametrizados: período e máquina de estados.
- Integração com PostgreSQL Testcontainers: reserva, conflitos, auditoria, pesquisa, relatórios, movimentação e notificações.
- Concorrência: múltiplas solicitações para o mesmo recurso.
- Segurança/API: autenticação Basic, 401/403 e papéis.
- Fluxo ponta a ponta HTTP: cadastro, solicitação, aprovação, auditoria, relatório e notificação.
- WireMock: webhook de notificação com resposta aceita e falha simulada.

Consulte [PLANO-DE-TESTES.md](PLANO-DE-TESTES.md) e [REGISTRO-DEFEITOS.md](REGISTRO-DEFEITOS.md).

## Indicadores que ainda precisam de resultado

| Indicador | Meta/critério | Resultado atual |
|---|---|---|
| Build Maven / testes | 100% verdes no commit final | Aguardando reexecução após correção do teste WireMock |
| Cobertura de linhas | >= 80% | Não medida/aprovada nesta revisão |
| Cobertura de branches | >= 70% | Não medida/aprovada nesta revisão |
| SonarCloud | Sem bugs/vulnerabilidades críticas | Integração condicional; falta configurar token/identificadores e executar |
| Teste de carga | 20 threads; uma criação e demais conflitos para mesmo slot | Plano/JMX versionados; execução pendente |
| E2E de navegador | Fluxo visual completo | Não implementado; E2E HTTP cobre integração entre camadas |
| Responsividade | 360px, 768px e desktop | Checklist manual pendente |

## Riscos residuais

1. Migração em banco legado pode falhar se houver recursos duplicados normalizados; fazer backup e executar limpeza antes de migrar.
2. HTTP Basic exige HTTPS fora de localhost.
3. O relatório de qualidade só pode ser finalizado depois dos resultados reais dos indicadores.
4. O enunciado exige evidência verificável, não apenas configuração de ferramentas.

## Conclusão

A implementação funcional foi ampliada substancialmente, mas esta versão permanece candidata. O responsável pela entrega deve aguardar CI verde no SHA final, gerar e arquivar JaCoCo, configurar/rodar SonarCloud, executar JMeter e concluir validação visual/manual antes de integrar à `main`.
