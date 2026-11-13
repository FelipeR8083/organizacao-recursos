# Organização de Recursos

Sistema acadêmico para alocar **salas, professores e materiais** sem conflitos de horário.

Projeto de Qualidade de Software — Senac TADS 2026.2.

> **Para avaliação e entrega:** comece pelo [Guia de entrega e evidências verificáveis](docs/ENTREGA-FINAL.md). Ele reúne RTM, testes, JaCoCo, SonarCloud, JMeter, diagramas, revisão retrospectiva da dupla, roteiro de demonstração e pendências honestamente identificadas.

## Autoria e colaboração

Este projeto acadêmico foi desenvolvido em dupla por:

- **Felipe** — [@FelipeR8083](https://github.com/FelipeR8083)
- **Pietro** — [@pietrolaureano](https://github.com/pietrolaureano)

O planejamento, desenvolvimento e os testes foram realizados em conjunto. Como a dupla compartilhou um computador durante o trabalho, parte dos commits foi enviada por uma única conta do GitHub. Por isso, o nome que aparece como autor de um commit não representa, sozinho, toda a colaboração no projeto.

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

**Não use `docker compose down -v` para atualizar o sistema:** esse comando remove os volumes gerenciados pelo Compose e pode apagar o PostgreSQL. Se já existe um banco, confira o volume e faça backup antes de iniciar outra versão. Veja [o guia de atualização segura](README-NOVA-VERSAO.md).

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

Na interface, o **administrador** gerencia pessoas, recursos e manutenção; o **responsável** avalia solicitações (comuns ou restritas), registra início/fim, ausência e movimentações de materiais. O responsável também pode reservar, **mas outro responsável deve aprovar sua solicitação**. Professores e coordenadores atuam como solicitantes (contas `PROFESSOR` ou `SOLICITANTE`) e veem apenas as próprias reservas. O administrador pode consultar reservas para auditoria, mas não muda seus estados nem registra retiradas/devoluções. É possível solicitar uma sala com vários materiais na mesma reserva. A aba de relatórios usa o período selecionado pelo usuário.

### Banco atualizado de demonstracao (Flyway V9)

Para a entrega academica, o repositorio inclui [um seed SQL de **dados ficticios** e instrucoes de reproducao](demo/README.md). Ele cria uma base **isolada** (`recursos_demo_v9`) com recursos e reservas de exemplo, sem expor os dados nem as senhas do banco real. **O seed nao deve ser executado no banco original `recursos`.** As migracoes oficiais V1–V9 continuam sendo aplicadas automaticamente pelo Flyway.

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
| PATCH | `/reservas/{id}/estado` | Somente RESPONSAVEL muda estados, incluindo aprovar, rejeitar, iniciar, concluir e não compareceu; não pode aprovar/rejeitar reserva própria |
| POST | `/reservas/{id}/retirada` | Somente RESPONSAVEL registra retirada de material aprovado |
| POST | `/reservas/{id}/devolucao` | Devolução de reserva com um único material, concluindo após a devolução |
| GET | `/reservas/movimentacoes` | RESPONSAVEL registra movimentações; ADMINISTRADOR pode consultar o histórico |
| POST | `/reservas/{id}/materiais/{materialId}/retirada` | Somente RESPONSAVEL registra retirada de cada material de uma reserva conjunta |
| POST | `/reservas/{id}/materiais/{materialId}/devolucao` | Somente RESPONSAVEL registra devolução; conclui quando todos os materiais voltam |
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

O plano e o script Apache JMeter estão em `docs/PLANO-TESTE-CARGA.md` e `jmeter/reservas-concorrentes.jmx`. A **evidência atual pós-CSRF** é o [JMeter na main #43316844059](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844059): **20 GET /csrf HTTP 200**, 20 POST concorrentes (**1 HTTP 201, 19 HTTP 409**), **média 38,5 ms, P95 117 ms e máximo 122 ms** para os POST. Artefatos JTL e HTML disponíveis. A execução [#42711044633](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42711044633) é histórica, **anterior ao CSRF obrigatório**.

## Testes e cobertura

Execute a suíte completa:

```bash
mvn clean verify
```

Os testes de integração usam Testcontainers e precisam de Docker. O relatório JaCoCo é gerado em `target/site/jacoco/index.html`. No [CI de referência final do código na main](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844286), **188 testes passaram, sem falhas ou erros**, cobertura JaCoCo de **91,85% de linhas** e **78,37% de branches**; compilação/build Docker e validações automatizadas documentadas. O Maven verifica as metas de 80% linhas / 70% branches. Os números pertencem ao SHA da execução indicada; atualização posterior requer novo CI verde.

O workflow do GitHub Actions executa `mvn -B verify` em pull requests e pushes para `main` e para a branch de melhorias, e publica o relatório como artefato quando disponível. SonarCloud é executado quando `SONAR_TOKEN` (secret) e `SONAR_ORGANIZATION`/`SONAR_PROJECT_KEY` (variables) estiverem configuradas nas configurações do repositório.

## Decisões de design

- **Intervalo semiaberto [início, fim):** reservas adjacentes, como 10h–11h e 11h–12h, não conflitam.
- **Concorrência na criação:** bloqueia sala/recurso principal, professor vinculado e todos os materiais solicitados em ordem global crescente de ID, validando conflitos de horários entre eles.
- **Concorrência nas transições:** o bloqueio pessimista da linha da reserva serializa alterações de estado simultâneas.
- **Máquina de estados:** transições são definidas em `EstadoReserva`; estados finais não permitem novas transições.
- **Auditoria transacional:** criação, mudança de estado, reagendamento e cancelamento geram eventos persistidos; quando autenticado, o login é registrado como origem. Cadastro/edição de recursos e gestão de usuários geram eventos administrativos imutáveis sem senhas.
- **Nomes de recursos:** validação normalizada por tipo e nome, complementada por restrição de unicidade no banco conforme o script de correção.

## Status conhecido e limitações

Implementado e coberto por testes no código atual:
- Criação de reservas e validação de período.
- Prevenção de sobreposição por sala, professor vinculado e múltiplos materiais.
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
- A execução 557 passou; confirme novamente o CI do SHA final antes de integrar atualizações posteriores.

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
