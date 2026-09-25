# Conversor de Moedas

Aplicação web para converter moedas e consultar a evolução das cotações. O resultado mostra a taxa utilizada e a data de referência.

## Versões do projeto

O projeto foi criado como uma aplicação de portfólio com foco em back-end Java: Spring Boot, páginas Thymeleaf, API REST, persistência com Spring Data JPA e histórico separado por sessão. A execução local usa H2; o perfil de produção Java usa PostgreSQL.

Para publicar uma demonstração sem custo de hospedagem no Cloudflare Pages, o repositório também contém uma versão estática em HTML, CSS e JavaScript. Ela mantém o conversor, o gráfico, os temas, os idiomas e as telas de histórico e sobre. O Cloudflare Pages publica essa versão estática; ele não executa a JVM nem o servidor Spring Boot. O código e as instruções da aplicação Java continuam no repositório.

Na versão Pages, o navegador consulta diretamente a [API pública Frankfurter](https://frankfurter.dev/) e guarda o histórico no armazenamento local do navegador. Esse histórico não é compartilhado com a aplicação Java, não sincroniza entre dispositivos e pode ser apagado ao limpar os dados do site. A versão estática não inclui os endpoints REST nem o Swagger da aplicação Java.

Consulte [docs/DEPLOY_CLOUDFLARE_PAGES.md](docs/DEPLOY_CLOUDFLARE_PAGES.md) para configurar a publicação estática. A documentação de execução local, Docker Compose e Render abaixo se refere à aplicação original Java.

## Telas

| Tema claro | Tema escuro |
| --- | --- |
| ![Dashboard no tema claro](docs/screenshots/tema-claro.png) | ![Dashboard no tema escuro](docs/screenshots/tema-escuro.png) |

Capturas feitas em 24/09/2026 com a aplicação local. Os dois temas mostram a mesma cotação de referência consultada nessa data; os valores podem mudar durante o uso.

### Página Sobre

| Tema claro | Tema escuro |
| --- | --- |
| ![Página Sobre no tema claro](docs/screenshots/sobre-claro.png) | ![Página Sobre no tema escuro](docs/screenshots/sobre-escuro.png) |

O título "Meu nome é Marcos Aurélio." apresenta o criador do projeto. As capturas mostram seu card com o ponteiro sobre ele, revelando a inclinação 3D e o foco de luz. Os três cards respondem ao ponteiro; o maior usa um movimento mais suave. O efeito é desativado em dispositivos sem ponteiro preciso e quando o sistema solicita movimento reduzido.

## O que dá para fazer

- Converter valores entre BRL, USD, EUR, GBP, JPY, CAD, ARS e CNY.
- Consultar o gráfico dos últimos 7, 30 ou 90 registros de cotação e abrir a tabela com os valores exatos.
- Ver as conversões recentes no dashboard ou o histórico completo na página **Histórico**.
- Alternar entre português e inglês e entre os temas claro e escuro. As escolhas ficam salvas no navegador.
- Consultar os endpoints pela interface do Swagger.
- Conhecer o criador do projeto e seus contatos na página **Sobre**, com cards interativos que respeitam a preferência por movimento reduzido.

Esses recursos descrevem a aplicação Java original. A versão do Cloudflare Pages mantém as páginas públicas e o conversor, mas não oferece Swagger nem uma API própria.

## Executar localmente

É necessário ter o JDK 17 ou superior. Na raiz do projeto, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

No macOS ou Linux, use `./mvnw spring-boot:run`.

| Página | Endereço |
| --- | --- |
| Conversor | http://localhost:8080/ |
| Histórico | http://localhost:8080/historico |
| Sobre | http://localhost:8080/sobre |
| Swagger | http://localhost:8080/swagger-ui.html |

O perfil local se vincula somente a `127.0.0.1` e usa H2 em memória. Cada navegador recebe um histórico separado; os dados locais são apagados quando o processo termina. O console H2 não é incluído na aplicação.

## API da aplicação Java

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/conversoes` | Converte um valor e salva a operação no histórico |
| `GET` | `/api/conversoes/historico` | Lista as conversões salvas |
| `DELETE` | `/api/conversoes/historico` | Limpa o histórico |
| `GET` | `/api/cotacoes/historico?origem=BRL&destino=USD&dias=7` | Consulta as cotações usadas pelo gráfico |

O parâmetro `dias` aceita `7`, `30` ou `90`. Os endpoints de histórico mostram e limpam apenas os dados da sessão atual. Chamadas `POST` e `DELETE` exigem CSRF: primeiro consulte `GET /api/security/csrf`, mantenha o cookie de sessão retornado e envie o token no cabeçalho indicado por `headerName` na resposta. Exemplo:

```http
GET /api/security/csrf
```

Depois, mantendo o cookie de sessão:

```http
POST /api/conversoes
Content-Type: application/json
X-CSRF-TOKEN: <token retornado pelo endpoint CSRF>

{"amount":100.00,"sourceCurrency":"BRL","targetCurrency":"USD"}
```

## Deploy com Docker Compose

O deploy incluído usa Caddy para HTTPS automático, Nginx como proxy com limites por IP, a aplicação sem porta pública e PostgreSQL em uma rede interna. Requer um servidor Linux com Docker Engine e Compose v2, um domínio apontado para o servidor e as portas TCP 80/443 abertas. Para testar localmente, use `localhost` no domínio.

1. Copie `.env.example` para `.env` e substitua os três valores de senha por segredos aleatórios diferentes, com pelo menos 32 caracteres (por exemplo, execute `openssl rand -hex 32` três vezes). Restrinja o arquivo no servidor com `chmod 600 .env` e configure `DOMAIN` com o domínio real.
2. Inicie a stack:

   ```sh
   docker compose up --build -d
   docker compose ps
   ```

3. Acesse `https://SEU_DOMINIO`. Caddy solicita e renova os certificados TLS quando o DNS e as portas estão configurados corretamente.

Somente o Caddy publica portas para a Internet. No Nginx, conversões têm limite de 10 requisições por minuto por IP (com rajada de 8), cotações têm limite de 30 por minuto (rajada de 15) e a emissão de tokens CSRF tem limite de 5 por minuto (rajada de 10). O proxy também limita tamanho e tempo de requisições. Para publicar na Render, siga [docs/DEPLOY_RENDER.md](docs/DEPLOY_RENDER.md): a configuração mantém a aplicação privada e usa o Nginx como serviço público com os mesmos limites por IP. Essa configuração mantém uma instância da aplicação; para escalar horizontalmente, configure sessões compartilhadas ou afinidade de sessão.

## Deploy na Render

O repositório inclui um guia para configurar PostgreSQL gerenciado, aplicação privada e proxy público na Render. Siga [docs/DEPLOY_RENDER.md](docs/DEPLOY_RENDER.md) antes de criar os serviços; o fluxo configura credenciais de banco com permissões separadas para a aplicação e para o Flyway.

Os registros ficam no volume `database_data`, com retenção padrão de 30 dias e limite de 100 conversões por sessão. A sessão anônima expira após 30 minutos de inatividade; não há contas de usuário nem recuperação de histórico depois que o cookie de sessão expira. Planeje backups criptografados fora do servidor. No Linux, uma exportação manual pode ser feita com `docker compose exec -T database pg_dump -U postgres -d currency_converter -Fc > backup.dump`; agende e teste a restauração de acordo com a infraestrutura escolhida. `docker compose down` mantém o volume de dados; remover o volume apaga o banco.

Para iniciar localmente, copie `.env.example` para `.env`, mantenha `DOMAIN=localhost`, substitua os segredos de exemplo e rode os mesmos comandos. O certificado local do Caddy não é reconhecido pelo navegador automaticamente.

## Tecnologias e testes

O backend usa Java, Spring Boot, Spring Web, Spring Security, Spring Data JPA, Thymeleaf e Bean Validation. O perfil local usa H2; o perfil `prod` usa PostgreSQL com migrações Flyway. A interface usa HTML, CSS, JavaScript e SVG. A documentação da API é gerada com Springdoc OpenAPI e fica desativada no perfil `prod`.

Para rodar os testes:

```powershell
.\mvnw.cmd test
```

As cotações servem como referência e podem ser diferentes das taxas oferecidas por instituições financeiras.

## Desenvolvimento

Toda correção, melhoria, nova função ou remediação começa por uma Issue e é entregue por um Pull Request que a referencia. Os passos para agentes e colaboradores estão em [AGENTS.md](AGENTS.md).

## Autor

Marcos Aurélio — estudante de Engenharia de Software e desenvolvedor em formação, com foco em back-end.

- [GitHub](https://github.com/MarcosAAurelio)
- [LinkedIn](https://www.linkedin.com/in/eu-marcosaurelio-dev)

## Observabilidade

O Actuator/OpenTelemetry e a captura de erros pelo Sentry estão configurados em modo local seguro por padrão. Consulte [docs/OBSERVABILITY.md](docs/OBSERVABILITY.md) para configurar OTLP, Sentry, Datadog ou New Relic.

## Qualidade e testes

Lint, testes unitários, testes de integração e testes de navegador rodam automaticamente nos PRs. Consulte [docs/QUALITY.md](docs/QUALITY.md) para comandos locais, mutation testing, ArchUnit e envio de cobertura ao Codecov.
