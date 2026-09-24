# Conversor de Moedas

Aplicação web para converter moedas e consultar a evolução das cotações. O resultado mostra a taxa utilizada e a data de referência. Cada conversão fica no histórico enquanto a aplicação estiver em execução.

## Telas

| Tema claro | Tema escuro |
| --- | --- |
| ![Dashboard no tema claro](docs/screenshots/tema-claro.png) | ![Dashboard no tema escuro](docs/screenshots/tema-escuro.png) |

Os prints mostram o dashboard em 21/09/2026. As cotações exibidas no site são consultadas durante o uso e podem mudar.

## O que dá para fazer

- Converter valores entre BRL, USD, EUR, GBP, JPY, CAD, ARS e CNY.
- Consultar o gráfico dos últimos 7, 30 ou 90 registros de cotação e abrir a tabela com os valores exatos.
- Ver as conversões recentes no dashboard ou o histórico completo na página **Histórico**.
- Alternar entre português e inglês e entre os temas claro e escuro. As escolhas ficam salvas no navegador.
- Consultar os endpoints pela interface do Swagger.
- Conhecer o criador do projeto e seus contatos na página **Sobre**.

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

O histórico usa um banco H2 em memória. Ao encerrar a aplicação, as conversões salvas são perdidas. O console do banco fica em `http://localhost:8080/h2-console`, com URL JDBC `jdbc:h2:mem:currency_converter`, usuário `sa` e senha vazia.

## API

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/conversoes` | Converte um valor e salva a operação no histórico |
| `GET` | `/api/conversoes/historico` | Lista as conversões salvas |
| `DELETE` | `/api/conversoes/historico` | Limpa o histórico |
| `GET` | `/api/cotacoes/historico?origem=BRL&destino=USD&dias=7` | Consulta as cotações usadas pelo gráfico |

O parâmetro `dias` aceita `7`, `30` ou `90`. Uma conversão pode ser enviada assim:

```http
POST /api/conversoes
Content-Type: application/json

{"amount":100.00,"sourceCurrency":"BRL","targetCurrency":"USD"}
```

## Tecnologias e testes

O backend usa Java, Spring Boot, Spring Web, Spring Data JPA, Thymeleaf, Bean Validation e H2. A interface usa HTML, CSS, JavaScript e SVG. A documentação da API é gerada com Springdoc OpenAPI.

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
