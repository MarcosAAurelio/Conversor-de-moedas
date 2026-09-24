# Auditoria de seguranca

**Data:** 2026-09-24  
**Escopo autorizado:** execucao local, `http://localhost:8080`. O historico global foi confirmado como intencional nesta versao local. Nenhuma URL de producao foi fornecida.  
**Base observada:** commit `c693784`, antes das correcoes deste PR.

## Metodo e limites

- Revisao estatica dos controllers, DTOs, validacao, cliente HTTP, entidade JPA, repositorio e configuracao.
- Revisao de dependencias com `mvnw dependency:tree`; versoes consultadas em fontes oficiais em 2026-09-24.
- Probes GET locais, sem autenticacao: `/`, `/h2-console` e `/api/conversoes/historico`. O historico estava vazio. Nenhum POST, DELETE, carga, fuzzing, varredura de portas ou teste contra outro host foi executado.
- Esta revisao nao certifica seguranca de uma implantacao remota. A exposicao de rede, TLS, proxy, firewall, identidade e configuracao de hospedagem estao fora do escopo.

## Achados

### SEC-01 — Console H2 ativo por padrao, com credencial vazia

**Severidade:** media se a porta estiver acessivel por terceiros; baixa para execucao isolada local.  
**Evidencia:** `spring.h2.console.enabled=true`, usuario `sa` e senha vazia em `src/main/resources/application.properties`. O GET local em `/h2-console` retornou redirecionamento HTTP 302; nao foram enviadas credenciais nem comandos SQL. A documentacao Spring diz que o console e apenas para desenvolvimento, nao oferece protecao CSRF e nao deve estar habilitado em producao.  
**Correcao neste PR:** console desabilitado por padrao; fica opt-in por `H2_CONSOLE_ENABLED=true`. O servidor tambem passa a vincular `127.0.0.1` por padrao. A documentacao local alerta para nao habilitar o console fora de um ambiente isolado. O modulo de console continua presente para esse uso local explicito.

