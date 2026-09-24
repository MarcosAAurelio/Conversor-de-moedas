# Observabilidade

O projeto usa Spring Boot Actuator, Micrometer e OpenTelemetry para métricas e traces. Erros inesperados podem ser enviados ao Sentry. O histórico de conversões permanece global no banco H2 local nesta versão.

## Modo local

Por padrão, a aplicação escuta somente em `127.0.0.1`, não envia telemetria para serviços externos e deixa o Sentry desativado enquanto `SENTRY_DSN` estiver vazio. Os endpoints HTTP do Actuator expostos são apenas `/actuator/health` e `/actuator/info`; detalhes de componentes não são incluídos na resposta de saúde. Métricas e dados de diagnóstico não são expostos por uma rota pública.

Execute os comandos a seguir no PowerShell para enviar traces e métricas a um coletor OTLP local:

```powershell
$env:OTEL_TRACES_EXPORT_ENABLED = 'true'
$env:OTEL_METRICS_EXPORT_ENABLED = 'true'
$env:OTEL_EXPORTER_OTLP_ENDPOINT = 'http://127.0.0.1:4318'
$env:OTEL_EXPORTER_OTLP_PROTOCOL = 'http/protobuf'
.\mvnw.cmd spring-boot:run
```

O endpoint geral recebe automaticamente os caminhos `/v1/traces` e `/v1/metrics`. Para escolher endpoints separados, use `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT` e `OTEL_EXPORTER_OTLP_METRICS_ENDPOINT`. Os atributos do serviço usam `OTEL_SERVICE_NAME` e `OTEL_RESOURCE_ATTRIBUTES`; o nome padrão é `currency-converter`.

As chamadas HTTP de saída usam o `RestClient.Builder` configurado pelo Spring, permitindo que os contextos de trace acompanhem chamadas ao Frankfurter.

## Sentry

Defina `SENTRY_DSN` no ambiente para ativar a captura de exceções não esperadas. Para produção, também defina `SENTRY_TRACES_SAMPLE_RATE` com uma taxa apropriada entre `0.0` e `1.0`. A opção `sentry.send-default-pii=false` fica habilitada para evitar o envio automático de dados pessoais. Não grave DSNs nem chaves nos arquivos do repositório.

## Datadog e New Relic

Ambos recebem dados OpenTelemetry via OTLP. Aponte `OTEL_EXPORTER_OTLP_ENDPOINT` para um OpenTelemetry Collector ou para um endpoint compatível do fornecedor, ative as exportações com as variáveis acima e forneça os cabeçalhos de autenticação exigidos pelo destino via `OTEL_EXPORTER_OTLP_HEADERS`. Use TLS e guarde chaves no gerenciador de segredos do ambiente. O endpoint e o formato de autenticação dependem do site/região da conta.

Um Collector é preferível quando for necessário enviar os mesmos dados para mais de um fornecedor. Sentry usa o SDK Java incluído aqui para captura de erros; ele não depende da exportação OTLP configurada para Datadog/New Relic.

## Endpoints

- `GET /actuator/health`: estado sem detalhes de componentes.
- `GET /actuator/info`: informações básicas do serviço.
- Métricas e detalhes de saúde ficam fora da exposição HTTP; métricas podem ser exportadas por OTLP quando habilitadas.

Em ambiente fora do computador local, proteja os endpoints operacionais na rede e configure autenticação/autorização antes de ampliar a lista exposta.

## Referências

- [Spring Boot: observabilidade e variáveis OTLP](https://docs.spring.io/spring-boot/reference/actuator/observability.html)
- [Spring Boot: tracing e propagação com `RestClient.Builder`](https://docs.spring.io/spring-boot/reference/actuator/tracing.html)
- [Sentry para Spring Boot](https://docs.sentry.io/platforms/java/guides/spring-boot/)
- [Datadog: recepção OTLP](https://docs.datadoghq.com/opentelemetry/setup/otlp_ingest_in_the_agent/)
- [New Relic: endpoint OTLP](https://docs.newrelic.com/docs/opentelemetry/best-practices/opentelemetry-otlp/)
