# Organização de Recursos

Sistema acadêmico para alocar **salas, professores e materiais** sem conflitos de horário.

Projeto de Qualidade de Software — Senac TADS 2026.2.

## Tecnologias

- Java 21
- Spring Boot 3.3.5
- Spring Data JPA e Hibernate
- PostgreSQL 16
- Maven
- JUnit 5, Spring Boot Test e Testcontainers
- JaCoCo

## Pré-requisitos

- JDK 21
- Maven 3.9+
- Docker Desktop ou Docker Engine em execução

## Como executar

Inicie o banco de dados PostgreSQL:

```bash
docker run -d --name recursos-db \
  -e POSTGRES_DB=recursos \
  -e POSTGRES_USER=recursos \
  -e POSTGRES_PASSWORD=recursos \
  -p 5432:5432 postgres:16-alpine
```

### Executar tudo com Docker Compose

Copie `.env.example` para `.env` na raiz do projeto e substitua o valor por uma senha forte (não envie `.env` ao Git):

```dotenv
DB_PASSWORD=escolha-uma-senha-local
APP_ADMIN_USERNAME=admin
APP_ADMIN_PASSWORD=defina-uma-senha-com-12-caracteres-ou-mais
```

Suba PostgreSQL e aplicação:

```bash
docker compose up --build -d
```

Acesse a interface em http://localhost:8080. Para acompanhar os logs:

```bash
docker compose logs -f app
```

Para parar os serviços, preservando os dados do banco:

```bash
docker compose down
```

Para apagar também o volume de dados (operação destrutiva):

```bash
docker compose down -v
```

O Compose publica aplicação e PostgreSQL apenas em 127.0.0.1. DB_PASSWORD e as credenciais APP_ADMIN_* são obrigatórias via .env. O primeiro administrador é criado automaticamente na primeira inicialização. A API usa HTTP Basic; cadastre os perfis adicionais pela API administrativa `/usuarios` usando as credenciais do administrador. Senhas são armazenadas com BCrypt e devem ter pelo menos 12 caracteres. Use HTTPS fora do ambiente local.

Execute a aplicação diretamente com Maven:

```bash
mvn spring-boot:run
```

A aplicação ficará disponível em `http://localhost:8080`. A interface web está em `src/main/resources/static/index.html`.

### Configuração do banco

As variáveis de ambiente aceitas são:

| Variável | Padrão |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/recursos` |
| `DB_USER` | `recursos` |
| `DB_PASSWORD` | sem valor padrão |

Defina `DB_PASSWORD` no ambiente antes de iniciar a aplicação.

## API REST

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/recursos` | Lista recursos |
| POST | `/recursos` | Cadastra recurso |
| PUT | `/recursos/{id}` | Atualiza recurso |
| GET | `/reservas` | Lista reservas |
| POST | `/reservas` | Solicita reserva |
| PATCH | `/reservas/{id}/estado` | Responsável/administrador altera estado respeitando a máquina de estados |
| DELETE | `/reservas/{id}` | Solicitante cancela a própria reserva; responsável/administrador pode cancelar |
| GET | `/usuarios` | Administrador lista usuários |
| POST | `/usuarios` | Administrador cadastra usuário e perfil |
| PATCH | `/usuarios/{id}/ativo` | Administrador ativa/desativa usuário |
| GET | `/reservas/{id}/auditoria` | Consulta histórico de criação e transições |
| GET | `/relatorios/utilizacao?inicio=...&fim=...` | Reservas e minutos alocados por recurso |
| GET | `/relatorios/conflitos?inicio=...&fim=...` | Conflitos de horário evitados |
| GET | `/notificacoes` | Administrador/responsável consulta notificações simuladas |

Erros de validação retornam HTTP 400; conflitos de horário e transições inválidas retornam HTTP 409; recurso ou reserva inexistente retorna HTTP 404; regras de negócio inválidas retornam HTTP 422.

## Teste de carga

O plano e o script Apache JMeter estão em `docs/PLANO-TESTE-CARGA.md` e `jmeter/reservas-concorrentes.jmx`. O resultado ainda precisa ser executado e anexado; não há métricas de carga aprovadas neste repositório.

## Testes e cobertura

Execute a suíte completa:

```bash
mvn clean verify
```

Os testes de integração usam Testcontainers e precisam de Docker. O relatório JaCoCo é gerado em `target/site/jacoco/index.html`.

O workflow do GitHub Actions executa `mvn -B verify` em pull requests e pushes para `main`, e publica o relatório como artefato quando disponível.

## Decisões de design

- **Intervalo semiaberto [início, fim):** reservas adjacentes, como 10h–11h e 11h–12h, não conflitam.
- **Concorrência na criação:** o bloqueio pessimista da linha do recurso serializa solicitações simultâneas para o mesmo recurso.
- **Concorrência nas transições:** o bloqueio pessimista da linha da reserva serializa alterações de estado simultâneas.
- **Máquina de estados:** transições são definidas em `EstadoReserva`; estados finais não permitem novas transições.
- **Auditoria transacional:** criação, mudança de estado e cancelamento geram eventos persistidos na mesma transação; quando autenticado, o login é registrado como origem.
- **Nomes de recursos:** validação normalizada por tipo e nome, complementada por restrição de unicidade no banco conforme o script de correção.

## Status conhecido e limitações

Implementado e coberto por testes no código atual:
- Criação de reservas e validação de período.
- Prevenção de sobreposição por recurso.
- Proteção contra dupla reserva concorrente.
- Bloqueio de recursos em manutenção.
- Máquina de estados das reservas.
- Validação de nome duplicado de recurso.
- Registro e consulta do histórico de criação/transições (branch de melhorias).

Ainda não deve ser considerado concluído:
- Alteração de período/recurso de reservas ainda não implementada.
- Aprovação de recurso restrito é protegida pelo papel RESPONSAVEL, mas requer testes de regra de negócio específicos.
- Retirada e devolução de materiais.
- Migrações versionadas do banco (o projeto ainda usa `ddl-auto: update`).
- Rastreabilidade completa dos 14 requisitos, testes de ponta a ponta e carga, e análise SonarCloud.

**Segurança:** HTTP Basic com BCrypt e papéis está implementado nesta branch. O ambiente Compose limita as portas ao localhost; não exponha publicamente sem HTTPS, gestão de segredos e revisão de segurança.

## Correção de dados duplicados existentes

Se o banco já possuir recursos duplicados, leia primeiro `LEIA-PRIMEIRO-DUPLICADOS.md`. Faça backup antes de executar `CORRIGIR-DUPLICADOS.sql`. Não rode o script sem revisar os dados e confirmar o backup.

## Rastreabilidade e documentação

- Matriz de requisitos e evidências: [RTM.md](RTM.md).
- Arquitetura e operação: [docs/ARQUITETURA-E-OPERACAO.md](docs/ARQUITETURA-E-OPERACAO.md).
- Plano de validação: [docs/PLANO-DE-VALIDACAO.md](docs/PLANO-DE-VALIDACAO.md).

A RTM distingue requisitos implementados, parciais e pendentes conforme o enunciado oficial. Não considerar o sistema pronto para produção enquanto autenticação/autorização e demais lacunas registradas na matriz não forem resolvidas.

### Documentação interativa da API

Com a aplicação em execução, acesse `http://localhost:8080/swagger-ui/index.html` para consultar os endpoints e o contrato OpenAPI em `/v3/api-docs`.
