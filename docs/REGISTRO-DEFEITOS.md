# Registro de defeitos e riscos

Este registro consolida problemas encontrados durante a revisão do código. "Fechado" significa que existe implementação nesta branch; a validação final depende do CI e da demonstração.

| ID | Prioridade | Problema observado | Correção nesta branch | Estado |
|---|---|---|---|---|
| DEF-01 | Crítica | API sem autenticação/autorização | Spring Security, BCrypt, papéis e bootstrap administrativo; testes de 401/403 e HTTP Basic | Corrigido no código; CI pendente |
| DEF-02 | Alta | Reserva de sala não vinculava professor, permitindo conflito cruzado na agenda docente | Professor vinculado à reserva, bloqueio ordenado de ambos os recursos e teste de concorrência cruzada | Corrigido no código; CI pendente |
| DEF-03 | Alta | Auditoria registrava apenas canal técnico e não distinguia alteração de reserva | Login autenticado como origem; tipo/detalhes de evento para reagendamento; auditoria em transições e movimentações | Parcial; auditar CRUD de recursos/usuários |
| DEF-04 | Alta | Schema dependia de Hibernate ddl-auto update, sem histórico versionado | Flyway V1–V4 e Hibernate ddl-auto validate | Corrigido no código; validar migração em banco existente com dados |
| DEF-05 | Alta | Não havia registro de retirada/devolução de material | Entidade, serviço, endpoints e testes com Testcontainers | Corrigido no código; CI pendente |
| DEF-06 | Média | Ausência de notificação e relatório de utilização/conflitos | Notificação simulada/webhook opcional com teste WireMock; relatórios e testes de integração | Corrigido no código; CI pendente |
| DEF-07 | Alta | Não havia reagendamento com nova checagem de conflitos | PUT de reserva SOLICITADA com verificação de titularidade, conflito, auditoria e teste | Corrigido no código; CI pendente |
| DEF-08 | Média | Pesquisa de recursos não tinha metadados/filtros | Capacidade, localização, competências e disponibilidade | Parcial; validar filtros na interface e HTTP |
| RSK-01 | Alta | Cobertura mínima 80% linhas / 70% branches ainda não comprovada | Relatório JaCoCo no CI | Aberto até medir e atingir metas |
| RSK-02 | Alta | SonarCloud depende de token e variáveis da equipe | Etapa condicional no workflow | Aberto até configurar SONAR_TOKEN, SONAR_ORGANIZATION e SONAR_PROJECT_KEY |
| RSK-03 | Média | Teste JMeter existe, mas não foi executado nem possui resultado | Plano e JMX versionados | Aberto até anexar JTL/HTML e métricas |
| RSK-04 | Média | Responsividade ainda não possui evidência em tamanhos/dispositivos | CSS responsivo existente | Aberto até executar checklist visual |
| RSK-05 | Média | Teste E2E cobre fluxo HTTP completo, não navegação real do navegador | TestRestTemplate + PostgreSQL Testcontainers | Parcial; E2E de navegador não implementado |

## Política de triagem

- Crítica: bloqueia autenticação, integridade ou dupla reserva; corrigir antes de qualquer demonstração pública.
- Alta: impede requisito funcional ou pode gerar alocação incorreta; corrigir antes da entrega.
- Média: afeta evidência, observabilidade, usabilidade ou meta de qualidade; registrar plano e resultado.
- Não marcar risco como fechado apenas por existir um commit: exigir teste verde e evidência de execução.