Fontes: [documentacao Spring Boot sobre o console H2](https://docs.spring.io/spring-boot/reference/data/sql.html#data.sql.h2-web-console), [OWASP HTTP Headers](https://cheatsheetseries.owasp.org/cheatsheets/HTTP_Headers_Cheat_Sheet.html).

### SEC-02 — Entrada numerica podia exceder a precisao persistida

**Severidade:** baixa, impacto principal em erro de conversao e disponibilidade.  
**Evidencia:** o DTO exigia apenas valor positivo, enquanto `originalAmount` e `convertedAmount` usam precisao 19 e escala 6 no banco. Nao foi enviada uma requisicao grande contra o servidor.  
**Correcao neste PR:** validacao HTTP e de servico limita o valor a 13 digitos inteiros e 6 decimais, com testes de regressao na fronteira.

### SEC-03 — Cliente de cotacao sem timeouts explicitos

**Severidade:** baixa; um servico externo lento podia manter requisicoes abertas por tempo excessivo.  
**Evidencia:** `RestClientConfig` criava o cliente apenas com URL base. Nenhum teste de indisponibilidade prolongada foi feito.  
**Correcao neste PR:** timeout de conexao de 3 s e leitura de 5 s.

### SEC-04 — Historico global pode ser lido e apagado sem identidade

**Severidade:** aceita para a demonstracao local; alta se o servidor for disponibilizado a outros usuarios.  
**Evidencia:** `GET /api/conversoes/historico`, `DELETE /api/conversoes/historico`, `GET /historico` e `POST /historico/limpar` nao exigem autenticacao; o historico e compartilhado no banco H2 em memoria. O endpoint GET local retornou 200 e uma lista vazia. O DELETE nao foi executado.  
**Decisao de escopo:** o proprietario confirmou que o historico deve permanecer global nesta versao local. Nao foi adicionada identidade ficticia nem mudada a semantica. Antes de expor o servico a uma rede compartilhada, adicione autenticacao/autorizacao e decida a propriedade dos registros. OWASP classifica leitura, alteracao ou destruicao de dados fora da politica de acesso como falha de controle de acesso.

Fonte: [OWASP Top 10 2025 — A01 Broken Access Control](https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/).

### SEC-05 — Historico sem limite de crescimento ou paginacao

**Severidade:** baixa no escopo local; risco de consumo de memoria e respostas crescentes se houver uso continuo.  
**Evidencia:** cada conversao persistida entra no H2 em memoria e os endpoints de historico leem todos os registros. Nao foi feito teste de carga ou gerado volume artificial.  
**Estado:** pendente. Para um servico multiusuario, definir retencao, paginacao, limite de requisicoes e limites de tamanho de pedidos. Este risco corresponde a consumo de recursos sem restricao; nao ha evidencia de indisponibilidade real.

Fonte: [OWASP API Security 2023 — API4 Unrestricted Resource Consumption](https://owasp.org/API-Security/editions/2023/en/0xa4-unrestricted-resource-consumption/).

### SEC-06 — Respostas nao tinham cabecalhos basicos de navegador

**Severidade:** baixa; defesa em profundidade.  
**Evidencia:** a resposta GET `/` observada na base nao continha `X-Content-Type-Options`, `X-Frame-Options` nem `Referrer-Policy`. Nao foi identificado CSP; como a aplicacao usa scripts inline e Google Fonts, nao foi inventada uma politica CSP incompleta.  
**Correcao neste PR:** respostas agora definem `nosniff`, `DENY`, `strict-origin-when-cross-origin` e uma politica que desativa camera, microfone e geolocalizacao. Teste MockMvc cobre os cabecalhos principais.

Fonte: [OWASP HTTP Security Response Headers Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/HTTP_Headers_Cheat_Sheet.html).

### SEC-07 — Logs nao identificavam falhas inesperadas

**Severidade:** baixa no demo local; reduz investigacao e deteccao de abuso.  
**Evidencia:** `GlobalExceptionHandler` retornava erro generico para excecoes inesperadas sem registrar a causa. Nao foram enviados erros propositais ao servidor para validar alerta.  
**Estado:** encaminhado para a Issue de observabilidade. A mensagem HTTP generica evita devolver detalhes internos ao cliente; um destino de logs e alertas ainda precisa ser configurado.

### SEC-08 — Tomcat 11.0.24 continha tres advisories criticos

**Severidade:** exposicao critica de dependencia; explorabilidade nesta aplicacao nao confirmada.  
**Evidencia:** o OSV-Scanner 2.6.0, executado sobre o `pom.xml` em 2026-09-24, encontrou `tomcat-embed-core` 11.0.24 afetado por tres advisories, CVSS 9.8, 9.1 e 9.1. O primeiro requer autenticacao DIGEST; os outros dependem de autenticacao FORM e regras de seguranca para caminhos, recursos que este projeto nao configura. Portanto, o resultado prova uma versao vulneravel no grafo, nao uma rota exploravel demonstrada neste app.  
**Correcao neste PR:** Tomcat atualizado para 11.0.26, release estavel mais recente verificada em 2026-09-24. A versao 11.0.25 ja continha as correcoes listadas nos advisories; a 11.0.26 foi lancada em 2026-09-15. A varredura pos-atualizacao nao reportou advisories.

Fontes: [GHSA-9xv2-5v5q-p794 / CVE-2026-65905](https://osv.dev/vulnerability/GHSA-9xv2-5v5q-p794), [GHSA-gcx9-497g-6cp6 / CVE-2026-65182](https://osv.dev/vulnerability/GHSA-gcx9-497g-6cp6), [GHSA-h3x4-894j-xpx5 / CVE-2026-68525](https://osv.dev/vulnerability/GHSA-h3x4-894j-xpx5), [Apache Tomcat 11 security advisories](https://tomcat.apache.org/security-11), [Apache Tomcat 11.0.26 changelog](https://tomcat.apache.org/tomcat-11.0-doc/changelog.html).

**Resultado apos a correcao:** OSV-Scanner 2.6.0, chamado com `scan source --lockfile pom.xml --data-source native`, identificou 144 componentes na resolucao Maven e reportou `No issues found`. Nove entradas locais ou sem ecossistema scannable foram filtradas pelo scanner. O resultado e da base de advisories consultada em 2026-09-24 e nao demonstra que o codigo seja livre de vulnerabilidades.

### SEC-09 — Caminho inexistente responde 500 em vez de 404

**Severidade:** baixa; nao e acesso indevido, mas mascara recurso ausente como falha interna.  
**Evidencia:** depois de desativar o console, o GET local `/h2-console` retornou 500. `GlobalExceptionHandler` captura `NoResourceFoundException` pelo handler generico de `Exception`. O endpoint nao expõe dados nem torna o console acessivel.  
**Estado:** Issue #8 criada para devolver 404 e adicionar teste de regressao; este achado ainda nao esta corrigido nesta branch.

## OWASP Top 10:2025

| Categoria | Resultado desta revisao |
| --- | --- |
| A01 Broken Access Control | Acesso global sem identidade e intencional apenas no escopo local; rever antes de compartilhar a aplicacao. |
| A02 Security Misconfiguration | Console H2 ligado por padrao e cabecalhos ausentes foram corrigidos neste PR. |
| A03 Software Supply Chain Failures | Dependencias centrais antigas foram atualizadas; a resolucao Maven do OSV-Scanner nao reportou advisories conhecidos apos as atualizacoes. |
| A04 Cryptographic Failures | Nenhuma criptografia propria foi identificada nos arquivos revisados. TLS de producao nao foi avaliado. |
| A05 Injection | Moedas sao verificadas contra uma lista fixa e o acesso a dados usa JPA. Nenhuma injecao foi confirmada; fuzzing nao foi executado. |
| A06 Insecure Design | Historico global e politica aceita para uso local, mas nao e um desenho de isolamento multiusuario. |
| A07 Authentication Failures | Nao existe autenticacao; coerente apenas com o uso local demonstrativo informado pelo proprietario. |
| A08 Software or Data Integrity Failures | Nenhum fluxo de upload, atualizacao automatica ou artefato nao confiavel foi encontrado na superficie revista. |
| A09 Security Logging and Alerting Failures | Falhas inesperadas nao eram registradas; pendente junto a observabilidade. |
| A10 Mishandling of Exceptional Conditions | A resposta inesperada era generica, sem stack trace ao cliente, mas sem log server-side; timeouts externos foram adicionados. Caminho estatico inexistente ainda retorna 500; Issue #8 cobre essa correcao. |

Fonte de categorias: [OWASP Top 10:2025](https://top10.owasp.org/2025/0x00_2025-Introduction/).

## STRIDE

| Ameaca | Caminho observado e resultado |
| --- | --- |
| Spoofing | Nao ha contas, identidade ou tokens nesta versao. Qualquer cliente com acesso ao servidor e tratado da mesma forma; nao foi testado fora do loopback. |
| Tampering | Conversao e limpeza alteram o historico global. A limpeza e uma acao de escrita sem autenticacao, aceite apenas para o uso local. O console H2 agora e opt-in. |
| Repudiation | O historico armazena data/hora, mas nao ator; logs de seguranca nao estavam configurados. Sem identidade local, nao ha atribuicao individual. |
| Information Disclosure | O historico pode ser lido por qualquer cliente que alcancar a aplicacao. Estava vazio no probe. Acesso global foi confirmado como esperado localmente. |
| Denial of Service | Historico cresce sem limite e o cliente externo nao tinha timeout; o timeout foi corrigido. Nao houve teste de carga. |
| Elevation of Privilege | Nao ha papeis ou fronteira de privilegio de usuario. Com console H2 explicitamente habilitado, o operador local pode alterar dados diretamente; o recurso esta desabilitado por padrao. |

Metodo STRIDE: [Microsoft Threat Modeling Tool — STRIDE](https://learn.microsoft.com/en-us/azure/security/develop/threat-modeling-tool-threats).

## Dependencias e versoes consultadas

Versoes verificadas em fontes oficiais em 2026-09-24. As dependencias transitivas sao alinhadas pelo BOM do Spring Boot, em vez de serem atualizadas isoladamente para versoes possivelmente incompativeis.

| Componente direto | Antes | Atual consultada e usada |
| --- | --- | --- |
| Spring Boot | 3.3.5 | 4.1.1 |
| springdoc-openapi WebMVC UI | 2.6.0 | 3.1.1 |
| H2 | 2.2.224 (BOM anterior) | 2.5.250 |
| Apache Tomcat embed (transitivo) | 11.0.24 | 11.0.26 |

A migracao para Spring Boot 4 exigiu trocar o starter Web pelo WebMVC, modularizar dependencias de teste e substituir `@MockBean` por `@MockitoBean`. O projeto continua com alvo Java 17, minimo exigido pelo Spring Boot 4. O grafo exato pos-atualizacao esta disponivel em `mvnw dependency:tree`.

Fontes: [Spring Boot 4.1.1 release](https://spring.io/blog/2026/08/20/spring-boot-4-1-1-available-now/), [guia de migracao Spring Boot 4](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide), [Maven Central springdoc 3.1.1](https://central.sonatype.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui), [Maven Central H2 2.5.250](https://central.sonatype.com/artifact/com.h2database/h2), [springdoc confirma suporte Spring Boot 4 pela linha v3](https://github.com/springdoc/springdoc-openapi), [Apache Tomcat 11.0.26 release](https://tomcat.apache.org/tomcat-11.0-doc/changelog.html).

## Conclusao

Nao foi confirmada exploracao remota nem acesso a producao. Os achados de configuracao, validacao, timeout, cabecalhos e tres advisories de Tomcat foram corrigidos e cobertos pelo suite local, scanner ou ambos. O caminho inexistente que retorna 500 foi registrado separadamente na Issue #8. O historico sem autenticacao e o crescimento ilimitado permanecem riscos conhecidos do demo local; a primeira mudanca necessaria para uso compartilhado e definir identidade, autorizacao, retencao e limites.
