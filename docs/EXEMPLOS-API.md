# API — exemplos de uso e critérios de autorização

**Ambiente local:** http://localhost:8080. Autenticação HTTP Basic. Use HTTPS para qualquer ambiente fora do localhost.

Swagger UI: `http://localhost:8080/swagger-ui/index.html` · OpenAPI JSON: `http://localhost:8080/v3/api-docs`.

## Preparação

Copie `.env.example` para `.env`, configure `DB_PASSWORD`, `APP_ADMIN_USERNAME` e `APP_ADMIN_PASSWORD` (mínimo de 12 caracteres), execute `docker compose up --build -d` e cadastre os perfis pelo endpoint administrativo.

Exemplo para criar um solicitante (substitua `ADMIN` e `SENHA` localmente; não versione senhas):

```bash
curl --user "$ADMIN:$SENHA" -H 'Content-Type: application/json' \
  -d '{"login":"solicitante1","senha":"substitua-esta-senha-123","perfil":"SOLICITANTE"}' \
  http://localhost:8080/usuarios
```

## Recursos

Cadastro por ADMINISTRADOR:

```http
POST /recursos
Content-Type: application/json

{"tipo":"SALA","nome":"Laboratório 2","restrito":false,"emManutencao":false,"capacidade":30,"localizacao":"Bloco A","competencias":"Java e redes"}
```

Pesquisa por metadados (qualquer pessoa autenticada):

```http
GET /recursos?tipo=SALA&capacidadeMinima=20&localizacao=Bloco%20A&competencia=java
```

Pesquisa por disponibilidade (instantes em UTC; períodos adjacentes não conflitam):

```http
GET /recursos?tipo=SALA&inicio=2026-12-01T10:00:00Z&fim=2026-12-01T11:00:00Z
```

Se apenas `inicio` ou `fim` for informado, o endpoint retorna erro. Recursos em manutenção e recursos com reservas ativas conflitantes não aparecem na busca de disponibilidade.

## Reservas

Solicitação por usuário autenticado (o servidor substitui `solicitante` pelo login autenticado):

```http
POST /reservas
Content-Type: application/json

{"recursoId":1,"professorId":2,"solicitante":"solicitante1","inicio":"2026-12-01T10:00:00Z","fim":"2026-12-01T11:00:00Z"}
```

Resposta esperada em caso de sucesso: HTTP 201 com identificador e estado `SOLICITADA`. Conflito de horário: HTTP 409. `professorId` é opcional, mas quando informado deve apontar para recurso do tipo PROFESSOR.

Reagendamento de reserva ainda solicitada pelo titular ou gestor:

```http
PUT /reservas/1
Content-Type: application/json

{"recursoId":1,"professorId":2,"inicio":"2026-12-01T13:00:00Z","fim":"2026-12-01T14:00:00Z"}
```

Cancelamento por titular ou gestor:

```http
DELETE /reservas/1
```

Aprovação de reservas comuns por RESPONSAVEL ou ADMINISTRADOR. Para sala, professor vinculado **ou material adicional** marcado como restrito, a aprovação é exclusiva de RESPONSAVEL. O administrador recebe HTTP 403 ao tentar aprovar esse pedido:

```http
PATCH /reservas/1/estado
Content-Type: application/json

{"estado":"APROVADA"}
```

Histórico auditável:

```http
GET /reservas/1/auditoria
```

## Materiais e relatórios

RESPONSAVEL/ADMINISTRADOR pode registrar retirada de material aprovado e devolução do material em uso:

```http
POST /reservas/1/retirada
POST /reservas/1/devolucao
```

Para uma reserva de **sala com materiais adicionais**, utilize um movimento por material. Se a sala possuir dois materiais, cada retirada e devolução deverá apontar o ID do material correspondente:

```http
GET /reservas/movimentacoes
POST /reservas/1/materiais/2/retirada
POST /reservas/1/materiais/3/retirada
POST /reservas/1/materiais/2/devolucao
POST /reservas/1/materiais/3/devolucao
```

O estado muda para `EM_USO` na primeira retirada e só muda para `CONCLUIDA` após **todos os materiais associados** serem devolvidos. Somente RESPONSAVEL/ADMINISTRADOR têm acesso. Um material não vinculado à reserva é recusado; tentar retirar o mesmo material duas vezes também é recusado. A migração `V9` preserva movimentações anteriores, sem permitir duplicidade do mesmo material na mesma reserva.

Relatórios disponíveis para RESPONSAVEL/ADMINISTRADOR:

```http
GET /relatorios/utilizacao?inicio=2026-12-01T00:00:00Z&fim=2026-12-02T00:00:00Z
GET /relatorios/conflitos?inicio=2026-12-01T00:00:00Z&fim=2026-12-02T00:00:00Z
GET /notificacoes
```

## Status HTTP e regras

| HTTP | Significado |
|---|---|
| 200/201/204 | Consulta, criação ou cancelamento bem-sucedidos |
| 400 | JSON ou campos obrigatórios inválidos |
| 401 | Credenciais ausentes ou inválidas |
| 403 | Papel ou titularidade sem permissão |
| 404 | Recurso ou reserva inexistente |
| 409 | Horário conflitante, estado inválido ou restrição de integridade |
| 422 | Regra de negócio/intervalo inválido ou recurso indisponível |

Os exemplos são documentação de contrato. A evidência de execução real está nos testes E2E e no GitHub Actions; não confundir exemplos com capturas de testes executados.
