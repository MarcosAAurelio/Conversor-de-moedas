# Publicar no Cloudflare Pages

O Cloudflare Pages publica a versão estática do conversor. Esse serviço não executa o Spring Boot nem o código Java do projeto.

## Arquivos publicados

- As páginas específicas do Pages ficam em `cloudflare-pages/content`.
- CSS, scripts compartilhados e a imagem do autor vêm de `src/main/resources/static`.
- `npm run build:pages` combina esses arquivos em `cloudflare-pages/site`. A pasta de saída é gerada durante o build e não precisa ser commitada.
- O site consulta a API pública Frankfurter no navegador. Ela não exige chave. O valor digitado e o histórico não são enviados à API; o navegador envia os códigos das moedas e o período solicitado ao gráfico.
- O histórico fica no `localStorage` do navegador. Ele não sincroniza entre dispositivos e pode ser apagado junto com os dados do site.
- A versão Pages não inclui a API REST, o Swagger ou o banco PostgreSQL da aplicação Java.

## Configurar o Pages

1. No Cloudflare, crie um projeto Pages conectado ao repositório `MarcosAAurelio/Conversor-de-moedas`.
2. Defina `main` como branch de produção.
3. Escolha **None** como framework preset e mantenha o diretório raiz do repositório.
4. Configure os campos:

   | Campo | Valor |
   | --- | --- |
   | Build command | `npm run build:pages` |
   | Build output directory | `cloudflare-pages/site` |

5. Salve e faça o primeiro deploy. O site ficará disponível em um endereço `*.pages.dev`; não é preciso configurar um domínio próprio.

O arquivo `.nvmrc` seleciona Node.js 24.21.0 para o build. O Cloudflare Pages aceita a versão de Node indicada por `.nvmrc`; consulte a [documentação do ambiente de build](https://developers.cloudflare.com/pages/configuration/build-image/). Depois que o PR for integrado a `main`, os próximos commits nessa branch iniciam novos builds.

## Custo e limites

Esta configuração usa apenas arquivos estáticos, sem Pages Functions, Workers ou banco de dados. No plano gratuito, as requisições aos arquivos estáticos são gratuitas e ilimitadas, e há um limite de 500 builds por mês. Confira os [limites atuais do Pages](https://developers.cloudflare.com/pages/platform/limits/) antes de configurar outros recursos.

A API de cotações é um serviço separado, fora do controle deste repositório. Esta versão não usa chave de API nem envia o valor da conversão ao Frankfurter.

## Gerar os arquivos localmente

Instale a versão de Node.js indicada por `.nvmrc` e execute na raiz do repositório:

```sh
npm run build:pages
```

O script copia os arquivos compartilhados da aplicação Java e monta `cloudflare-pages/site`. Não edite essa pasta diretamente; faça mudanças nas páginas em `cloudflare-pages/content` ou nos arquivos originais em `src/main/resources/static` e gere a saída novamente.

## Cabeçalhos e segurança

O arquivo `cloudflare-pages/content/_headers` define a Content Security Policy, impede que o site seja carregado em frames e desativa recursos do navegador que a aplicação não usa. A política permite conexões ao próprio site e à API Frankfurter. Não há segredos no conteúdo estático.

As cotações são consultadas diretamente pelo navegador. O histórico permanece no perfil local de quem usa o site.
