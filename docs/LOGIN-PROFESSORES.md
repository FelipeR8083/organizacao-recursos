# Acesso de professores e aprovação de reservas

## Novo fluxo de uso

1. O administrador entra pela nova tela `http://localhost:8080/login.html`.
2. Na aba **Usuários**, cadastra um usuário com perfil **PROFESSOR**, informando login e senha inicial (mínimo de 12 caracteres). Compartilhe a senha por canal privado e troque-a se for exposta.
3. O professor entra com as próprias credenciais, vai em **Reservas**, seleciona sala, professor vinculado (opcional), período e clica em **Solicitar reserva**.
4. A reserva é registrada no estado **SOLICITADA**, sem aprovação automática.
5. O administrador entra na própria conta e vê a seção **Solicitações aguardando aprovação**, na parte superior da tela, com botões **Aprovar** e **Rejeitar**.
6. O administrador continua podendo cadastrar/editar recursos, colocar sala em manutenção e liberar manutenção, administrar usuários e consultar relatórios.
7. O professor vê somente suas reservas e históricos. Não consegue cadastrar salas, colocar em manutenção, aprovar ou rejeitar solicitações de outros usuários.
8. A aprovação de uma sala marcada como restrita é permitida ao ADMINISTRADOR e ao RESPONSAVEL, preservando o perfil existente.

## Comportamento da autenticação

- O acesso pelo navegador agora utiliza uma página de login personalizada com sessão autenticada.
- A API mantém autenticação HTTP Basic para ferramentas e testes existentes.
- O botão **Sair da conta** encerra a sessão do navegador.
- O banco PostgreSQL continua persistido no volume existente; **não** use `docker compose down -v` para atualizar.
- Esta versão mantém a configuração anterior de CSRF desabilitado para compatibilidade com os endpoints REST; é destinada a execução local (porta 8080 vinculada a 127.0.0.1). Reavaliar CSRF antes de expor publicamente.

## Atualizar no computador Windows

Na pasta `C:\Users\felip\organizacao-recursos`, com Docker Desktop aberto:

```powershell
git pull origin melhorias/qualidade-e-requisitos
docker compose up --build -d
docker compose ps
```

Para enxergar a nova tela de login mesmo que o navegador tenha memorizado credenciais HTTP Basic, abra uma janela anônima e acesse `http://localhost:8080/login.html`.

## Cenário para validação manual

- [ ] Login visual do admin e acesso à aba Usuários.
- [ ] Admin cadastra usuário PROFESSOR.
- [ ] Professor efetua login em janela anônima e solicita sala livre.
- [ ] Professor vê o estado SOLICITADA, mas não vê botões para aprovar/rejeitar.
- [ ] Admin acessa o painel de pendências e aprova a solicitação.
- [ ] Professor atualiza a tela e encontra o estado APROVADA.
- [ ] Professor não consegue cadastrar recursos nem alterar manutenção.
- [ ] Admin coloca sala em manutenção e nova solicitação é recusada.
- [ ] Botão Sair encerra sessão.

**Observação:** a visualização foi implementada, mas este checklist de navegador só pode ser marcado após execução e captura real.
