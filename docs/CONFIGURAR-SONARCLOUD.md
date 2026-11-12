# SonarQube Cloud — estado real e integração com JaCoCo (12/11/2026)

O projeto [`FelipeR8083_organizacao-recursos`](https://sonarcloud.io/dashboard?id=FelipeR8083_organizacao-recursos&branch=main) já foi **importado e analisado automaticamente** pela integração do GitHub App SonarCloud.

## Evidência já obtida

- Commit analisado: merge do [PR #4](https://github.com/FelipeR8083/organizacao-recursos/pull/4), `60862cd8`, branch `main`.
- Check do GitHub: `SonarCloud Code Analysis` — **success**, `Quality Gate passed`.
- Resumo: **6 novas questões**, **0 Security Hotspots no código novo**, **0,0% de duplicação no código novo** e **0,0% de cobertura no código novo**.
- As métricas do *código novo* não substituem a triagem de todas as questões/hotspots do histórico.
- GitHub Actions: [run 43304807488](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43304807488), **185 testes, 0 falhas**, JaCoCo **91,73% linhas / 77,55% branches**, Docker build aprovado.

**Importante:** o SonarCloud está executando uma **análise automática separada do GitHub Actions**. A etapa `Analisar no SonarCloud` de `.github/workflows/ci.yml` permanece `skipped` porque os parâmetros do CI ainda não foram configurados. Isso **não significa que a análise SonarCloud não aconteceu**.

## Pendente: enviar JaCoCo ao SonarCloud

O arquivo `target/site/jacoco/jacoco.xml` já é produzido por `mvn verify` e o workflow contém a ação SonarSource para importá-lo. Para habilitar essa modalidade:

1. No SonarQube Cloud, verificar a modalidade atual de análise (`Automatic analysis`) e **mudar para análise por GitHub Actions**, conforme o assistente da plataforma, evitando dois mecanismos de análise simultâneos.
2. No GitHub, acessar `https://github.com/FelipeR8083/organizacao-recursos/settings/secrets/actions`. Criar o **segredo** de repositório `SONAR_TOKEN` com um token criado na conta SonarQube Cloud. **Nunca compartilhar o token no chat, captura, código ou commit.**
3. Em **Repository variables**, configurar `SONAR_ORGANIZATION=feliper8083` (confirmar a chave na organização Sonar) e `SONAR_PROJECT_KEY=FelipeR8083_organizacao-recursos` (confirmar no projeto).
4. Executar o CI de uma nova alteração e comprovar que a etapa `Analisar no SonarCloud` **não** é ignorada; conferir a cobertura importada na página do Sonar e a classificação das 6 questões.

Não declarar que a cobertura do SonarCloud é 91,73% enquanto a plataforma mostrar 0,0% para o código novo. **91,73% é a métrica JaCoCo do CI, com evidência separada e válida.**

## Segurança e ressalvas

O PR #4 ativou CSRF em todos os perfis e a equipe validou login/logout e `GET /csrf` na instância de homologação `:8085`, com 185 testes automatizados. A análise após merge passou no Quality Gate, porém **não há triagem documentada do impacto/severidade das 6 novas questões**.

O teste JMeter com 20 threads e 1 sucesso / 19 conflitos refere-se à implementação **anterior** à proteção CSRF obrigatória; o plano JMX atualizado ainda demanda nova execução caso essa evidência seja exigida para a versão final. A aplicação original em :8080 e o banco original em :5432 **não foram alterados**.

[Documentação oficial de integração com GitHub](https://docs.sonarsource.com/sonarqube-cloud/getting-started/github)


## Atualização de configuração — 12/11/2026, migração para análise pelo CI

- No GitHub, segredo de Actions `SONAR_TOKEN` criado e confirmado na interface. **O valor não deve ser exibido ou versionado.**
- Variáveis do repositório confirmadas: `SONAR_ORGANIZATION=feliper8083` e `SONAR_PROJECT_KEY=FelipeR8083_organizacao-recursos`.
- O token mostrado durante a configuração inicial foi revogado; foi emitido um novo token para uso exclusivo como segredo.
- A análise automática do projeto foi **desativada** pela equipe no painel `Administration → Analysis method` para evitar duplicação com o scanner do CI.
- O último resultado confirmado antes da troca foi **Quality Gate aprovado** e **0 novas questões**; a triagem de confiabilidade do PR #5 foi concluída com 5 correções e 1 questão de SQL documentada sem alterar Flyway histórico.
- **Próxima verificação:** CI de `main` deve executar a etapa `Analisar no SonarCloud` e importar `target/site/jacoco/jacoco.xml`. Não registrar cobertura no SonarCloud como concluída antes de observar o resultado da análise.
