# Correção: recursos com nome duplicado

Agora o backend recusa (HTTP 409) cadastro e alteração com o mesmo **tipo e nome** (sem diferenciar maiúsculas/minúsculas e espaços nas extremidades). O frontend mostra a mensagem e exibe IDs no seletor e nas reservas.

## IMPORTANTE: banco já existente

O banco já contém duas `Sala 101`. **Antes de executar a nova versão**, faça backup do PostgreSQL e rode `CORRIGIR-DUPLICADOS.sql` **uma vez**, com a aplicação parada. O script mantém o menor ID com o nome original e renomeia os demais (ex.: `Sala 101 - ID 2`), sem apagar recursos nem reservas. Depois cria um índice único no PostgreSQL para impedir duplicidade até em requisições simultâneas.

No PowerShell, com Docker Desktop e o container `recursos-db` em execução:

```powershell
# backup (arquivo local):
docker exec recursos-db pg_dump -U recursos -d recursos -f /tmp/recursos-antes-duplicados.sql
docker cp recursos-db:/tmp/recursos-antes-duplicados.sql .\recursos-antes-duplicados.sql
# execute o SQL de correção (PowerShell):
Get-Content -Raw -Encoding UTF8 .\CORRIGIR-DUPLICADOS.sql | docker exec -i recursos-db psql -v ON_ERROR_STOP=1 -U recursos -d recursos
```

Confira os nomes no navegador após iniciar a aplicação. Para rodar testes: `& "C:\Maven\bin\mvn.cmd" clean verify`.

**Atenção:** Se o banco/container/usuário tiver nomes diferentes, adapte os comandos. Não execute o SQL sem confirmar o backup. O índice de unicidade é instalado pelo script SQL, não automaticamente pelo Hibernate.
