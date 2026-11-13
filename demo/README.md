# Banco de demonstracao V9 — dados ficticios

Esta pasta permite **reproduzir um banco PostgreSQL atualizado ate Flyway V9 com dados ficticios**, sem enviar dados pessoais ou senhas. O esquema oficial esta versionado em [migracoes Flyway](../src/main/resources/db/migration); este **nao e um backup do computador de nenhum integrante**.

O script [dados-ficticios-v9.sql](dados-ficticios-v9.sql) insere, em uma base isolada chamada obrigatoriamente **recursos_demo_v9**:
- 6 recursos ficticios (2 salas, 1 professor, 3 materiais, incluindo 1 em manutencao);
- 2 reservas ilustrativas (1 concluida e 1 solicitada para o dia seguinte), vinculadas a materiais;
- historico identificado explicitamente como importacao de demonstracao;
- 1 notificacao simulada, sem envio de e-mail.

Esses registros **nao comprovam** a execucao real dos fluxos de autorizacao, aprovacao ou movimentacao. O SQL nao cria usuarios nem guarda senhas.

## Preparar (PowerShell, na raiz do repositorio)

Requer Docker Desktop e arquivo **.env** local com DB_PASSWORD, APP_ADMIN_USERNAME e APP_ADMIN_PASSWORD definidos. Se for a primeira execucao, inicie o Compose conforme o [README principal](../README.md), confirmando que o PostgreSQL **recursos-db** e a rede **organizacao-recursos_default** existem.

**Nao execute estes comandos no banco original recursos.**

1. Crie uma base separada (nao repita se ela ja existir):

~~~powershell
docker exec recursos-db createdb -U recursos recursos_demo_v9
~~~

2. Construa a aplicacao desta revisao do Git e inicie outra instancia **na porta 8087**, conectada so a base de demonstracao:

~~~powershell
docker build -t organizacao-recursos-demo:v9 .
docker run -d --name recursos-demo-v9 --network organizacao-recursos_default -p 127.0.0.1:8087:8080 --env-file .env -e DB_URL=jdbc:postgresql://recursos-db:5432/recursos_demo_v9 -e DB_USER=recursos organizacao-recursos-demo:v9
~~~

3. Confira se o Flyway chegou a V9 e a aplicacao iniciou:

~~~powershell
docker logs --tail 60 recursos-demo-v9
docker exec recursos-db psql -U recursos -d recursos_demo_v9 -c "SELECT version, success FROM flyway_schema_history ORDER BY installed_rank;"
~~~

Nao continue se a lista nao chegar a V9 sem erros.

4. Copie e execute o SQL **somente** na base de demonstracao. O arquivo impede a execucao em outro banco ou sem migracao V9:

~~~powershell
docker cp .\demo\dados-ficticios-v9.sql recursos-db:/tmp/dados-ficticios-v9.sql
docker exec recursos-db psql -U recursos -d recursos_demo_v9 -v ON_ERROR_STOP=1 -f /tmp/dados-ficticios-v9.sql
~~~

5. Confira:

~~~powershell
docker exec recursos-db psql -U recursos -d recursos_demo_v9 -c "SELECT tipo, nome FROM recurso ORDER BY tipo, nome;"
docker exec recursos-db psql -U recursos -d recursos_demo_v9 -c "SELECT id, estado, solicitante, inicio FROM reserva ORDER BY id;"
~~~

Abra **http://localhost:8087/login.html**, usando o administrador local do .env (nao divulgue a senha). A porta **8080** e o banco original **recursos** permanecem separados.

Para demonstrar o relatorio de utilizacao, selecione um periodo desde tres dias antes ate tres dias depois da execucao do seed. O periodo inclui reservas concluidas e solicitadas.

## Integridade, limites e seguranca

- As **migracoes V1–V9** criam o esquema na primeira execucao do aplicativo. O seed **nao substitui** nem altera o Flyway.
- O seed e opcional; **nunca execute automaticamente sobre dados existentes**.
- Pode ser repetido no mesmo dia sem duplicar as reservas da carga inicial. As datas relativas mudam conforme o dia e podem gerar novas reservas numa execucao futura.
- Nao ha validacao funcional automatica deste seed: teste os fluxos pelo sistema e pelos testes do projeto.
- **Nao publique** o .env, backup real, credenciais, tokens de email ou informacoes pessoais no GitHub. Para entregar banco de demonstracao, os scripts versionados sao a forma reprodutivel, sem senhas.
