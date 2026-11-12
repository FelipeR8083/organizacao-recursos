# SonarQube Cloud e JaCoCo — integração concluída em 12/11/2026

## Estado atual comprovado

- Projeto SonarQube Cloud: [`FelipeR8083_organizacao-recursos`](https://sonarcloud.io/dashboard?id=FelipeR8083_organizacao-recursos&branch=main).
- **Modalidade:** análise por **GitHub Actions**, não automática. A análise automática foi desligada em `Administration → Analysis method` para evitar duplicações.
- GitHub Actions: segredo do repositório `SONAR_TOKEN` cadastrado **sem expor o valor**; variáveis `SONAR_ORGANIZATION=feliper8083` e `SONAR_PROJECT_KEY=FelipeR8083_organizacao-recursos`.
- O token de onboarding inicialmente exposto foi revogado; o novo é privado no GitHub. Por ser token pessoal com vencimento, deverá ser renovado quando expirar, caso a integração continue em uso.
- O workflow `.github/workflows/ci.yml` executa `mvn -B verify`, gera `target/site/jacoco/jacoco.xml` e executa `SonarSource/sonarqube-scan-action` **fixado em SHA completo** após os testes.
- **Evidência principal:** [CI `main` 43313349056](https://github.com/FelipeR8083/organizacao-recursos/actions/runs/43313349056), merge do [PR #6](https://github.com/FelipeR8083/organizacao-recursos/pull/6), SHA `746cc14e77c112973220c8262269ffdaaa34254e`: **188 testes, 0 falhas**, **JaCoCo 91,85% das linhas e 78,37% dos branches**, build Docker aprovado.
- O scanner publicou a análise no SonarCloud; seus logs incluem `Sensor JaCoCo XML Report Importer` e **`Importing 1 report(s)`**, confirmando importação de XML.
- Check do GitHub `SonarCloud Code Analysis`: **success — Quality Gate passed** na `main`, **1 nova questão** e **0 Security Hotspots no código novo**. Não significa ausência absoluta de problemas no código histórico.

## Linha do tempo dos alertas

1. Análise automática inicial: pontos de atenção em CSRF. O [PR #4](https://github.com/FelipeR8083/organizacao-recursos/pull/4) ativou CSRF em todos os perfis; login/logout foram verificados na homologação `:8085`.
2. Análise estática seguinte: 6 questões de confiabilidade/acessibilidade; o [PR #5](https://github.com/FelipeR8083/organizacao-recursos/pull/5) corrigiu 5. Manteve `CHAR(64)` no Flyway PostgreSQL, em vez de seguir indevidamente a recomendação de `VARCHAR2` (Oracle).
3. Ao mudar para análise por CI, foram identificados **4 alertas de segurança nos workflows** (1 alto, 3 médios). O [PR #6](https://github.com/FelipeR8083/organizacao-recursos/pull/6) resolveu: ação pinada por commit, remoção de senhas fixas no JMeter, redirecionamentos de download restritos a HTTPS. O Quality Gate da `main` voltou a **passar**.

## Distinção importante: cobertura global vs código novo

A suíte Maven/JaCoCo registrou **91,85% de linhas / 78,37% de branches**, resultado independente e reproduzível pelos artefatos do CI. O scanner Sonar importou **um XML JaCoCo** nessa versão. O check do Sonar apresenta **0,0% de cobertura em código novo** quando o período recente altera apenas YAML/documentação e não contém linhas Java novas cobráveis; **esse indicador não representa a cobertura global**. Registrar o valor de cobertura global do próprio painel Sonar apenas após observação direta do número.

## Riscos e cuidados

- Continua registrada 1 questão nova no Sonar; conferir seu conteúdo antes de declarar a inexistência de problemas críticos no projeto inteiro. A classificação `CHAR(64)`/Flyway precisa de revisão formal e justificativa se for aceita.
- O teste JMeter com 20 threads e **1 HTTP 201 / 19 HTTP 409** é uma evidência **histórica**, anterior ao CSRF obrigatório. O plano JMX e workflow foram adaptados para obter tokens por sessão e credenciais efêmeras, mas **não houve reexecução comprovada após essa mudança**.
- A aplicação local original `:8080` e seu PostgreSQL `:5432` permanecem intocados. A homologação isolada `:8085` recebeu a versão de CSRF do PR #4, mas não se afirma que a imagem local tenha sido reconstruída após PRs #5 e #6.
- A aprovação formal de revisão pelo colega e a demonstração final ainda precisam ser evidenciadas.

[Referência SonarSource: integração com GitHub](https://docs.sonarsource.com/sonarqube-cloud/getting-started/github)
