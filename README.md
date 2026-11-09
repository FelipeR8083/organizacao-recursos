# Organização de Recursos

Sistema acadêmico para alocar **salas, professores e materiais** sem conflitos de horário.

Projeto de Qualidade de Software — Senac TADS 2026.2.

## Tecnologias

- Java 21
- Spring Boot 3.3.5
- Spring Data JPA e Hibernate
- Spring Security HTTP Basic e BCrypt
- Flyway (migrações SQL versionadas)
- Springdoc OpenAPI / Swagger UI
- PostgreSQL 16
- Maven
- JUnit 5, Spring Boot Test e Testcontainers
- JaCoCo
- WireMock
- Apache JMeter

## Pré-requisitos

- JDK 21
- Maven 3.9+
- Docker Desktop ou Docker Engine em execução

## Como executar

O caminho recomendado é o Docker Compose, que inicia **banco e aplicação juntos**. Não execute um contêiner PostgreSQL separado com o mesmo nome ou porta enquanto usar o Compose.

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

O Compose publica aplicação e PostgreSQL apenas em 127.0.0.1. DB_PASSWORD e as credenciais APP_ADMIN_* são obrigatórias via .env. O primeiro administrador é criado automaticamente na primeira inicialização. A API usa HTTP Basic; cadastre os perfis adicionais pela aba **Usuários** da interface (ou pela API administrativa `/usuarios`) usando as credenciais do administrador. Senhas são armazenadas com BCrypt e devem ter pelo menos 12 caracteres. Use HTTPS fora do ambiente local. O webhook é opcional: configure `NOTIFICACAO_URL` para envio HTTP; sem ele, as notificações ficam no modo simulado.

Execute diretamente com Maven configurando as credenciais (PowerShell):

```powershell
$env:DB_PASSWORD='senha-do-banco'
$env:APP_ADMIN_USERNAME='admin'
$env:APP_ADMIN_PASSWORD='senha-admin-com-12-caracteres'
mvn spring-boot:run
```

No Linux/macOS, exporte as mesmas três variáveis antes de executar `mvn spring-boot:run`.

A aplicação ficará disponível em `http://localhost:8080`. A interface web está em `src/main/resources/static/index.html`. O navegador pode solicitar as credenciais HTTP Basic; para comparar perfis distintos, abra sessões separadas (por exemplo, janela anônima).

Na interface, o administrador vê cadastro de recursos e usuários; o responsável vê aprovação, retirada/devolução, relatórios e notificações; o solicitante vê apenas suas reservas e ações autorizadas. A aba de relatórios usa o período selecionado pelo usuário.

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
| PUT | `/reservas/{id}` | Titular altera reserva solicitada; responsável/administrador pode gerenciar |
| PATCH | `/reservas/{id}/estado` | Responsável/administrador altera estado respeitando a máquina de estados |
| POST | `/reservas/{id}/retirada` | Responsável registra retirada de material aprovado |
| POST | `/reservas/{id}/devolucao` | Responsável registra devolução e conclui a reserva |
| DELETE | `/reservas/{id}` | Solicitante cancela a própria reserva; responsável/administrador pode cancelar |
| GET | `/sessao` | Retorna login e perfil da sessão autenticada |
| GET | `/usuarios` | Administrador lista usuários |
| POST | `/usuarios` | Administrador cadastra usuário e perfil |
| PATCH | `/usuarios/{id}/ativo` | Administrador ativa/desativa usuário |
| GET | `/reservas/{id}/auditoria` | Histórico restrito ao titular ou gestor |
| GET | `/auditoria-administrativa` | Administrador consulta alterações de recursos e usuários |
| GET | `/relatorios/utilizacao?inicio=...&fim=...` | Reservas e minutos alocados por recurso |
| GET | `/relatorios/conflitos?inicio=...&fim=...` | Conflitos de horário evitados |
| GET | `/notificacoes` | Administrador/responsável consulta notificações simuladas |

Erros de validação retornam HTTP 400; conflitos de horário e transições inválidas retornam HTTP 409; recurso ou reserva inexistente retorna HTTP 404; regras de negócio inválidas retornam HTTP 422.

## Teste de carga

