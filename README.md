# Organização de Recursos

Sistema para alocar **salas, professores e materiais** sem conflitos de horário.
Projeto de Qualidade de Software — Senac TADS 2026.2.

## Stack
Java 21 · Spring Boot 3.3 · PostgreSQL · JUnit 5 · Testcontainers · JaCoCo

## Como executar
```bash
docker run -d --name recursos-db -e POSTGRES_DB=recursos -e POSTGRES_USER=recursos \
  -e POSTGRES_PASSWORD=recursos -p 5432:5432 postgres:16-alpine
mvn spring-boot:run
```

## Testes
```bash
mvn verify   # requer Docker (Testcontainers); relatório em target/site/jacoco
```

## Decisões de design
- **Intervalo semiaberto `[início, fim)`**: reservas que apenas se tocam (10h–11h e 11h–12h) não conflitam.
- **Dupla reserva**: `SELECT ... FOR UPDATE` na linha do recurso (`PESSIMISTIC_WRITE`) serializa solicitações concorrentes; provado em `ReservaConcorrenciaTest`.
- **Estados** em `EstadoReserva`; estados finais não têm saída e `EM_USO` não pode ser cancelada.

## Status dos requisitos (RTM resumida)
| # | Requisito | Status |
|---|-----------|--------|
| 4 | Criar reserva | Parcial (alterar/cancelar pendentes) |
| 5 | Sobreposição de horários | Feito por recurso (agenda do professor vinculada à sala pendente) |
| 6 | Proteção contra dupla reserva | Feito + teste |
| 8 | Bloqueio por manutenção | Feito + teste |
| 1,2,3,7,9–14 | Demais | Pendente |

## Próximos passos
Autenticação por perfil, CRUD de recursos, aprovação de restritos, auditoria,
retirada/devolução, WireMock, JMeter, SonarCloud, RTM.md completa e diagramas C4.
