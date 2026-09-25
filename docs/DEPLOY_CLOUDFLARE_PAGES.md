# Publicar a versão estática no Cloudflare Pages

Este guia publica a versão estática do conversor usando o GitHub. O Cloudflare Pages entrega os arquivos HTML, CSS e JavaScript; não executa a aplicação Spring Boot.

## O que será publicado

- A pasta `cloudflare-pages/site` é um site estático completo e pronto para publicação.
- Ela inclui cópias do CSS, das imagens e de scripts usados também pela aplicação Java. Essas cópias mantêm a página independente de um servidor Spring Boot.
- O Pages não recebe o código Java, os arquivos de configuração do banco ou as imagens de build Docker como conteúdo do site.
- A versão estática consulta a API Frankfurter diretamente no navegador e guarda até 100 conversões no `localStorage` do navegador.
- O histórico local não sincroniza com outros navegadores ou dispositivos e pode ser apagado ao limpar os dados do site. Não use essa versão para guardar informações privadas.
- A API REST, o Swagger e o banco PostgreSQL pertencem à aplicação Java original e não são executados no Pages.

O endpoint público do Frankfurter não exige chave de API; suas respostas e disponibilidade dependem do serviço externo. A requisição envia o par de moedas e o período do gráfico; o valor convertido e o histórico não são enviados à API. Consulte a [documentação do Frankfurter](https://frankfurter.dev/).

## Configurar o projeto no Cloudflare

1. No painel Cloudflare, abra **Workers & Pages** e crie uma aplicação Pages conectada ao repositório `MarcosAAurelio/Conversor-de-moedas`.
2. Use `main` como branch de produção.
3. Selecione **None** como framework preset e deixe o diretório raiz no padrão do repositório.
4. Configure:

   | Campo | Valor |
   | --- | --- |
   | Build command | deixe em branco |
   | Build output directory | `cloudflare-pages/site` |

5. Salve e faça o primeiro deploy. O site ficará disponível no subdomínio `*.pages.dev`; não é necessário comprar domínio próprio para usar esse endereço.

Depois que a mudança for integrada a `main`, o Git integration do Pages gera os próximos deploys a partir dessa branch. Pull requests também podem gerar URLs de preview, conforme as configurações do projeto Pages.

## Custo e limites

Esta configuração usa apenas arquivos estáticos e não cria Pages Functions, Worker, banco ou servidor Java. A Cloudflare informa que requisições a arquivos estáticos do Pages são gratuitas e ilimitadas; se Pages Functions forem adicionadas no futuro, as chamadas passam a contar para a cota de Workers. Veja [preços de Pages Functions](https://developers.cloudflare.com/pages/functions/pricing/) e [limites do Pages](https://developers.cloudflare.com/pages/platform/limits/).

O uso do `pages.dev` evita custo com domínio próprio. A API de cotações é um serviço separado, fora do controle deste repositório; esta adaptação não inclui chave, cobrança ou garantia de disponibilidade para ela.

## Atualizar os arquivos estáticos compartilhados

Quando alterar o CSS ou um script compartilhado na aplicação Java, copie a versão atualizada para a pasta Pages correspondente antes de publicar:

```powershell
Copy-Item src/main/resources/static/css/styles.css cloudflare-pages/site/css/styles.css -Force
Copy-Item src/main/resources/static/js/app.js cloudflare-pages/site/js/app.js -Force
Copy-Item src/main/resources/static/js/currency-input.js cloudflare-pages/site/js/currency-input.js -Force
Copy-Item src/main/resources/static/js/theme-init.js cloudflare-pages/site/js/theme-init.js -Force
Copy-Item src/main/resources/static/js/about-tilt.js cloudflare-pages/site/js/about-tilt.js -Force
Copy-Item src/main/resources/static/images/marcos-aurelio.jpeg cloudflare-pages/site/images/marcos-aurelio.jpeg -Force
```

Não substitua `cloudflare-pages/site/js/pages-adapter.js`: ele conecta a versão estática ao serviço de cotações e ao histórico local. Os arquivos em `cloudflare-pages/site` são os próprios arquivos de saída e devem ser commitados.

## Cabeçalhos e segurança

`cloudflare-pages/site/_headers` define Content Security Policy, bloqueia enquadramento do site e desativa recursos do navegador que não são usados. A política permite conexões somente ao próprio site e ao endpoint Frankfurter usado para taxas. Não há segredos no bundle estático.

O navegador recebe diretamente as requisições de cotações. Os dados do histórico permanecem no perfil local do navegador, sujeitos ao acesso de quem usa esse mesmo perfil.