O plano e o script Apache JMeter estão em `docs/PLANO-TESTE-CARGA.md` e `jmeter/reservas-concorrentes.jmx`. O workflow [JMeter #42711044633](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42711044633) executou 20 reservas concorrentes: **1 HTTP 201, 19 HTTP 409**, média de 106,9 ms e P95 de 209 ms. As evidências JTL, HTML e resumo estão nos artefatos da execução.

## Testes e cobertura

Execute a suíte completa:

```bash
mvn clean verify
```

Os testes de integração usam Testcontainers e precisam de Docker. O relatório JaCoCo é gerado em `target/site/jacoco/index.html`. No CI aprovado [42708687473](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42708687473), 115 testes passaram, com **87,81% de linhas** e **71,64% de branches**. O Maven agora verifica automaticamente as metas mínimas de 80%/70%; confirmar o resultado no SHA final após atualizações.

O workflow do GitHub Actions executa `mvn -B verify` em pull requests e pushes para `main` e para a branch de melhorias, e publica o relatório como artefato quando disponível. SonarCloud é executado quando `SONAR_TOKEN` (secret) e `SONAR_ORGANIZATION`/`SONAR_PROJECT_KEY` (variables) estiverem configuradas nas configurações do repositório.

## Decisões de design

- **Intervalo semiaberto [início, fim):** reservas adjacentes, como 10h–11h e 11h–12h, não conflitam.
- **Concorrência na criação:** bloqueia recurso principal e professor vinculado em ordem crescente de ID e valida conflitos em ambos.
- **Concorrência nas transições:** o bloqueio pessimista da linha da reserva serializa alterações de estado simultâneas.
- **Máquina de estados:** transições são definidas em `EstadoReserva`; estados finais não permitem novas transições.
- **Auditoria transacional:** criação, mudança de estado, reagendamento e cancelamento geram eventos persistidos; quando autenticado, o login é registrado como origem. Cadastro/edição de recursos e gestão de usuários geram eventos administrativos imutáveis sem senhas.
- **Nomes de recursos:** validação normalizada por tipo e nome, complementada por restrição de unicidade no banco conforme o script de correção.

## Status conhecido e limitações

Implementado e coberto por testes no código atual:
- Criação de reservas e validação de período.
- Prevenção de sobreposição por recurso e professor vinculado.
- Autenticação e autorização por perfil.
- Pesquisa por tipo, capacidade, localização, competência e disponibilidade.
- Alteração/cancelamento de reserva solicitada pelo titular.
- Retirada e devolução de materiais/equipamentos.
- Notificações simuladas ou por webhook opcional.
- Relatórios de utilização e conflitos evitados.
- Migrações SQL versionadas com Flyway.
- Proteção contra dupla reserva concorrente.
- Bloqueio de recursos em manutenção.
- Máquina de estados das reservas.
- Validação de nome duplicado de recurso.
- Registro e consulta do histórico de criação/transições (branch de melhorias).

Os **14 requisitos funcionais** têm implementação no código da branch de melhorias; consulte `RTM.md` para evidências e ressalvas de aceitação. A entrega acadêmica **ainda não está homologada integralmente**:
- SonarCloud precisa ser conectado e analisado para comprovar ausência de bugs/vulnerabilidades críticas.
- O cenário JMeter foi executado e aprovado; os artefatos devem ser guardados junto da entrega.
- A interface necessita de validação manual em celular/tablet e demonstração da Swagger UI.
- Revisão por pares, evidências finais e apresentação devem ser concluídas.
- O último CI precisa terminar verde no SHA final, incluindo a regra de JaCoCo; consulte Actions antes de integrar.

**Segurança:** HTTP Basic com BCrypt e papéis está implementado nesta branch. O ambiente Compose limita as portas ao localhost; não exponha publicamente sem HTTPS, gestão de segredos e revisão de segurança.

## Correção de dados duplicados existentes

Se o banco já possuir recursos duplicados, leia primeiro `LEIA-PRIMEIRO-DUPLICADOS.md`. Faça backup antes de executar `CORRIGIR-DUPLICADOS.sql`. Não rode o script sem revisar os dados e confirmar o backup.

## Rastreabilidade e documentação

- Matriz de requisitos e evidências: [RTM.md](RTM.md).
- Arquitetura e operação: [docs/ARQUITETURA-E-OPERACAO.md](docs/ARQUITETURA-E-OPERACAO.md).
- Diagramas de sequência: [docs/DIAGRAMAS-SEQUENCIA.md](docs/DIAGRAMAS-SEQUENCIA.md).
- Plano de testes: [docs/PLANO-DE-TESTES.md](docs/PLANO-DE-TESTES.md).
- Registro de defeitos: [docs/REGISTRO-DEFEITOS.md](docs/REGISTRO-DEFEITOS.md).
- Relatório de qualidade (versão candidata): [docs/RELATORIO-QUALIDADE.md](docs/RELATORIO-QUALIDADE.md).
- Roteiro de apresentação: [docs/APRESENTACAO.md](docs/APRESENTACAO.md).
- Plano de validação: [docs/PLANO-DE-VALIDACAO.md](docs/PLANO-DE-VALIDACAO.md).
- Configuração segura do SonarCloud: [docs/CONFIGURAR-SONARCLOUD.md](docs/CONFIGURAR-SONARCLOUD.md).
- Exemplos dos endpoints e permissões: [docs/EXEMPLOS-API.md](docs/EXEMPLOS-API.md).
- Checklist de responsividade e acessibilidade: [docs/TESTE-RESPONSIVIDADE.md](docs/TESTE-RESPONSIVIDADE.md).
- Cenários BDD e evidência de defeito corrigido: [docs/EVIDENCIA-BDD.md](docs/EVIDENCIA-BDD.md).

A RTM distingue requisitos implementados, parciais e pendentes conforme o enunciado oficial. Não considerar o sistema pronto para produção ou a entrega totalmente homologada enquanto as evidências de SonarCloud, carga, revisão de segurança e validação visual não forem concluídas.

### Documentação interativa da API

Com a aplicação em execução, acesse `http://localhost:8080/swagger-ui/index.html` para consultar os endpoints e o contrato OpenAPI em `/v3/api-docs`.
