# Plano de teste de carga — JMeter

## Objetivo

Verificar o comportamento da proteção contra dupla reserva quando múltiplas solicitações tentam alocar o mesmo recurso no mesmo intervalo. O cenário exercita a API real, autenticação HTTP Basic, PostgreSQL e bloqueio pessimista.

## Preparação

1. Suba a aplicação e o PostgreSQL com Docker Compose.
2. Crie um recurso de teste exclusivo e anote seu ID.
3. Use um usuário de perfil SOLICITANTE.
4. Escolha um intervalo futuro livre e não reutilize o intervalo em execuções anteriores.
5. Instale Apache JMeter 5.6.3 ou superior.

## Execução

Na raiz do repositório, execute em terminal (substitua usuário, senha e ID):

    jmeter -n -t jmeter/reservas-concorrentes.jmx -Jhost=localhost -Jport=8085 -JbaseUrl=http://localhost:8085 -Jlogin=usuario_teste -Jpassword='senha-de-teste' -JresourceId=1 -Jthreads=20 -Jstart=2026-12-01T10:00:00Z -Jend=2026-12-01T11:00:00Z -l resultados-reservas.jtl -e -o relatorio-jmeter

Não use credenciais reais em arquivos versionados ou capturas de tela. O JMX recebe as credenciais por propriedades de execução.

**Atualização CSRF (12/11/2026):** antes de cada `POST /reservas`, cada thread executa `GET /csrf`, extrai `$.token` e mantém seu próprio cookie de sessão (`JSESSIONID`). O `POST` envia `X-CSRF-TOKEN`. Isso impede que o teste de carga dependa de CSRF desativado. O JTL contém amostras de **preparação GET** e de **reserva POST**; conte apenas as amostras com rótulo `Solicitar reserva concorrente` para comparar a distribuição **1 HTTP 201 + 19 HTTP 409**. Use o banco isolado de homologação, sem alterar o sistema original da porta 8080.

A alteração do JMX foi **validada em execução JMeter real no GitHub Actions após o CSRF obrigatório**, com resultados e artefatos abaixo. Não foi necessário testar a instalação local; as evidências novas vieram de runner isolado do GitHub.

## Resultado esperado

Em banco e intervalo limpos, para 20 requisições simultâneas com o mesmo recurso e período:
- exatamente uma resposta HTTP 201 (reserva criada);
- as outras 19 respostas HTTP 409 (conflito de horário);
- nenhuma duplicidade persistida no PostgreSQL.

O resultado real deve ser conferido em resultados-reservas.jtl, no relatório HTML e no banco. Este repositório contém o plano, não um resultado de execução; não declarar meta de desempenho atingida sem executar o teste e anexar as evidências.

## Métricas a registrar

- número de threads e duração;
- total de amostras e distribuição por status HTTP;
- throughput (requisições/s);
- média, mediana, percentis 90/95/99 e máximo de latência;
- erros, timeouts e conexões recusadas;
- quantidade final de reservas no intervalo;
- CPU/memória da aplicação e PostgreSQL, se monitoradas.

## Critério de aprovação funcional

A proteção contra dupla reserva passa se houver exatamente uma reserva aceita e nenhuma duplicidade. Metas de latência/throughput devem ser definidas pelo docente ou equipe antes de avaliar performance; não foram inventadas neste plano.

## Resultado comprovado pós-CSRF na `main` — GitHub Actions 43316844059 (12/11/2026)

**Evidência final:** [run JMeter aprovado](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844059), commit `7b14b17f` do [PR #7](https://github.com/FelipeR8083/organizacao-recursos/pull/7). O workflow iniciou aplicação e PostgreSQL em um **runner efêmero do GitHub**, sem usar nem alterar os contêineres locais.

- **20 threads** disputando a mesma sala e horário em banco de teste novo.
- **20 GET `/csrf` com HTTP 200**. Cada thread preservou seu cookie de sessão e enviou o token `X-CSRF-TOKEN` no POST.
- **20 POST `/reservas`**: exatamente **1 HTTP 201** (reserva aceita) e **19 HTTP 409** (conflitos esperados). Critério de concorrência aprovado.
- Latência dos **POST**, excluindo GET de preparação: **média 38,5 ms, P95 117 ms e máximo 122 ms**. P95 calculado pelo posto mais próximo (`ceil(0,95 × N) - 1` no índice zero).
- O artefato `evidencias-jmeter` contém `resultados-reservas.jtl`, `resultado-jmeter.md` e `relatorio-jmeter/` HTML. O JTL foi lido diretamente: **40 amostras (20 GET + 20 POST)**, com as distribuições acima.
- Os 19 códigos HTTP 409 são **conflitos esperados**, ainda que o JMeter os marque como amostras com erro HTTP. Não houve status inesperados nessa execução.
- Não foi feita uma consulta SQL independente de contagem final; o critério de aprovação automático verificou as respostas HTTP. Essas latências são observações desta execução, não garantias de desempenho de produção.

**Repetição na branch de verificação:** [JMeter 43315977934](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43315977934) também aprovado: 20 tokens HTTP 200, 1 reserva 201 e 19 conflitos 409; média 47,8 ms, P95 142 ms e máximo 177 ms. Uma execução anterior [43315341737](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43315341737) aprovou a concorrência; foi identificado e corrigido no PR #7 um cálculo que rotulava o máximo como P95 (P95 correto do JTL anterior: 147 ms).

## Resultado histórico (anterior ao CSRF obrigatório) — GitHub Actions 42711044633

O teste executou com aplicação e PostgreSQL iniciados por Docker Compose no runner do GitHub, Apache JMeter 5.6.3 e **20 threads** no mesmo recurso e intervalo. [Execução aprovada e artefatos](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/42711044633).

- Total: **20 requisições**.
- HTTP 201 (reserva aceita): **1**.
- HTTP 409 (conflito): **19**.
- Erros de transporte/timeout: **0** na execução aprovada.
- Latência média: **106,9 ms**.
- P95 de latência: **209 ms**.
- Evidências: `resultados-reservas.jtl`, `resultado-jmeter.md`, `relatorio-jmeter/` no artefato `evidencias-jmeter`.

### Registro de defeitos do cenário

A primeira execução falhou ao carregar o arquivo JMX por usar classe `AuthorizationManager` inválida; corrigiu-se para `AuthManager` (commit `1c8e4c86`). Na execução seguinte, 11 solicitações atingiram timeout de 15 segundos, embora não houvesse dupla reserva; aumentou-se o prazo de espera para 90 segundos (commit `f6af1ded`). A execução final cumpriu todos os critérios. O limite de 90 segundos é proteção do cenário, não uma meta de desempenho da aplicação.
