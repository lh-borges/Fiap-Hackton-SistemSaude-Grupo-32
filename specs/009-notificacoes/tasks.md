# Tarefas — Notificações

- **Spec:** `./spec.md` · **Plano:** `./plan.md` · **Responsável:** Danilo
- **Convenção:** `[P]` = pode rodar em paralelo (arquivos distintos, sem dependência).
- **Ordem obrigatória:** contrato e migration → domínio → aplicação → infraestrutura →
  apresentação → integração e eventos → produtores → entrega.
- **Build:** `mvn verify` com `JAVA_HOME` apontando para um JDK 21.

---

## Fase 0 — Preparação

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T901 | Resolver pendências da spec (RN-08, RN-09, RN-10) e aprová-la | `spec.md` | — | | [x] |
| T902 | Revisar catálogo de eventos contra o código existente | `specs/000-plataforma-sus/events.md` | — | | [x] |
| T903 | Contrato OpenAPI | `contracts/openapi.yaml` | — | [P] | [x] |
| T904 | Migration do módulo | `db/migration/V0082__not_notificacao.sql` | — | [P] | [x] |
| T905 | Migration da tabela `event_publication` (copiar `schema-postgresql.sql` do jar `spring-modulith-events-jdbc:1.4.3`) | `db/migration/V0091__infra_event_publication.sql` | T910 | | [x] |
| T906 | Registrar `V0082` e `V0091` e a coluna `evento_id` no modelo de dados global | `specs/000-plataforma-sus/data-model.md` | — | [P] | [x] |

## Fase 1 — Infraestrutura de eventos (transversal)

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T910 | Dependências: `spring-modulith-starter-jdbc`, `spring-modulith-events-api`, `spring-modulith-events-kafka`, `spring-modulith-events-jackson` | `pom.xml` | — | | [x] |
| T911 | Envelope `EventoDominio` | `shared/domain/event/EventoDominio.java` | — | [P] | [x] |
| T912 | Propriedades: externalização desligada por padrão, `republish-outstanding-events-on-restart`, `schema-initialization` desligada, `spring.kafka.bootstrap-servers` | `application.yml`, `application-local.yml`, `application-docker.yml` | T910 | | [x] |
| T913 | Compose: `api` recebe `KAFKA_BOOTSTRAP_SERVERS` e `MESSAGING_ENABLED`; healthcheck no `kafka`; `api` depende do `kafka` | `docker-compose.yml`, `.env.example` | — | [P] | [x] |
| T914 | **Verificação:** `mvn verify` continua verde com as dependências novas (H2 + registro JDBC) | build | T905, T910, T912 | | [x] |

## Fase 2 — Domínio (sem framework)

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T920 | Enum `TipoNotificacao` com o recurso de origem de cada valor | `notificacoes/domain/enums/` | — | [P] | [x] |
| T921 | Entidade `Notificacao` (nasce não lida; `marcarComoLida` idempotente; `pertenceA`; limites de tamanho) | `notificacoes/domain/model/` | T920 | | [x] |
| T922 | `NotificacaoFiltro` e portas `NotificacaoRepository`, `EventoProcessadoRepository` | `notificacoes/domain/repository/` | T921 | | [x] |
| T923 | **Teste unitário** de `Notificacao` (RN-04: leitura única e irreversível; posse; obrigatórios) e de `TipoNotificacao` | `test/.../notificacoes/domain/` | T921 | | [x] |

