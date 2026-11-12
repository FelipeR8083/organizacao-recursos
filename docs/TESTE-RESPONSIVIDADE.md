# Checklist de responsividade e acessibilidade (RF-13)

Este documento define **o teste a executar**. A existência deste checklist não comprova que o teste manual foi realizado.

## Ambientes

| Largura de viewport | Contexto | Resultado | Evidência |
|---|---|---|---|
| 360 × 800 px | Celular compacto | Pendente | Captura de tela |
| 390 × 844 px | Celular típico | Pendente | Captura de tela |
| 768 × 1024 px | Tablet | Pendente | Captura de tela |
| 1366 × 768 px | Desktop | Pendente | Captura de tela |

## Roteiro de aceitação

- [ ] Título, estatísticas e botões de navegação permanecem visíveis sem corte.
- [ ] Formulários de cadastro, pesquisa e reserva ficam em uma coluna no celular.
- [ ] É possível operar tudo por teclado (Tab, Shift+Tab, Enter e Espaço); o foco fica destacado.
- [ ] As tabelas grandes possuem rolagem horizontal própria; a página não força rolagem lateral.
- [ ] Botões em telas pequenas possuem área de toque confortável.
- [ ] Avisos de sucesso/erro são legíveis, em português, sem ultrapassar a tela.
- [ ] O fluxo completo de cadastro de recurso, pesquisa, reserva e cancelamento funciona em celular.
- [ ] Recursos de acessibilidade para redução de movimento são respeitados.
- [ ] Contraste, ampliação em 200% e leitor de tela passam por revisão manual.
- [ ] Perfil SOLICITANTE não é incentivado a usar botões administrativos que retornarão 403.

## Evidências a anexar

Registre data, navegador e sistema operacional de cada execução; anexe imagens no relatório final de qualidade, identifique não conformidades e registre as correções em `docs/REGISTRO-DEFEITOS.md`.

## O que já está no código

O CSS da interface contempla breakpoints móveis, grades de uma coluna, área de toque mínima, estados `focus-visible`, rolagem horizontal isolada nas tabelas e `prefers-reduced-motion`. Essas são **medidas implementadas**, não uma certificação de responsividade ou acessibilidade.
