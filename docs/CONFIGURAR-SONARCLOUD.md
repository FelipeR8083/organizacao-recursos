# Ativar SonarQube Cloud (SonarCloud) — passo a passo

Esta é a única integração de análise estática externa que ainda exige configuração da conta/equipe. **Não coloque tokens em commits, arquivos .env ou no chat.**

## 1. Criar e vincular projeto

1. Acesse https://sonarcloud.io e entre com sua conta GitHub.
2. Vincule sua conta/organização GitHub, concedendo acesso ao repositório `FelipeR8083/organizacao-recursos` quando solicitado.
3. Crie/importe o projeto do repositório. Copie do SonarQube Cloud os valores exatos de **organization key** e **project key**.
4. Configure análise por GitHub Actions se a tela apresentar modos alternativos.

Referência oficial: https://docs.sonarsource.com/sonarqube-cloud/getting-started/github

## 2. Configurar variáveis no GitHub

Abra https://github.com/FelipeR8083/organizacao-recursos/settings/secrets/actions

- Em **Repository secrets**, crie `SONAR_TOKEN` com um token do projeto/organização que possa executar análises.
- Em **Repository variables**, crie `SONAR_ORGANIZATION` com a organization key.
- Em **Repository variables**, crie `SONAR_PROJECT_KEY` com a project key.

Esses nomes já são usados em `.github/workflows/ci.yml`; a análise só executa quando os três estiverem configurados. O token é segredo e seu valor não deve aparecer em relatório, captura ou resposta do chat.

## 3. Executar e guardar evidência

1. Abra a pull request #1 ou faça um novo push do código.
2. Confira no GitHub Actions que a etapa **Analisar no SonarCloud** executou com sucesso (não pode aparecer como skipped).
3. Abra o projeto no SonarQube Cloud, confira Quality Gate, bugs e vulnerabilidades críticas e anexe captura ou link do relatório à entrega.
4. Se houver alerta crítico, corrija, reexecute e atualize `docs/REGISTRO-DEFEITOS.md` e `docs/RELATORIO-QUALIDADE.md`.

**Status atual:** workflow e parâmetros preparados, mas análise real ainda não comprovada. Não afirmar zero vulnerabilidades sem relatório do serviço.
