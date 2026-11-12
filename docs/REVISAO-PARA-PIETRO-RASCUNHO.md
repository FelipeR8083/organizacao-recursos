# Análise técnica para conferência de Pietro — segurança, APIs, testes e CI

**Projeto:** Gestão de Recursos Acadêmicos | **Elaborado em:** 12/11/2026  
**Preparação:** análise estática/documental assistida por IA, com base no código da `main`, patches e evidências do GitHub.  
**Destinatário sugerido para conferir e assumir, se concordar:** Pietro (`pietrolaureano`).  
**Situação:** **RASCUNHO TÉCNICO — NÃO É UMA REVISÃO OU APROVAÇÃO PUBLICADA POR PIETRO.**  
**Escopo:** requisitos e APIs desenvolvidos desde os PRs #1–#3; mudanças de CSRF, validação, pipelines, SonarCloud e JMeter nos PRs #4–#7. A data de autoria/revisão humana não será retroagida.  
**Autoria dos trechos:** não confirmada. Revisão por pares genuína requer análise independente de trabalho de outra pessoa.

## 1. Código e evidências examinados

- [`SegurancaConfig.java`](../src/main/java/com/senac/recursos/config/SegurancaConfig.java), [`ReservaController.java`](../src/main/java/com/senac/recursos/api/ReservaController.java), [`UsuarioService.java`](../src/main/java/com/senac/recursos/servico/UsuarioService.java) e [`CadastroProfessorController.java`](../src/main/java/com/senac/recursos/api/CadastroProfessorController.java).
- [`AutorizacaoApiTest.java`](../src/test/java/com/senac/recursos/api/AutorizacaoApiTest.java), [plano de testes](PLANO-DE-TESTES.md), [matriz de requisitos](../RTM.md), [BDD](EVIDENCIA-BDD.md).
- [PR #4 — CSRF](https://github.com/FelipeR8083/organizacao-recursos/pull/4), [PR #5 — regex/acessibilidade](https://github.com/FelipeR8083/organizacao-recursos/pull/5), [PR #6 — segurança do CI](https://github.com/FelipeR8083/organizacao-recursos/pull/6) e [PR #7 — carga pós-CSRF](https://github.com/FelipeR8083/organizacao-recursos/pull/7).

## 2. Constatações positivas verificadas

**P-01 — Proteção de APIs e sessão.** `SegurancaConfig` configura `BCryptPasswordEncoder`, permite autenticação por formulário/HTTP Basic, exige papéis específicos para gerenciamento e utiliza `.csrf(Customizer.withDefaults())`, sem desativação condicional. Os testes `AutorizacaoApiTest` incluem acesso sem autenticação, proibição de cadastro de recursos por solicitante, bloqueio de transição de estados por administrador/professor, autenticação de senha persistida e ativação administrativa do professor.

**P-02 — Proteção contra falsificação de solicitante.** Em `ReservaController.criar`, quando existe `Authentication`, o solicitante efetivo é obtido de `autenticacao.getName()`, e não do valor enviado livremente no corpo. Isso é uma salvaguarda contra associação de reservas a outro login; vale verificar como o contrato público explica o campo `solicitante`.

**P-03 — Cadastro de professores.** `UsuarioService.solicitarCadastroProfessor` restringe o domínio institucional, exige senha de pelo menos 12 caracteres, usa `encoder.encode` e cria conta **inativa**, para posterior aprovação. Quando a verificação de e-mail está habilitada, a ativação exige confirmação anterior. A ausência de SMTP real em homologação está registrada como ressalva e não deve ser omitida.

**P-04 — Controles automatizados e qualidade.** No PR #6, Actions passaram a SHA fixo, credenciais temporárias no runner e downloads do JMeter somente via HTTPS; no PR #7, o cálculo de P95 foi separado do máximo. [CI integrado](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844286) registra 188 testes, cobertura JaCoCo de **91,85% linhas e 78,37% branches**, conforme o [relatório de qualidade](RELATORIO-QUALIDADE.md). [JMeter integrado](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43316844059) registra **20 GET CSRF HTTP 200, 1 POST HTTP 201 e 19 POST HTTP 409**. SonarCloud Quality Gate passou, mas o histórico registra uma questão residual a triar. **Nenhum teste novo foi executado para este documento.**

## 3. Achados e melhorias para a avaliação independente

| ID | Severidade preliminar | Evidência ou limitação | Ação sugerida |
| --- | --- | --- | --- |
| **PI-REV-01** | **Média — inconsistência de contrato** | `ReservaController.NovaReserva` declara `@NotBlank String solicitante`, exigindo-o no JSON, mas `criar` ignora seu valor quando o usuário está autenticado e usa o login real. A substituição é boa para segurança; o campo obrigatório pode confundir consumidores da API. | Considerar retirar o campo do DTO para clientes autenticados, ou documentar explicitamente que é obrigatório mas não controla a titularidade. Ajustar exemplos/OpenAPI e testes antes de qualquer alteração. |
| **PI-REV-02** | **Média — decisão de exposição** | Em `SegurancaConfig`, `/swagger-ui/**` e `/v3/api-docs/**` estão públicos. É uma escolha de configuração observável, não uma vulnerabilidade automaticamente comprovada. | Confirmar se a documentação deve permanecer pública fora de localhost; definir política para produção e testar o acesso esperado. |
| **PI-REV-03** | **Alta — limite de homologação** | `UsuarioService` só exige confirmação de e-mail antes da ativação quando `verificacaoEmailObrigatoria` está ativa; a homologação descrita estava com SMTP desabilitado. Assim, teste local de aprovação não comprova posse de e-mail institucional real. | Manter ressalva e homologar fluxo com SMTP seguro em ambiente apropriado antes de declarar validação completa de e-mail institucional. |
| **PI-REV-04** | **Média — pendência de triagem** | O SonarCloud registrou observação histórica na migração Flyway V6 sobre `CHAR(64)`. A alternativa `VARCHAR2` sugerida é específica de Oracle, enquanto o projeto usa PostgreSQL. | Conferir regra/perfil SQL, justificar a classificação no SonarCloud sem modificar uma migração histórica aplicada nem alegar que o alerta foi automaticamente resolvido. |
| **PI-REV-05** | **Média — cobertura de evidências** | O plano de testes documenta E2E HTTP com TestRestTemplate/Testcontainers e CSS responsivo, mas não apresenta isso como teste visual real de navegador. | Validar telas em tamanhos reais e anexar evidências se o docente exigir; diferenciar ensaio visual de teste HTTP na apresentação. |

**Classificação:** PI-REV-01 e PI-REV-02 são propostas de melhoria/decisão de projeto; PI-REV-03 a PI-REV-05 são limitações e triagens abertas. Não atribuir a estes itens o status de bug crítico demonstrado.

## 4. Parecer de Pietro — somente após análise pessoal

Pietro deve acessar a [revisão aberta no PR #8](https://github.com/FelipeR8083/organizacao-recursos/pull/8), ler o código e os documentos, confirmar ou contestar os achados e registrar sua avaliação **com sua própria conta** e na **data em que realmente a fizer**.

- [ ] Li os arquivos de segurança e API, e conferi os testes e as evidências.
- [ ] Registrei minha avaliação sobre PI-REV-01 a PI-REV-05, indicando concordâncias e discordâncias.
- [ ] Verifiquei se avaliei código efetivamente produzido pelo outro integrante.
- [ ] Submeti meu parecer pelo GitHub com observações originais.

**Data real da análise:** pendente.  
**Trechos verificados pessoalmente:** pendente.  
**Achados aceitos/contestados:** pendente.  
**Decisão (Approve / Request changes / Comment):** pendente.  
**URL da revisão enviada pela conta `pietrolaureano`:** pendente.

**Transparência acadêmica:** estas observações são preparação retrospectiva assistida por IA. A revisão por pares exigida **durante o desenvolvimento** não pode ser dada como concluída no passado por este arquivo.
