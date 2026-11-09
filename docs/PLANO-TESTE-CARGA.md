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

    jmeter -n -t jmeter/reservas-concorrentes.jmx -Jhost=localhost -Jport=8080 -JbaseUrl=http://localhost:8080 -Jlogin=usuario_teste -Jpassword='senha-de-teste' -JresourceId=1 -Jthreads=20 -Jstart=2026-12-01T10:00:00Z -Jend=2026-12-01T11:00:00Z -l resultados-reservas.jtl -e -o relatorio-jmeter

Não use credenciais reais em arquivos versionados ou capturas de tela. O JMX recebe as credenciais por propriedades de execução.

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
