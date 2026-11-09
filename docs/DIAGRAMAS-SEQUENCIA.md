# Diagramas de componentes e sequências críticas

## Componentes (C4 — nível 3 simplificado)

```mermaid
flowchart TB
  UI[HTML / JavaScript]
  subgraph API[Camada REST]
    RC[RecursoController]
    BC[ReservaController]
    UC[UsuarioController]
    MC[MovimentacaoMaterialController]
    REL[RelatorioController]
    NC[NotificacaoController]
  end
  subgraph APP[Serviços de aplicação]
    RS[ReservaService]
    US[UsuarioService]
    MS[MovimentacaoMaterialService]
    RLS[RelatorioService]
    NS[NotificacaoService]
    CES[ConflitoEvitadoService]
  end
  subgraph DATA[Persistência]
    JPA[Spring Data JPA]
    PG[(PostgreSQL)]
  end
  UI --> API
  API --> APP
  APP --> JPA
  JPA --> PG
  NS -. webhook opcional .-> EXT[API externa]
```

## Sequência — solicitação de reserva com professor

```mermaid
sequenceDiagram
  actor S as Solicitante
  participant SEC as Spring Security
  participant API as ReservaController
  participant APP as ReservaService
  participant DB as PostgreSQL
  participant AUD as Auditoria/Notificação
  S->>SEC: POST /reservas (HTTP Basic)
  SEC->>API: identidade autenticada + papel
  API->>APP: recurso, professor e período
  APP->>APP: valida intervalo
  APP->>DB: bloqueia recursos em ordem crescente
  APP->>DB: consulta conflitos de sala/material e professor
  alt conflito detectado
    APP->>DB: registra conflito evitado em transação independente
    APP-->>S: HTTP 409
  else sem conflito
    APP->>DB: grava reserva SOLICITADA
    APP->>AUD: grava auditoria e notificação
    APP-->>S: HTTP 201
  end
```

## Sequência — aprovação de recurso restrito

```mermaid
sequenceDiagram
  actor R as Responsável
  participant SEC as Spring Security
  participant API as ReservaController
  participant APP as ReservaService
  participant DB as PostgreSQL
  R->>SEC: PATCH /reservas/{id}/estado
  SEC->>API: identidade autenticada
  API->>APP: aprovar reserva
  APP->>DB: bloqueia linha da reserva
  APP->>DB: verifica se recurso principal/professor é restrito
  alt restrito e perfil não é RESPONSAVEL
    APP-->>R: HTTP 403
  else autorizado e transição válida
    APP->>DB: atualiza estado e grava auditoria
    APP->>DB: grava notificação (ou webhook configurado)
    APP-->>R: HTTP 200
  end
```

## Sequência — retirada e devolução

```mermaid
sequenceDiagram
  actor R as Responsável
  participant API as MovimentacaoMaterialController
  participant APP as MovimentacaoMaterialService
  participant DB as PostgreSQL
  R->>API: POST /reservas/{id}/retirada
  API->>APP: retirar(reserva, responsável)
  APP->>DB: bloqueia reserva
  APP->>DB: valida MATERIAL e estado APROVADA
  APP->>DB: transiciona para EM_USO, cria movimentação e auditoria
  APP-->>R: movimentação criada
  R->>API: POST /reservas/{id}/devolucao
  API->>APP: devolver(reserva, responsável)
  APP->>DB: registra devolução e transiciona para CONCLUIDA
  APP->>DB: grava auditoria
  APP-->>R: movimentação concluída
```
