# ADR-002 — Aprovação de recursos restritos e movimentação individual de materiais

**Estado:** aceito na branch de homologação (requer validação operacional antes de integrar à main).  
**Data:** 12/11/2026  
**Requisitos:** RF-01, RF-07, RF-09, RF-10 e RF-13.

## Contexto

O enunciado determina que apenas o perfil RESPONSAVEL pode aprovar recursos restritos, que cada retirada/devolução de material precisa ser registrada e que toda transição de estado seja auditada. A versão anterior permitia ao ADMINISTRADOR aprovar recursos restritos e não registrava movimentações independentes de materiais adicionais de uma sala.

## Decisões

1. `RESPONSAVEL` e `ADMINISTRADOR` podem avaliar reservas. Quando a sala, o professor vinculado ou qualquer material adicional é restrito, **somente RESPONSAVEL pode aprovar**. O servidor valida a função recebida da autenticação e não confia em dados do navegador.
2. O perfil RESPONSAVEL enxerga os pedidos pendentes e não ganha acesso à administração de usuários ou recursos.
3. Cada movimentação pertence ao par único `(reserva_id, recurso_id)`; a migração Flyway V9 substitui apenas a restrição de unicidade antiga por essa chave composta e **preserva os registros existentes**.
4. Retirada do primeiro material promove a reserva de APROVADA para EM_USO. Retiradas adicionais são permitidas durante EM_USO. A devolução do último material efetivamente retirado, com todas as movimentações requeridas registradas e devolvidas, promove a reserva para CONCLUIDA.
5. Endpoints antigos `/reservas/{id}/retirada` e `/devolucao` continuam válidos quando existe exatamente um material. Endpoints `/reservas/{id}/materiais/{materialId}/...` identificam explicitamente o material nas reservas conjuntas.
6. A consulta de movimentações é exclusiva de RESPONSAVEL e ADMINISTRADOR. Professores não podem acessar esse histórico administrativo.
7. A interface apresenta o estado da manutenção e oferece ações coerentes com o perfil. A API impõe a autorização independentemente do HTML.

## Alternativas descartadas

- Permitir aprovação de recurso restrito pelo ADMINISTRADOR: contraria a regra do professor.
- Registrar uma retirada para toda a sala mesmo com vários materiais: impede saber o que voltou e o que está pendente.
- Alterar a migração V3 já aplicada: quebraria a validação Flyway; por isso a mudança é a V9.

## Consequências e verificações

- Banco de homologação com V8 precisa receber V9; utilizar **cópia isolada do banco**, não a base já compartilhada pelas portas 8082/8083/8084.
- Testes automatizados: `AutorizacaoApiTest`, `ReservaControllerTest`, `ReservaServiceIntegracaoTest` e `FluxoCompletoE2ETest`. CI: https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43217218310
- Verificar manualmente aprovação especial, múltiplas retiradas/devoluções, bloqueio ao professor e histórico. Guardar capturas e atualizar RTM/relatório final.