## Fase 3 — Aplicação (casos de uso)

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T930 | DTOs `NotificacaoOutput`, `ContagemNaoLidasOutput`, `FatoNotificavel` (com `Destinatario`) | `notificacoes/application/dto/` | T921 | [P] | [x] |
| T931 | `CatalogoMensagens`: título e mensagem por tipo e papel do destinatário, sem dado clínico; datas em `America/Sao_Paulo` | `notificacoes/application/mensagem/` | T920 | [P] | [x] |
| T932 | `ProcessarFatoUseCase` (dedup por `eventoId`, marcador na mesma transação, descarte de destinatário sem usuário, violação de unicidade tratada como duplicata) | `notificacoes/application/usecase/` | T922, T930, T931 | | [x] |
| T933 | `ListarMinhasNotificacoesUseCase`, `ConsultarMinhaNotificacaoUseCase`, `ContarNaoLidasUseCase` com `@PreAuthorize("isAuthenticated()")` e `usuarioId` do token | `notificacoes/application/usecase/` | T922, T930 | [P] | [x] |
| T934 | `MarcarNotificacaoComoLidaUseCase` (alheia → `RecursoNaoEncontradoException`) | `notificacoes/application/usecase/` | T922, T930 | [P] | [x] |
| T935 | **Teste unitário** de `ProcessarFatoUseCase`: fato novo com 2 destinatários → 2 notificações; mesmo `eventoId` → 0 e log; destinatário sem usuário → só os demais (HU-04, RN-02, RN-03, RN-10, EX-01, EX-02) | `test/.../notificacoes/application/` | T932 | | [x] |
| T936 | **Teste unitário** de `CatalogoMensagens`: nenhuma mensagem contém os parâmetros clínicos proibidos; todos os tipos têm texto (RN-05, RNF) | `test/.../notificacoes/application/` | T931 | [P] | [x] |
| T937 | **Teste unitário** dos casos de uso de leitura e de marcação: posse (usuário diferente → 404), filtro `lida`, contagem (HU-02, HU-03, EX-04) | `test/.../notificacoes/application/` | T933, T934 | [P] | [x] |

## Fase 4 — Infraestrutura do módulo

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T940 | `package-info.java` do módulo com `allowedDependencies` da seção 2 do plano | `notificacoes/package-info.java` | — | [P] | [x] |
| T941 | Entidades JPA `NotificacaoEntity`, `EventoProcessadoEntity` | `notificacoes/infrastructure/persistence/entity/` | T904 | [P] | [x] |
| T942 | `NotificacaoPersistenceMapper` | `notificacoes/infrastructure/persistence/mapper/` | T941 | | [x] |
| T943 | Adapters e interfaces Spring Data (listagem ordenada por `criado_em desc`, filtro `lida`, `countByUsuarioIdAndLidaFalse`, `existsByEventoId`) | `notificacoes/infrastructure/persistence/repository/` | T942, T922 | | [x] |
| T944 | `DestinatarioResolver` (`pacienteId`/`medicoId` → `usuarioId` via `CadastroQuery`, `Optional` quando não resolve) | `notificacoes/infrastructure/messaging/` | T930 | [P] | [x] |
| T945 | `NotificacoesAsyncConfig` com `@EnableAsync` | `notificacoes/infrastructure/config/` | — | [P] | [x] |
| T946 | **Teste de integração** dos adapters com `@DataJpaTest` + H2 + `@Sql` da `V0082`: ordenação, filtro, contagem, `UNIQUE (evento_id, usuario_id)` | `test/.../notificacoes/infrastructure/` | T943 | | [x] |

## Fase 5 — Apresentação

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T950 | `NotificacaoResponse` (com `recurso`), `ContagemNaoLidasResponse` | `notificacoes/presentation/response/` | T930 | [P] | [x] |
| T951 | `NotificacaoController` com anotações OpenAPI (4 rotas do contrato) | `notificacoes/presentation/controller/` | T933, T934, T950 | | [x] |
| T952 | **Teste de endpoint**: listagem paginada, filtro `lida`, contagem, detalhe, marcação, `tamanho` > 100 → 400 | `test/.../notificacoes/presentation/` | T951 | | [x] |
| T953 | **Teste de autorização**: sem autenticação → 401; notificação de terceiro em GET e PATCH → 404; cada perfil só vê as suas | `test/.../notificacoes/presentation/` | T951 | | [x] |

## Fase 6 — Consumo de eventos

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T960 | `NotificacaoEventListener`: um `@ApplicationModuleListener` por evento (10), montando o `FatoNotificavel` e chamando `ProcessarFatoUseCase` | `notificacoes/infrastructure/messaging/` | T932, T944, T970–T976 | | [x] |
| T961 | **Teste de módulo** com `@ApplicationModuleTest` + `Scenario`: `ConsultaAgendadaEvent` → 2 notificações; republicar o mesmo `eventoId` → nada novo; `ResultadoExameDisponivelEvent` → paciente e médico (HU-01, HU-04, HU-05) | `test/.../notificacoes/NotificacoesModuloTest.java` | T960 | | [x] |
| T962 | `ModularidadeTest` e `ArquiteturaTest` verdes com o módulo novo | build | T960 | | [x] |

