# Gestão de Recursos Acadêmicos — versão expandida

## O que foi acrescentado
- Interface web responsiva em `src/main/resources/static/index.html` (abre em http://localhost:8080/).
- Cadastro, listagem e edição de manutenção de recursos via API.
- Listagem de reservas e transições de estado permitidas pela máquina de estados existente.
- Histórico persistente de criação e transições de reservas, consultável por `GET /reservas/{id}/auditoria`.
- Tratamento de requisições inválidas e mensagens de erro na interface.
- Mantidos os testes e a lógica original de prevenção de conflitos com lock pessimista.

## Executar no Windows
1. Inicie seu PostgreSQL já existente: `docker start recursos-db`.
2. Abra um terminal na pasta que contém `pom.xml`.
3. Execute `& "C:\Maven\bin\mvn.cmd" spring-boot:run`.
4. Abra `http://localhost:8080/` no navegador.

## APIs
- `GET /recursos`, `POST /recursos`, `PUT /recursos/{id}`.
- `GET /reservas`, `POST /reservas`, `PATCH /reservas/{id}/estado`.
- `GET /reservas/{id}/auditoria` — histórico cronológico de criação e mudanças de estado.

## IMPORTANTE — NÃO É ENTREGA FINAL COMPLETA
Esta versão é um protótipo funcional acadêmico, **sem autenticação nem autorização**. Não exponha a aplicação à internet nem use dados reais. Ainda faltam: autenticação e perfis, regras de autorização (inclusive aprovação de recursos restritos), retirada/devolução de materiais, edição de horários com verificação de conflitos, testes de ponta a ponta, testes de carga, documentação de rastreabilidade, integração SonarCloud e validação integral dos 14 requisitos. O histórico de reservas já possui persistência básica, mas ainda não identifica o usuário responsável porque não há autenticação. Não há evidência de cobertura mínima ou de aprovação de todos os testes neste pacote.

O código foi estendido preservando a estrutura e os testes existentes. A compilação/execução precisa ser validada na sua máquina com Maven e PostgreSQL.
