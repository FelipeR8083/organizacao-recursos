# Plano de validação e rastreabilidade

> Este documento registra o que é verificável no código desta branch. A lista oficial dos 14 requisitos do trabalho não está versionada no repositório consultado; portanto, não atribui IDs nem declara cobertura integral sem a especificação do docente.

## Escopo validável atualmente

| ID técnico | Critério verificável | Evidência automatizada |
|---|---|---|
| VAL-01 | Período exige início/fim e fim posterior ao início | `PeriodoTest`, `ReservaServiceIntegracaoTest` |
| VAL-02 | Períodos adjacentes são aceitos | `ReservaServiceIntegracaoTest.reservasQueApenasSeTocamSaoAceitas` |
| VAL-03 | Períodos sobrepostos no mesmo recurso são recusados | `ReservaServiceIntegracaoTest` |
| VAL-04 | Recursos diferentes podem ser reservados no mesmo horário | `ReservaServiceIntegracaoTest.mesmoHorarioEmRecursosDiferentesEhPermitido` |
| VAL-05 | Recurso em manutenção não recebe reserva | `ReservaConcorrenciaTest.naoPermiteReservarRecursoEmManutencao` |
| VAL-06 | Solicitações simultâneas para o mesmo recurso não geram dupla reserva | `ReservaConcorrenciaTest.solicitacoesSimultaneasProduzemUmaUnicaReserva` |
| VAL-07 | Transições obedecem à máquina de estados | `EstadoReservaTest`, `ReservaTest` |
| VAL-08 | Criação e transições geram auditoria transacional | `ReservaServiceIntegracaoTest.criaAuditoriaNaCriacaoEEmCadaMudancaDeEstado` |
| VAL-09 | Consulta de auditoria retorna 404 para reserva inexistente | `ReservaControllerTest.reservaInexistenteNaConsultaDeAuditoriaRetorna404` |
| VAL-10 | Cadastro/alteração recusam nome duplicado normalizado por tipo | `RecursoControllerDuplicidadeTest` |
| VAL-11 | Contrato HTTP valida entrada e traduz erros para status documentados | `ReservaControllerTest` |
| VAL-12 | Compose valida configuração e a imagem Docker é construída no CI | workflow `.github/workflows/ci.yml` |

## Checklist manual de demonstração

- [ ] Subir os serviços com `docker compose up --build -d`.
- [ ] Abrir `http://localhost:8080`.
- [ ] Cadastrar uma sala e confirmar que aparece na listagem.
- [ ] Tentar cadastrar o mesmo nome/tipo com diferenças de caixa e espaços; confirmar conflito.
- [ ] Criar uma reserva para um intervalo livre.
- [ ] Tentar criar uma reserva sobreposta para o mesmo recurso; confirmar conflito.
- [ ] Criar reserva adjacente; confirmar que é aceita.
- [ ] Colocar um recurso em manutenção e tentar reservá-lo; confirmar bloqueio.
- [ ] Aprovar uma reserva e avançá-la para em uso e concluída.
- [ ] Consultar o histórico e conferir criação e transições.
- [ ] Conferir logs e encerrar com `docker compose down`.

## Lacunas que impedem declarar o trabalho completo

- [ ] Receber e transcrever a especificação oficial dos 14 requisitos.
- [ ] Relacionar cada requisito oficial a implementação, teste e evidência de demonstração.
- [ ] Implementar autenticação e autorização conforme os perfis e regras definidos pelo docente.
- [ ] Definir a política de aprovação para recursos restritos.
- [ ] Implementar retirada/devolução de materiais, caso exigido na especificação.
- [ ] Identificar a pessoa responsável nos eventos de auditoria após implementar autenticação.
- [ ] Substituir `ddl-auto: update` por migrações versionadas e testadas.
- [ ] Executar testes ponta a ponta e de carga com cenários e metas acordados.
- [ ] Configurar análise SonarCloud e registrar o resultado, se fizer parte da avaliação.

## Critério de conclusão

A conclusão só deve ser declarada quando os 14 requisitos oficiais estiverem mapeados, todos os critérios obrigatórios tiverem evidência, o pipeline da revisão estiver verde e as limitações aceitas estiverem registradas. Testes automatizados não substituem validação de requisitos nem homologação.
