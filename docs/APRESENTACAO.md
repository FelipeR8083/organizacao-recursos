# Roteiro de apresentação — Organização de Recursos

## Slide 1 — Problema e objetivo
- Conflitos na alocação de salas, professores e materiais.
- Objetivo: registrar reservas, impedir dupla alocação e controlar utilização.

## Slide 2 — Escopo e requisitos
- Apresentar os 14 requisitos funcionais.
- Mostrar a matriz RTM e explicar status/evidências.
- Destacar o que está implementado e o que depende de validação final.

## Slide 3 — Arquitetura
- Monólito Spring Boot / Java 21.
- Interface web, API REST, serviços, JPA e PostgreSQL.
- C4 contexto, contêineres e componentes.

## Slide 4 — Modelo de domínio
- Recurso: sala, professor, material.
- Reserva e professor vinculado.
- Intervalos semiabertos [início, fim).
- Estados da reserva.

## Slide 5 — Segurança
- HTTP Basic, BCrypt e perfis SOLICITANTE, RESPONSAVEL e ADMINISTRADOR.
- Regra: somente RESPONSAVEL aprova recurso restrito.
- Auditoria registra identidade autenticada.

## Slide 6 — Concorrência e integridade
- Lock pessimista em recursos na ordem de ID.
- Conflito consultado para recurso principal e professor.
- Mostrar teste simultâneo e resultado 201/409.

## Slide 7 — Ciclo da reserva e materiais
- Solicitação, aprovação, retirada, uso, devolução e conclusão.
- Reagendamento e cancelamento com titularidade.
- Histórico e notificação.

## Slide 8 — Relatórios e documentação
- Minutos alocados por recurso.
- Conflitos evitados.
- Swagger UI e RTM.

## Slide 9 — Estratégia de qualidade
- JUnit parametrizado, Testcontainers, MockMvc, E2E HTTP, WireMock.
- JaCoCo, SonarCloud e JMeter: apresentar apenas resultados efetivamente gerados.

## Slide 10 — Demonstração e conclusão
1. Login como administrador e cadastro de perfis.
2. Cadastro de sala, professor e projetor.
3. Criar duas reservas conflitantes com o mesmo professor.
4. Aprovar reserva restrita como RESPONSAVEL.
5. Registrar retirada e devolução do projetor.
6. Consultar auditoria, notificações e relatórios.
7. Mostrar GitHub Actions, RTM e limitações/riscos residuais.

## Evidências para anexar antes da apresentação

- Captura do GitHub Actions verde no SHA final.
- Relatório HTML/XML JaCoCo com metas atingidas.
- Dashboard/resultado SonarCloud.
- Relatório HTML e arquivo JTL do JMeter.
- Capturas da interface em 360px, 768px e desktop.
- RTM atualizada e registro de defeitos.
