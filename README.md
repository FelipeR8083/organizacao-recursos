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

O Compose publica aplicação e PostgreSQL apenas em 127.0.0.1. DB_PASSWORD e as credenciais APP_ADMIN_* são obrigatórias via .env. O primeiro administrador é criado automaticamente na primeira inicialização.

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
| PATCH | `/reservas/{id}/estado` | Altera estado respeitando a máquina de estados |
| GET | `/reservas/{id}/auditoria` | Consulta histórico de criação e transições |

Erros de validação retornam HTTP 400; conflitos de horário e transições inválidas retornam HTTP 409; recurso ou reserva inexistente retorna HTTP 404; regras de negócio inválidas retornam HTTP 422.

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
- **Auditoria transacional:** criação e mudança de estado geram eventos persistidos na mesma transação da operação. A origem atual é registrada como `API`; ainda não representa um usuário autenticado.
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
- Autenticação e autorização por perfil.
- Identificação do usuário responsável por cada ação de auditoria.
- Política de autorização para aprovar reservas de recursos restritos.
- Retirada e devolução de materiais.
- Migrações versionadas do banco (o projeto ainda usa `ddl-auto: update`).
- Rastreabilidade completa dos 14 requisitos, testes de ponta a ponta e carga, e análise SonarCloud.

**Segurança:** esta versão não possui autenticação nem autorização. Não exponha a aplicação publicamente nem utilize dados pessoais ou operacionais reais.

## Correção de dados duplicados existentes

Se o banco já possuir recursos duplicados, leia primeiro `LEIA-PRIMEIRO-DUPLICADOS.md`. Faça backup antes de executar `CORRIGIR-DUPLICADOS.sql`. Não rode o script sem revisar os dados e confirmar o backup.

## Rastreabilidade e documentação

- Matriz de requisitos e evidências: [RTM.md](RTM.md).
- Arquitetura e operação: [docs/ARQUITETURA-E-OPERACAO.md](docs/ARQUITETURA-E-OPERACAO.md).
- Plano de validação: [docs/PLANO-DE-VALIDACAO.md](docs/PLANO-DE-VALIDACAO.md).

A RTM distingue requisitos implementados, parciais e pendentes conforme o enunciado oficial. Não considerar o sistema pronto para produção enquanto autenticação/autorização e demais lacunas registradas na matriz não forem resolvidas.

### Documentação interativa da API

Com a aplicação em execução, acesse `http://localhost:8080/swagger-ui/index.html` para consultar os endpoints e o contrato OpenAPI em `/v3/api-docs`.
