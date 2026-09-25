# Deploy na Render

O deploy na Render usa três recursos: um PostgreSQL gerenciado, um serviço privado para a aplicação Java e um serviço web público com Nginx. O Nginx recebe o tráfego público, mantém os limites por IP e encaminha as chamadas à aplicação pela rede privada da Render. A Render encerra TLS no serviço web; não crie um serviço Caddy.

O serviço privado e o banco persistente exigem planos pagos para produção. O banco gratuito da Render expira após 30 dias; serviços web gratuitos também podem suspender quando ociosos e não são recomendados para produção.

## 1. Criar o PostgreSQL

1. Crie um PostgreSQL com o nome de banco `currency_converter`. Use um plano pago para produção; bancos gratuitos expiram após 30 dias.
2. Escolha uma região e use a mesma para o PostgreSQL e os dois serviços da aplicação.
3. Guarde a credencial inicial do banco em um gerenciador de segredos antes de adicionar outras credenciais.
4. Na seção **Credentials**, crie duas credenciais com os nomes `converter_migrator` e `converter_app`. Gere senhas diferentes e guarde cada uma. A credencial `converter_migrator` será usada pelo Flyway; `converter_app` será usada pelas consultas da aplicação.
5. Temporariamente permita no banco o IP de onde você executará os comandos SQL abaixo. Use a URL externa do banco com TLS (`sslmode=require`) para a conexão administrativa.

Execute `deploy/postgres/render-grants-owner.sql` com a credencial inicial do banco. Depois execute `deploy/postgres/render-grants-migrator.sql` autenticando como `converter_migrator`. Os arquivos concedem ao migrador criação de tabelas e ao usuário da aplicação apenas leitura e escrita nas tabelas e sequências do schema `public`.

Após aplicar as permissões, restrinja o acesso externo ao PostgreSQL na seção **Networking**. Os serviços Render na mesma região continuam conectando pela URL interna. A aplicação deve começar a usar o banco só depois que as duas credenciais e permissões estiverem prontas.

## 2. Criar o serviço privado da aplicação

Crie um **Private Service** conectado ao repositório e à branch `main`, com Runtime `Docker` e Dockerfile `./Dockerfile`. Escolha o mesmo workspace e região do PostgreSQL.

Configure estas variáveis no serviço:

| Variável | Valor |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `APP_PORT` | `8080` |
| `DATABASE_URL` | `jdbc:postgresql://<host-interno>:5432/currency_converter` |
| `DATABASE_USERNAME` | `converter_app` |
| `DATABASE_PASSWORD` | Senha da credencial Render `converter_app` |
| `SPRING_FLYWAY_USER` | `converter_migrator` |
| `SPRING_FLYWAY_PASSWORD` | Senha da credencial Render `converter_migrator` |
| `HISTORY_RETENTION_DAYS` | `30` |

Use o host interno exibido no painel do PostgreSQL; não inclua usuário nem senha em `DATABASE_URL`. Adicione `SENTRY_DSN` somente se já tiver configurado um projeto Sentry.

Não crie um domínio público para este serviço. Ele deve aceitar chamadas apenas pela rede privada Render.

## 3. Criar o serviço web público

Crie um **Web Service** ligado ao mesmo repositório e branch `main`, com Runtime `Docker` e caminho de Dockerfile `./deploy/nginx/Dockerfile` e Docker Context `.` (raiz do repositório). Selecione o mesmo workspace e região dos outros recursos.

Configure a variável `APP_UPSTREAM` com o endereço interno completo do serviço privado da aplicação, no formato `host:8080` exibido no painel **Connect** do serviço. Deixe `PORT` no padrão `10000` da Render. Configure o caminho de health check como `/actuator/health`.

Adicione o domínio personalizado nas configurações do serviço web e siga as instruções de DNS da Render. TLS é gerenciado pela Render. Somente esse serviço web deve ter URL pública.

## 4. Verificar após o deploy

- Confirme que o deploy do serviço privado fica saudável e que o serviço web retorna a página inicial por HTTPS.
- Confirme que `/actuator/health` responde sem expor detalhes do banco.
- Confirme que requisições acima dos limites do Nginx recebem `429`.
- Confirme que `converter_app` não consegue criar tabelas ou alterar o schema.
- Mantenha uma instância da aplicação: as sessões anônimas são locais ao processo. Escala horizontal requer sessões compartilhadas ou afinidade de sessão.
- Configure backups e teste uma restauração do PostgreSQL antes de considerar o deploy pronto para uso contínuo.

O `Dockerfile` da raiz inicia somente a aplicação e deve ser usado pelo serviço privado. O serviço público usa o Dockerfile do Nginx. Publicar diretamente o `Dockerfile` da raiz como Web Service ignora os limites por IP configurados no proxy.
