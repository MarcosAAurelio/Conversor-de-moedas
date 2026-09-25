# Qualidade e testes

As verificações são executadas pelo GitHub Actions em cada pull request e em pushes para `main`. Abra uma Issue para cada correção, melhoria ou funcionalidade antes de começar e mencione essa Issue no corpo do PR, conforme [AGENTS.md](../AGENTS.md).

## Requisitos locais

- JDK 21 para corresponder ao CI (a aplicação mantém alvo Java 17).
- Node.js 24.21.0, conforme `.nvmrc`.
- Chromium instalado pelo Playwright na primeira execução.

Comandos na raiz do projeto:

```powershell
npm ci
npm run build:pages
npm run lint
npm run knip
npm run test:unit:coverage
npm run test:mutation
npx playwright install chromium
npm run test:e2e
.\mvnw.cmd clean verify
```

Biome formata e verifica o JavaScript e o CSS do front-end, o adaptador Pages e o script de build. Use `npm run format` para aplicar a formatação e `npm run lint` para verificar as regras. Knip procura arquivos e dependências sem uso. Commitlint verifica os títulos dos commits incluídos em cada PR contra Conventional Commits. ArchUnit impede dependências entre camadas que quebrariam a arquitetura atual. JUnit e MockMvc cobrem regras e integrações Java; Jest cobre as funções puras de interface; Playwright abre o sistema local e valida fluxos reais no Chromium. Stryker executa testes de mutação sobre a validação monetária usada pela tela.

O CI também valida o Compose e as configurações do Nginx e Caddy, constrói a imagem de produção e o Dependabot monitora atualizações de dependências, ações e imagens de contêiner.

JaCoCo gera `target/site/jacoco/jacoco.xml` e Jest gera `coverage/lcov.info`. Para enviar os dois relatórios ao Codecov, configure um segredo de repositório chamado `CODECOV_TOKEN`. Sem o segredo, os testes continuam obrigatórios e apenas a etapa de upload é ignorada.

O servidor usado pelo Playwright vincula `127.0.0.1`, mantém o console H2 desligado e intercepta a chamada do gráfico para que os testes não dependam do serviço externo de cotações.
