# Continuidade do projeto — 11/11/2026

## Repositório e ponto de retomada
- Repositório: FelipeR8083/organizacao-recursos
- Branch de trabalho: melhorias/qualidade-e-requisitos
- Commit verificado durante esta revisão: 9873207d1b9d5bfda263325021008e495adaf013
- Esta nota registra o estado encontrado no GitHub; não é evidência de build ou testes executados.

## Requisitos combinados com o usuário
1. Login moderno e separado para administrador e professores.
2. Autocadastro de professor com e-mail institucional Senac e senha forte.
3. Conta nova fica inativa até aprovação do administrador.
4. Professor autenticado escolhe sala, materiais e horários, solicita reserva e acompanha suas próprias solicitações.
5. Reserva começa SOLICITADA; somente administrador aprova ou rejeita.
6. Administrador gerencia recursos, manutenção, usuários e reservas.
7. Permissões precisam ser aplicadas na API, não apenas ocultadas na interface.
8. Preservar PostgreSQL e reservas já existentes.

## O que foi identificado no código publicado
- Há login.html e cadastro.html.
- CadastroProfessorController cria solicitação; UsuarioService.solicitarCadastroProfessor valida domínio senac.br e subdomínios, senha mínima de 12 caracteres e salva perfil PROFESSOR inativo.
- UsuarioService.alterarAtivo exige e-mail confirmado quando verificação é obrigatória.
- EmailConfirmacaoProfessorService implementa envio por SMTP, condicionado a EMAIL_ENVIO_HABILITADO; padrão em application.yml é false.
- SegurancaConfig possui regras de perfil, login de formulário e HTTP Basic. CSRF é desativado fora do perfil prod; no perfil prod é habilitado com exceções explícitas. Revisar riscos antes de publicação externa.
- Existem testes de autorização, confirmação de e-mail, CSRF, integração de reservas e E2E no repositório. Não foram executados nesta revisão.
- docs/LOGIN-PROFESSORES.md contém instruções e checklist, mas algumas observações são anteriores às alterações recentes (ex.: afirma que confirmação por e-mail ainda não existe); revisar documentação.

## Pendências antes de declarar pronto
- Executar Maven/testes e Docker, corrigir falhas reais e registrar resultados.
- Validar cadastro com domínio permitido/proibido, confirmação por e-mail habilitada e desabilitada, aprovação de conta, login/logout e bloqueio de professor inativo.
- Validar isolamento entre professores, permissões de API, aprovação/rejeição pelo admin e controle de materiais e salas.
- Testar CSRF com perfil prod, sessões, logout, e-mails e configuração SMTP em ambiente real.
- Verificar se materiais são selecionáveis e vinculados às reservas como o usuário deseja.
- Testar interface manualmente nos dois perfis, inclusive visual do login e responsividade.
- Preservar dados: nunca executar docker compose down -v. Antes de migrações, fazer backup do PostgreSQL.
- Atualizar docs/LOGIN-PROFESSORES.md e registrar commits após ajustes.

## Retomada no computador
Na pasta do projeto, verificar git status -sb e git log -1 --oneline. Fazer backup dos dados antes de atualizar containers. Após conferir mudanças locais, atualizar a branch com git pull origin melhorias/qualidade-e-requisitos e testar docker compose up --build -d.

## Regra de comunicação
Não afirmar que algo está pronto sem evidência de execução e testes. Não sobrescrever alterações locais não publicadas.
