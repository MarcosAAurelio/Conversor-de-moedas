# Conversor de Moedas

Conversor de moedas com histórico e gráfico de cotações de referência. O resultado mostra a taxa usada e a data da cotação. O repositório mantém a aplicação Java original e uma versão estática para publicar no Cloudflare Pages.

## Versões

### Aplicação Java

O projeto original foi criado como portfólio de back-end Java. A aplicação usa Spring Boot, páginas Thymeleaf, API REST e Spring Data JPA. O perfil local usa H2; o perfil `prod` usa PostgreSQL. O histórico da aplicação Java é separado por sessão.

### Cloudflare Pages

A versão Pages usa HTML, CSS e JavaScript. O navegador consulta a [API pública Frankfurter](https://frankfurter.dev/) e guarda o histórico no `localStorage`. Esse histórico fica neste navegador, não sincroniza entre dispositivos e pode ser apagado junto com os dados do site. O Pages publica os arquivos estáticos; a API REST, o Swagger e o banco da aplicação Java não fazem parte dessa versão.

O diretório publicado é gerado por `npm run build:pages`. A configuração do projeto está em [docs/DEPLOY_CLOUDFLARE_PAGES.md](docs/DEPLOY_CLOUDFLARE_PAGES.md).

## Telas

| Tema claro | Tema escuro |
| --- | --- |
| ![Dashboard no tema claro](docs/screenshots/tema-claro.png) | ![Dashboard no tema escuro](docs/screenshots/tema-escuro.png) |

Capturas feitas em 24/09/2026 com a aplicação local. Os dois temas mostram a mesma cotação de referência consultada nessa data; os valores podem mudar durante o uso.

### Página Sobre

| Tema claro | Tema escuro |
| --- | --- |
| ![Página Sobre no tema claro](docs/screenshots/sobre-claro.png) | ![Página Sobre no tema escuro](docs/screenshots/sobre-escuro.png) |

Na página Sobre, o título "Meu nome é Marcos Aurélio." apresenta o autor do projeto. As capturas mostram um card inclinado e o foco de luz ativado pelo ponteiro. Os três cards respondem a esse movimento, com uma inclinação mais suave no maior. O efeito fica desligado em dispositivos sem ponteiro preciso e quando o sistema pede movimento reduzido.

## Funcionalidades

- Converter valores entre BRL, USD, EUR, GBP, JPY, CAD, ARS e CNY.
- Consultar o gráfico de cotações dos últimos 7, 30 ou 90 dias e abrir a tabela com os valores.
- Ver conversões recentes no dashboard e o histórico completo na página Histórico.
- Escolher português ou inglês e alternar entre os temas claro e escuro. As escolhas ficam salvas no navegador.
- Consultar a API Java pelo Swagger.
- Conhecer o autor e seus contatos na página Sobre.

## Executar a aplicação Java

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

## Publicar no Cloudflare Pages

Configure o Pages com a branch `main`, sem framework preset, comando `npm run build:pages` e diretório de saída `cloudflare-pages/site`. O build reúne as páginas de `cloudflare-pages/content` e os recursos compartilhados da aplicação Java. Veja [docs/DEPLOY_CLOUDFLARE_PAGES.md](docs/DEPLOY_CLOUDFLARE_PAGES.md) para os passos e limites do plano.

## Hospedar a aplicação Java

### Docker Compose

O deploy com Docker Compose usa Caddy para HTTPS automático, Nginx como proxy com limites por IP, a aplicação sem porta pública e PostgreSQL em uma rede interna. Requer um servidor Linux com Docker Engine e Compose v2, um domínio apontado para o servidor e as portas TCP 80/443 abertas. Para testar localmente, use `localhost` no domínio.

1. Copie `.env.example` para `.env` e substitua os três valores de senha por segredos aleatórios diferentes, com pelo menos 32 caracteres (por exemplo, execute `openssl rand -hex 32` três vezes). Restrinja o arquivo no servidor com `chmod 600 .env` e configure `DOMAIN` com o domínio real.
2. Inicie a stack:

   ```sh
   docker compose up --build -d
   docker compose ps
   ```

3. Acesse `https://SEU_DOMINIO`. Caddy solicita e renova os certificados TLS quando o DNS e as portas estão configurados corretamente.

Somente o Caddy publica portas para a Internet. No Nginx, conversões têm limite de 10 requisições por minuto por IP (com rajada de 8), cotações têm limite de 30 por minuto (rajada de 15) e a emissão de tokens CSRF tem limite de 5 por minuto (rajada de 10). O proxy também limita tamanho e tempo de requisições.

### Render

Consulte [docs/DEPLOY_RENDER.md](docs/DEPLOY_RENDER.md) antes de criar os serviços. O guia configura PostgreSQL gerenciado, aplicação privada e proxy público, com credenciais separadas para a aplicação e o Flyway. Essa configuração mantém uma instância da aplicação; para escalar horizontalmente, configure sessões compartilhadas ou afinidade de sessão.

Os registros ficam no volume `database_data`, com retenção padrão de 30 dias e limite de 100 conversões por sessão. A sessão anônima expira após 30 minutos de inatividade; não há contas de usuário nem recuperação de histórico depois que o cookie de sessão expira. Planeje backups criptografados fora do servidor. No Linux, uma exportação manual pode ser feita com `docker compose exec -T database pg_dump -U postgres -d currency_converter -Fc > backup.dump`; agende e teste a restauração de acordo com a infraestrutura escolhida. `docker compose down` mantém o volume de dados; remover o volume apaga o banco.

Para iniciar localmente, copie `.env.example` para `.env`, mantenha `DOMAIN=localhost`, substitua os segredos de exemplo e rode os mesmos comandos. O certificado local do Caddy não é reconhecido pelo navegador automaticamente.

## Tecnologias, observabilidade e testes

O backend usa Java, Spring Boot, Spring Web, Spring Security, Spring Data JPA, Thymeleaf e Bean Validation. O perfil local usa H2; o perfil `prod` usa PostgreSQL com migrações Flyway. A interface usa HTML, CSS, JavaScript e SVG. Springdoc OpenAPI gera a documentação da API e fica desativado no perfil `prod`.

O Actuator/OpenTelemetry e a captura de erros pelo Sentry ficam em modo local seguro por padrão. Consulte [docs/OBSERVABILITY.md](docs/OBSERVABILITY.md) para configurar OTLP, Sentry, Datadog ou New Relic.

Para rodar os testes Java:

```powershell
.\mvnw.cmd test
```

Lint, testes unitários, testes de integração e testes de navegador rodam nos Pull Requests. Consulte [docs/QUALITY.md](docs/QUALITY.md) para os comandos locais, mutation testing, ArchUnit e envio de cobertura ao Codecov.

As cotações são valores de referência e podem diferir das taxas oferecidas por instituições financeiras.

## Desenvolvimento

Correções, melhorias, novas funções e remediações começam por uma Issue e são entregues por Pull Request. Os passos para agentes e colaboradores estão em [AGENTS.md](AGENTS.md).

## Autor

Marcos Aurélio — estudante de Engenharia de Software e desenvolvedor em formação, com foco em back-end.

- [GitHub](https://github.com/MarcosAAurelio)
- [LinkedIn](https://www.linkedin.com/in/eu-marcosaurelio-dev)
