# Configuracao do e-mail institucional

O cadastro publico aceita apenas dominios institucionais Senac. Com o envio SMTP
habilitado, o cadastro cria um token aleatorio de 256 bits, grava apenas seu hash
SHA-256 e envia um link valido por 24 horas. O professor permanece inativo ate
confirmar o e-mail e ser aprovado por um **ADMINISTRADOR**. O perfil
**RESPONSAVEL** pode aprovar reservas, mas nao pode ativar professores.

## Variaveis de ambiente

| Variavel | Funcao |
| --- | --- |
| `EMAIL_ENVIO_HABILITADO` | `true` para exigir confirmacao e enviar o link |
| `APP_PUBLIC_URL` | URL publica da aplicacao, sem barra final (HTTPS em producao) |
| `EMAIL_REMETENTE` | Endereco From autorizado no servidor SMTP |
| `SMTP_HOST` | Host do servidor SMTP |
| `SMTP_PORT` | Porta SMTP, conforme o provedor |
| `SMTP_USER` | Usuario SMTP, quando exigido |
| `SMTP_PASSWORD` | Senha SMTP, quando exigida |
| `SMTP_AUTH` | `true` se o servidor exigir autenticacao |
| `SMTP_STARTTLS` | `true` se o servidor exigir STARTTLS |
| `SMTP_CONNECTION_TIMEOUT_MS` | Limite de conexao (padrao: 5000 ms) |
| `SMTP_READ_TIMEOUT_MS` | Limite de leitura (padrao: 5000 ms) |
| `SMTP_WRITE_TIMEOUT_MS` | Limite de escrita (padrao: 5000 ms) |
| `SPRING_PROFILES_ACTIVE` | `prod` habilita validacoes de configuracao |

Nao salve credenciais SMTP no Git. Configure-as como segredos do ambiente de
execucao. Para testes locais, utilize um servidor SMTP de desenvolvimento,
como Mailpit, e nao envie mensagens reais sem autorizacao.

## Fluxo esperado

1. Professor envia `POST /cadastro-professor` com e-mail institucional e senha.
2. Com envio habilitado, o servidor cria a conta inativa, gera o token e envia
   um link no formato `https://seu-dominio/confirmar-email.html#token=...`.
3. A pagina publica envia o token em `POST /confirmacao-email-professor`.
   O token e de uso unico e expira em 24 horas.
4. Somente depois da confirmacao um administrador pode ativar a conta.
5. A conta so pode autenticar apos a ativacao administrativa.

Se o envio SMTP falhar durante o cadastro, a requisicao falha e a transacao
de cadastro e revertida. **Nao habilite envio sem um servidor SMTP funcional.**

## Producao

Com `SPRING_PROFILES_ACTIVE=prod`, a aplicacao recusa a inicializacao se
`EMAIL_ENVIO_HABILITADO` nao for `true`, se `APP_PUBLIC_URL` nao usar HTTPS
ou se `SMTP_HOST` estiver vazio ou definido como localhost.

A validacao de configuracao nao substitui testes de entrega real de e-mail,
TLS, DNS, SPF/DKIM/DMARC e controle de credenciais. Tambem permanece necessaria
a revisao da protecao CSRF antes de expor a aplicacao publicamente.
