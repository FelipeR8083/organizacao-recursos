# Relatório de qualidade — candidata à entrega (12/11/2026)

## Identificação e decisão

- Projeto: **Gestão de Recursos Acadêmicos** — Senac TADS / Qualidade de Software.
- Equipe: **Felipe e Pietro** (ver autoria e créditos no repositório).
- Stack: Java 21, Spring Boot 3.3, PostgreSQL 16, JUnit 5, Testcontainers, WireMock, JaCoCo e JMeter.
- Versão verificada: branch `ajuste/perfis-responsavel-aprovacao`, commit `a099e4daf248ffc3699e492070d7ca955d8464e9`.
- Base destinada à entrega: `main`, por meio de pull request com revisão por pares.
- **Decisão:** versão funcional candidata à entrega acadêmica **com ressalvas explicitadas**. Não há prova de cumprimento integral até executar a análise SonarCloud, registrar revisão por pares e anexar as evidências finais. Não afirmar ausência de vulnerabilidades críticas sem análise real.

## Resultado automatizado (evidência verificada)

| Indicador | Meta | Resultado |
|---|---|---|
| CI / Maven / suíte de testes | Aprovação | **184 testes, 0 falhas, 0 erros, 0 ignorados** |
| Cobertura de linhas (JaCoCo) | ≥ 80% | **91,91% (773/841)** |
| Cobertura de branches (JaCoCo) | ≥ 70% | **77,88% (338/434)** |
| Build Docker no CI | Concluir | **Sucesso** |
| Banco real / integração | Testcontainers | Testes de integração PostgreSQL concluídos |
| Notificações webhook | WireMock | Testes automatizados concluídos |
| SonarQube Cloud / SonarCloud | Análise e ausência de alertas críticos | **Não comprovado: etapa pulada (skipped)** |

Fonte: [GitHub Actions 43279334663 — CI verde no SHA candidato](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43279334663). JaCoCo e resultados da suíte estão disponíveis nos logs e artefatos da execução.

**Carga concorrente:** [GitHub Actions 42711044633](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42711044633), JMeter com 20 pedidos simultâneos ao mesmo recurso: **1 HTTP 201 e 19 HTTP 409**, média de 106,9 ms, P95 209 ms. Plano `jmeter/reservas-concorrentes.jmx`; arquivar os relatórios JTL/HTML junto da entrega.

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
- **Conclusão prematura:** a interface agora oculta `CONCLUIDA` enquanto há material pendente; o servidor impede a transição manual nesse caso. Foram incluídos testes para material principal e reserva com dois materiais. **CI verde, mas nova imagem com esse ajuste ainda não foi validada manualmente na porta 8085**.
- **Bloqueio por manutenção:** validado na interface e diretamente na API (HTTP 422).

## Riscos e pendências reais

1. **SonarCloud:** falta executar análise (workflow preparado, mas etapa `skipped` sem token/organização/projeto). Meta do professor de zero bugs e vulnerabilidades críticas **não está verificada**. Guia: `docs/CONFIGURAR-SONARCLOUD.md`.
2. **Revisão por pares:** realizar revisão real pelo Pietro no pull request final; registrar a decisão/comentários.
3. **Evidências/apresentação:** guardar captura do CI final e indicadores JaCoCo, JMeter JTL/HTML, telas desktop/celular (360px e 768px), diagramas/RTM; roteiro em `docs/APRESENTACAO.md`.
4. **Atualização de base legada:** Flyway V9 foi observado em **banco novo**, não sobre uma cópia de banco V8 com dados reais; preservar backup antes de uma futura atualização de produção.
5. **Regra temporal:** o sistema permitiu antecipar `APROVADA → EM_USO` e concluir reserva antes do horário agendado. Registrar como risco/decisão pendente; não afirmar que essa restrição está implementada.
6. **Execução final:** confirmar CI no SHA efetivamente integrado à `main` e preservar evidência.
7. **Conta professor:** domínio Senac é validado; confirmação real de e-mail requer SMTP/configuração de produção e não foi verificada na homologação.

## Referências

- [Especificação do professor](https://afonsolelis.github.io/aulas_senac/pages/qualidade2/especificacao-projeto.html)
- [RTM](../RTM.md)
- [Plano de testes](PLANO-DE-TESTES.md)
- [Registro de defeitos](REGISTRO-DEFEITOS.md)
- [Homologação e checklist](HOMOLOGACAO-FINAL-2026-11-12.md)
- [Roteiro de apresentação](APRESENTACAO.md)

**Conclusão:** a equipe demonstrou funcionamento de grande parte do escopo exigido e atingiu as metas automatizadas de cobertura. A entrega como versão candidata é tecnicamente justificável **com os riscos explicitados**; o relatório não certifica cumprimento de 100% dos requisitos de evidência nem qualidade de produção.
