# Análise técnica para conferência de Felipe — reservas, materiais e persistência

**Projeto:** Gestão de Recursos Acadêmicos | **Elaborado em:** 12/11/2026  
**Preparação:** análise estática/documental assistida por IA, baseada no código da `main` e nas evidências históricas do GitHub.  
**Destinatário sugerido para conferir e assumir, se concordar:** Felipe (`FelipeR8083`).  
**Situação:** **RASCUNHO TÉCNICO — NÃO É REVISÃO POR PARES ASSINADA NEM APROVAÇÃO DE FELIPE.**  
**Escopo temporal:** implementação inicial (PRs #1–#3), correções e evidências finais (PRs #4–#7). Não retroagir datas.  
**Autoria do código:** não foi estabelecido quem implementou cada trecho; antes de apresentar como revisão por pares, confirmar que o revisor não está simplesmente avaliando o próprio código.

## 1. Trechos examinados e método

Inspeção de fonte e testes, **sem executar testes novos**:
- [`ReservaService.java`](../src/main/java/com/senac/recursos/servico/ReservaService.java) — criação, alteração, cancelamento, transições, histórico.
- [`ReservaRepository.java`](../src/main/java/com/senac/recursos/repositorio/ReservaRepository.java) — consultas de conflito e `PESSIMISTIC_WRITE` na reserva.
- [`MovimentacaoMaterialService.java`](../src/main/java/com/senac/recursos/servico/MovimentacaoMaterialService.java) — retirada/devolução.
- [`EstadoReserva.java`](../src/main/java/com/senac/recursos/dominio/EstadoReserva.java) — estados e transições.
- [`RelatorioService.java`](../src/main/java/com/senac/recursos/servico/RelatorioService.java) — métricas.
- [`V9__movimentacoes_multiplos_materiais.sql`](../src/main/resources/db/migration/V9__movimentacoes_multiplos_materiais.sql) — migração.
- [`ReservaConcorrenciaTest.java`](../src/test/java/com/senac/recursos/servico/ReservaConcorrenciaTest.java), [RTM](../RTM.md) e [registro de defeitos](REGISTRO-DEFEITOS.md).

## 2. Constatações positivas verificadas no código

**F-01 — Proteção da criação concorrente.** Em `ReservaService.criar(...)`, os IDs de sala, professor e materiais são inseridos em `TreeSet` e bloqueados em ordem crescente via `recursos.buscarComLock`; depois há checagens de conflito para cada recurso. É uma estratégia adequada para serializar solicitações concorrentes dos mesmos recursos. O teste `solicitacoesSimultaneasProduzemUmaUnicaReserva` exercita **10 tentativas** com PostgreSQL Testcontainers; a [evidência do JMeter pós-CSRF](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844059) documenta **20 POST, 1 HTTP 201 e 19 HTTP 409** na `main`. Isso não comprova todos os casos de corrida de alteração e criação combinadas.

**F-02 — Devolução individual de materiais.** `MovimentacaoMaterialService` seleciona um material pertencente à reserva, impede retirada duplicada, restringe retirada aos estados `APROVADA/EM_USO`, grava devolução e só solicita conclusão automática quando **todos** os materiais foram devolvidos. `ReservaService.alterarEstado` possui verificação adicional para impedir conclusão manual com materiais pendentes.

**F-03 — Relatórios e auditoria.** `RelatorioService.utilizacao` considera recurso principal, professor e materiais vinculados, contabiliza interseção temporal da reserva com o período consultado e exclui estados não ativos para contabilização. `ReservaService` registra auditoria de criação, alteração, cancelamento e transição. A especificação das métricas deve deixar claro que a contagem de reservas e os minutos referem-se ao intervalo consultado.

## 3. Achados e ressalvas para revisão humana

| ID | Severidade preliminar | Constatação (código/evidência) | Recomendação ao revisor |
| --- | --- | --- | --- |
| **RF-REV-01** | **Alta — risco a investigar, ainda não reproduzido** | O fluxo `ReservaService.alterar(...)` bloqueia a reserva e o recurso principal/professor, mas consulta os **materiais já vinculados sem adquirir seus locks explícitos**; `criar(...)` bloqueia todos os materiais. Um reagendamento concorrente com uma nova reserva sobre o mesmo material pode merecer teste específico para excluir condição de corrida. | Solicitar teste de integração com duas transações simultâneas: reagendar uma reserva com material vinculado e criar outra reserva conflitante para o mesmo material em sala diferente; garantir que as duas não sejam aceitas. Se necessário, padronizar aquisição de locks em ordem global. |
| **RF-REV-02** | **Média/alta — comportamento conhecido, reproduzido na homologação histórica** | `EstadoReserva` permite `APROVADA → EM_USO → CONCLUIDA` sem verificar o relógio; o [relatório de qualidade](RELATORIO-QUALIDADE.md) registra início/conclusão antecipados. Como `CONCLUIDA` não faz parte dos estados que bloqueiam conflitos, vale verificar o impacto em datas futuras. | Definir a regra temporal com o professor antes de modificar. Acrescentar testes de início, conclusão e prevenção de dupla alocação após conclusão antecipada. |
| **RF-REV-03** | **Alta — evidência insuficiente** | A migração Flyway V9 troca a unicidade de movimentação por reserva para unicidade por `(reserva_id, recurso_id)`. A documentação registra homologação em banco novo, **sem ensaio V8→V9 sobre base populada**. | Manter risco aberto e planejar ensaio sobre cópia/backup restaurado, avaliando unicidades e histórico de movimentos; não alterar banco original sem autorização. |
| **RF-REV-04** | **Baixa — oportunidade de desempenho** | `listarPara` chama `reservas.findAll().stream().filter(...)` para filtrar as reservas do solicitante em memória. | Considerar filtro no repositório por solicitante para bases grandes e verificar privacidade em testes de API. Não há benchmark que confirme gargalo hoje. |

**Os itens de risco não são apresentados como falhas reproduzidas quando não há teste confirmatório.**

## 4. Parecer a ser realizado pela pessoa, não pela IA

Após verificar o código, os testes e os achados acima, Felipe deve escrever observações **com palavras próprias** e dizer quais conclusões aceita, discorda ou deseja investigar. Se não participou da implementação daquele trecho, pode registrar sua revisão independente; caso tenha sido o autor, não deve classificá-la como revisão por pares de terceiro.

- [ ] Li os trechos referenciados e conferi as evidências.
- [ ] Verifiquei os pontos RF-REV-01 a RF-REV-04 e registrei minhas conclusões.
- [ ] Identifiquei quais partes foram implementadas por outro integrante.
- [ ] Registrei meus próprios comentários no GitHub, com data verdadeira, sobre trechos realmente escritos pelo outro integrante.

**Data da conferência por Felipe:** pendente.  
**Achados aceitos/rejeitados pelo revisor:** pendente.  
**Manifestação de Felipe sobre o código alheio (comentário ou revisão em PR aberto por outra pessoa):** pendente. **Felipe é autor do PR #8 e não pode aprovar formalmente o próprio pull request.** Para review de GitHub formal por Felipe, é necessário um PR de outro autor (por exemplo, código submetido por Pietro), caso exista.  
**Link do comentário ou da revisão enviada pela própria conta:** pendente.

**Nota de escopo:** esta preparação cobre código e evidências de toda a evolução, mas é **retrospectiva**. Não constitui revisão humana durante o desenvolvimento, conforme exigência informada pelo docente.
