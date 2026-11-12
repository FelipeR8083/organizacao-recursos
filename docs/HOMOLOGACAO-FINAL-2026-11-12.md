# Homologação final e pendências — 12/11/2026

> Documento de acompanhamento, não um certificado de entrega concluída. Referência: enunciado de Qualidade 2026.2 (https://afonsolelis.github.io/aulas_senac/pages/qualidade2/especificacao-projeto.html).

## Evidências automatizadas já verificadas

- GitHub Actions 43217218310: **179 testes, zero falhas/erros, JaCoCo 91,70% de linhas e 77,44% de branches, build Docker aprovado**.
- JMeter 42711044633: 20 solicitações simultâneas, uma HTTP 201 e dezenove HTTP 409; anexar resultados JTL/HTML à entrega.
- TDD/BDD: `docs/EVIDENCIA-BDD.md` mostra falha/correção/regressão no RF-03.
- Em 11/11, homologação na porta 8084 confirmou relatório Sala 101 (4 reservas/240 minutos), Projetor adicional (1 reserva/60 minutos) e 2 conflitos evitados.

## Novos testes manuais obrigatórios, ainda pendentes

**Ambiente:** usar banco isolado `recursos_teste_v9` e porta de teste 8085. Antes de executar, criar backup da base `recursos_teste_v8`. NÃO utilizar a base original `recursos` nem interromper `organizacao-recursos` na porta 8080.

- [ ] Iniciar a nova versão com Flyway V9. Confirmar logs: migração V9 aplicada, Hibernate iniciado, sem erros, e seis reservas preservadas na cópia.
- [ ] ADMINISTRADOR gerencia usuários, recursos e manutenção, mas não aprova/rejeita nenhuma reserva, nem registra retirada/devolução.
- [ ] RESPONSAVEL aprova/rejeita pedido comum e restrito, registra início/fim/ausência e movimentações; pode criar sua própria reserva, mas só outro RESPONSAVEL pode avaliá-la.
- [ ] PROFESSOR recebe 403 ao tentar aprovar ou acessar movimentações; só vê as próprias reservas.
- [ ] Sala com **dois materiais**: retirar cada um, tentar duplicar retirada, devolver o primeiro (reserva permanece EM_USO), devolver o segundo (reserva CONCLUIDA); conferir auditoria.
- [ ] Tela **Usuários**: ADMINISTRADOR lista/cadastra/ativa/desativa; professor e responsável recebem 403; confirmação de e-mail institucional conforme configuração de teste.
- [ ] Estado `Em manutenção` visível para professor como `🔧 Indisponível — em manutenção`; recurso não selecionável; liberar depois.
- [ ] Reagendar/cancelar reserva própria e impedir ação sobre reserva alheia.
- [ ] Pesquisa por tipo, capacidade, localização, competência e disponibilidade; validar horários adjacentes.
- [ ] Demonstrar Swagger UI em `/swagger-ui/index.html` e contrato em `/v3/api-docs`.
- [ ] Checklist visual em 360px, 390px, 768px e desktop, foco no teclado, ampliação 200%, rolagem das tabelas; guardar capturas e registrar problemas.
- [ ] Configurar **SonarQube Cloud / SonarCloud**: criar o projeto, adicionar SONAR_TOKEN como segredo e SONAR_ORGANIZATION/SONAR_PROJECT_KEY como variáveis do GitHub, rodar CI sem etapa skipped e anexar dashboard/Quality Gate. Nunca incluir tokens no repositório ou no chat.
- [ ] Realizar revisão por pares no pull request com o Pietro (ou outro revisor conforme orientação docente), registrar comentários e correções, preservar evidência.
- [ ] Revisar documentação técnica com evidências reais, montar apresentação/slides, arquivar JaCoCo, SonarCloud e JMeter.
- [ ] Integrar branches somente depois da homologação e repetir CI no SHA final. As branches de coautoria ainda são cópias de teste; refazer a inclusão da coautoria depois das correções, sem substituir a main por push forçado sem plano.

## Evidência de responsividade — 12/11/2026

**Ambiente:** `http://localhost:8085`, Edge DevTools em emulação responsiva. Evidências PNG capturadas pela equipe na sessão de homologação (guardar/anexar os arquivos originais à entrega, pois as imagens não foram incorporadas ao repositório).

| Largura | Tela | Verificação observada |
|---|---|---|
| **360 px** | Login | Cabeçalho, rótulos, campos, botão e link de cadastro institucional visíveis sem sobreposição aparente. |
| **360 px** | Recursos | Indicadores e filtros empilhados; tabela possui rolagem horizontal e as colunas de manutenção/ação foram alcançadas manualmente. |
| **360 px** | Reservas | Formulário em uma coluna e listagem com três reservas; demais colunas consultáveis por deslocamento horizontal relatado pela equipe. |
| **768 px** | Reservas | Indicadores em três colunas, navegação horizontal, formulário em duas colunas, lista legível; coluna de ações parcialmente fora da captura, com verificação de rolagem específica ainda pendente. |

**Resultado:** inspeção visual básica favorável para celular (360 px) e tablet (768 px), sem falhas de layout óbvias nas capturas fornecidas. Não equivale a certificação de acessibilidade nem cobre os testes de teclado, zoom 200%, largura 390 px ou validação física em dispositivos reais. Esses itens permanecem pendentes, assim como captura final em anexo formal.

## Critérios de conclusão

A entrega só poderá ser declarada integralmente aprovada depois de: todos os requisitos críticos testados em API e interface, pipeline verde no código destinado à entrega, cobertura acima das duas metas, SonarCloud executado e sem bugs/vulnerabilidades críticas não resolvidos, artefatos JMeter, revisão por pares, responsividade validada e demonstração preparada.

O projeto original na porta 8080 permanece preservado durante a homologação.
