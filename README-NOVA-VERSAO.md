# Guia de atualização segura — Organização de Recursos

Este arquivo substitui as instruções antigas do protótipo. Para requisitos, API e execução completa, consulte [README.md](README.md).

## Estado validado

A execução [CI 557](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43044676997) no commit `fe916322` concluiu com sucesso: **172 testes, zero falhas, zero erros**, incluindo testes com PostgreSQL via Testcontainers, verificação do Docker Compose e construção da imagem. SonarCloud foi ignorado por configuração; a instalação no computador do usuário e a migração de dados reais **não foram testadas**.

## Reservas conjuntas

A interface permite solicitar **uma sala e vários materiais** em uma única reserva. Os materiais são persistidos na tabela `reserva_material` (migração Flyway V7). Conflitos de horários são verificados para sala, professor vinculado e materiais. A aprovação de solicitações cabe ao perfil **ADMINISTRADOR**; um usuário sem essa permissão recebe HTTP 403.

## Antes de atualizar uma instalação existente

1. **Identifique qual PostgreSQL e qual volume estão em uso.** O `docker-compose.yml` cria o volume nomeado `postgres_data`, mas instalações anteriores podem usar outro banco ou contêiner. Não suponha que um volume novo contenha seus dados antigos.
2. **Faça e confira um backup restaurável do banco existente**, antes de iniciar a versão com migrações Flyway. Preserve também o arquivo `.env` atual, sem enviá-lo ao Git.
3. Confira se as credenciais e a URL de conexão apontam para o banco correto. Não suba um segundo PostgreSQL por engano, nem altere senhas de um volume já inicializado sem planejamento.
4. Só depois do backup, aplique a atualização em uma cópia de teste do banco. Verifique a migração V7, os registros antigos, a autenticação e uma reserva com sala e dois materiais antes de atualizar a instalação principal.

**Não execute `docker compose down -v`, `docker volume rm`, `docker system prune --volumes` nem comandos SQL de exclusão** para atualizar o sistema. Esses comandos podem destruir dados. `docker compose down` sem `-v` normalmente preserva os volumes, mas confirme a topologia da instalação antes de qualquer parada.

## Validação local pendente

O CI não comprova que a versão nova já está instalada ou que o banco real foi migrado. A validação local deve confirmar login de administrador e professor, permissões, criação de sala com múltiplos materiais, aprovação exclusivamente administrativa, conflitos, cancelamento e preservação do histórico. Não execute migrações em produção sem backup e teste de restauração.