## Fase 7 — Produtores (módulos de Thiago e Juliana; PR pequeno e coordenado)

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T970 | `consultas`: 3 eventos + `package-info` `events`; `@Transactional` e `publishEvent` em `Agendar`, `Remarcar`, `Cancelar`; testes unitários verificam a publicação | `consultas/application/event/`, `consultas/application/usecase/` | T911 | [P] | [x] |
| T971 | `exames`: `ExameQuery.medicoSolicitanteIdDoExame` + adapter | `exames/api/ExameQuery.java`, adapter em `exames/infrastructure` | — | [P] | [x] |
| T972 | `exames`: 2 eventos + `package-info`; `@Transactional` e `publishEvent` em `Solicitar` e `Agendar`; testes | `exames/application/event/`, `exames/application/usecase/` | T911 | [P] | [x] |
| T973 | `resultados`: `ResultadoExameDisponivelEvent` + `package-info`; `@Transactional` e `publishEvent` nos dois `RegistrarResultado*`; testes | `resultados/application/` | T911, T971 | | [x] |
| T974 | `pareceres`: `ParecerCriadoEvent` com `eventoId`, `ocorridoEm`, `EventoDominio`, `@Externalized`; ajustar testes | `pareceres/application/event/` | T911 | [P] | [x] |
| T975 | `receitas`: 2 eventos + `package-info`; `@Transactional` e `publishEvent` em `Emitir` e `Renovar`; testes | `receitas/application/` | T911 | [P] | [x] |
| T976 | `documentos`: `package-info` `events`; `DocumentoEmitidoEvent` com envelope e `@Externalized`; `@Transactional` em `EmitirDocumentoUseCase`; ajustar testes | `documentos/application/` | T911 | [P] | [x] |

## Fase 8 — Entrega e demonstração

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T980 | `docker compose up --build`: agendar consulta pelo Postman, listar notificações do paciente e do médico, ler o tópico com `kafka-console-consumer` | ambiente | T914, T962 | | [x] |
| T981 | Pasta **14. Notificações** na coleção Postman (listar, contagem, detalhe, marcar como lida) | `postman/SUS-Plataforma.postman_collection.json` | T951 | [P] | [x] |
| T982 | README: seção de notificações e mensageria (como ligar/desligar Kafka, como observar os tópicos); status do módulo | `README.md` | T980 | | [x] |
| T983 | Índice de specs: 009 → Implementada | `specs/README.md` | T980 | [P] | [x] |
| T984 | Relatório final e roteiro do vídeo (seção de mensageria e notificações) | `docs/` | T980 | [P] | [x] |

## Resumo

- **Total:** 48 tarefas (T901–T984), todas concluídas em 2026-09-25.
- **Paralelizáveis:** 24 marcadas `[P]`.
- **Caminho crítico:** T910 → T905/T912 → T914 → T921 → T922 → T932 → T943 → T960 → T961 → T980.
  As fases 7 (produtores) e 5 (apresentação) correm em paralelo com a 4 assim que a 3 termina.

## Definition of Done da feature

- [x] RF-01 a RF-08 cobertos por tarefa concluída e teste passando
- [x] Teste: reprocessamento do mesmo fato não duplica notificação (T935, T961)
- [x] Teste: usuário não lê nem marca notificação de terceiro (T937, T953)
- [x] Teste: fluxo clínico conclui com o canal de mensageria indisponível (T961 roda sem Kafka)
- [x] Teste: mensagem gerada não contém conteúdo clínico (T936)
- [x] Cobertura dos dez fatos de RF-08 (T960 + Fase 7)
- [x] Contrato OpenAPI publicado e Swagger conforme (T903, T951)
- [x] Build verde: `mvn verify -Pquality` (cobertura ≥ 80%, Modulith, ArchUnit)
- [x] `docker compose up` sobe Postgres, Kafka e API sem passo manual; tópicos visíveis
- [x] Spec, plano e catálogo de eventos descrevem o que o código faz
