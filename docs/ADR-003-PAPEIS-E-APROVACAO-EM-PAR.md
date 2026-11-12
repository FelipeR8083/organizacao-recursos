# ADR-003 — Papéis de administração e gestão de reservas

**Estado:** proposta em homologação — substitui a política de aprovação descrita na ADR-002.  
**Data:** 12/11/2026  
**Origem:** detalhamento de responsabilidades validado pela equipe (Felipe/Pietro) e perfis do enunciado de Qualidade 2026.2.

## Modelo adotado

| Perfil | Responsabilidade |
|---|---|
| SOLICITANTE | Professor ou coordenador: consulta disponibilidade e solicita/altera/cancela suas reservas |
| PROFESSOR | Mantido como login de professor, com as mesmas permissões de reserva do SOLICITANTE |
| RESPONSAVEL | Pode solicitar suas reservas, avaliar pedidos de outras pessoas, aprovar/rejeitar reservas comuns ou restritas, registrar início/fim, não comparecimento e retirada/devolução dos materiais |
| ADMINISTRADOR | Gerencia usuários, salas, professores, materiais, bloqueios e manutenção. Consulta reservas e indicadores para acompanhamento, mas **não muda seus estados nem registra movimentações** |

## Regra de independência (quatro olhos)

A pessoa autenticada com o perfil RESPONSAVEL **não pode aprovar nem rejeitar uma reserva que ela própria solicitou**. A verificação usa login autenticado na API, comparação sem distinção de maiúsculas/minúsculas e identidade do solicitante gravada na reserva, nunca um campo editável enviado pelo navegador.

Exemplo: Responsável A solicita Sala 101 → permanece SOLICITADA → Responsável B aprova → APROVADA. Se A tentar aprovar/rejeitar o próprio pedido, recebe HTTP 403 e a reserva permanece SOLICITADA. Auditoria de mudança de estado só é criada em transição aceita.

## Separação de funções

- O acesso a `PATCH /reservas/{id}/estado` é somente para RESPONSAVEL. Inclui aprovação, rejeição, início, conclusão e marcação de ausência; as transições continuam sujeitas à máquina de estados.
- Apenas RESPONSAVEL registra `POST /reservas/{id}/retirada`, `/devolucao` e os endpoints de materiais adicionais.
- O ADMINISTRADOR ainda pode consultar movimentações e reservas como supervisão, mas não executar as transições.
- O cadastro de usuários/recurso/manutenção continua exclusivo de ADMINISTRADOR.
- Professor/coordenador não ganha acesso administrativo; perfil `SOLICITANTE` atende coordenador, enquanto `PROFESSOR` mantém o cadastro de professores existente. Não é necessário criar uma migração ou novo valor no enum para isso.

## Testes e riscos

- `ReservaServiceIntegracaoTest.responsavelNaoPodeAprovarNemRejeitarPropriaReserva` testa comparação insensível a caixa, ausência de auditoria após rejeição da ação e aprovação posterior por outro responsável.
- `AutorizacaoApiTest.administradorNaoPodeMudarEstadoNemMovimentarMateriais` verifica 403 no HTTP, independentemente do navegador.
- `FluxoCompletoE2ETest` percorre a criação da reserva por um responsável, autoaprovação negada e aprovação por outro responsável com PostgreSQL real.
- Conferir manualmente a tela por perfil em ambiente separado, revisão por pares e o CI da branch antes de integrar o comportamento à `main`.

Esta mudança deliberadamente restringe o administrador mais do que o mínimo exigido pelo docente para reservas comuns, em atendimento à decisão de organização de papéis da equipe.
