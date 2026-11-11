# Acesso de professores e aprovação de reservas

## Novo fluxo de uso

1. O administrador entra pela nova tela `http://localhost:8080/login.html`.
2. O professor acessa `http://localhost:8080/cadastro.html`, informa seu e-mail institucional `@senac.br` (ou subdomínio institucional) e define uma senha de pelo menos 12 caracteres. A conta é criada **inativa**, aguardando análise do administrador. O administrador também pode cadastrar usuários manualmente na aba **Usuários**.\n3. O administrador entra em **Usuários** e ativa a conta do professor após verificar sua identidade e vínculo institucional. **A aplicação ainda não envia e-mail de confirmação nem prova automaticamente a posse do endereço informado.**
4. O professor entra com as próprias credenciais, vai em **Reservas**, seleciona sala, professor vinculado (opcional), período e clica em **Solicitar reserva**.
5. A reserva é registrada no estado **SOLICITADA**, sem aprovação automática.
6. O administrador entra na própria conta e vê a seção **Solicitações aguardando aprovação**, na parte superior da tela, com botões **Aprovar** e **Rejeitar**.
7. O administrador continua podendo cadastrar/editar recursos, colocar sala em manutenção e liberar manutenção, administrar usuários e consultar relatórios.
8. O professor vê somente suas reservas e históricos. Não consegue cadastrar salas, colocar em manutenção, aprovar ou rejeitar solicitações de outros usuários.
9. A aprovação ou rejeição de reservas pela API é exclusiva do **ADMINISTRADOR**, inclusive para recursos restritos.

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
- [ ] Professor se cadastra com e-mail Senac e senha de 12+ caracteres.\n- [ ] Conta pendente não consegue entrar; administrador verifica o vínculo institucional e ativa a conta.\n- [ ] E-mail externo é recusado no cadastro.\n- [ ] Perfil RESPONSAVEL não consegue aprovar/rejeitar reservas.
- [ ] Professor efetua login em janela anônima e solicita sala livre.
- [ ] Professor vê o estado SOLICITADA, mas não vê botões para aprovar/rejeitar.
- [ ] Admin acessa o painel de pendências e aprova a solicitação.
- [ ] Professor atualiza a tela e encontra o estado APROVADA.
- [ ] Professor não consegue cadastrar recursos nem alterar manutenção.
- [ ] Admin coloca sala em manutenção e nova solicitação é recusada.
- [ ] Botão Sair encerra sessão.

**Observação:** a visualização foi implementada, mas este checklist de navegador só pode ser marcado após execução e captura real.
