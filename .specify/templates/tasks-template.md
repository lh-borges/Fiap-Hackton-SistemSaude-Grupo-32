# Tarefas — [NOME DA FEATURE]

- **Spec:** `./spec.md` · **Plano:** `./plan.md`
- **Convenção:** `[P]` = pode rodar em paralelo (arquivos distintos, sem dependência).
- **Ordem obrigatória:** contrato e migration → domínio → aplicação → infraestrutura →
  apresentação → testes de integração.

---

## Fase 0 — Preparação

| # | Tarefa | Arquivo/alvo | Dep. | P |
|---|---|---|---|---|
| T001 | Criar contrato OpenAPI do recurso | `contracts/[recurso].yaml` | — | [P] |
| T002 | Criar migration Flyway | `db/migration/V[NNNN]__...sql` | — | [P] |

## Fase 1 — Domínio (sem framework)

| # | Tarefa | Arquivo/alvo | Dep. | P |
|---|---|---|---|---|
| T010 | Enum `[...]` | `domain/enums/` | — | [P] |
| T011 | Entidade `[...]` com invariantes | `domain/model/` | T010 | |
| T012 | Porta de repositório `[...]Repository` | `domain/repository/` | T011 | |
| T013 | Evento de domínio `[...]Event` | `domain/event/` | T011 | [P] |
| T014 | **Teste unitário** das invariantes | `test/.../domain/` | T011 | |

## Fase 2 — Aplicação (casos de uso)

| # | Tarefa | Arquivo/alvo | Dep. | P |
|---|---|---|---|---|
| T020 | DTOs de entrada/saída | `application/dto/` | T011 | [P] |
| T021 | `[...]UseCase` + `@PreAuthorize` | `application/usecase/` | T012, T020 | |
| T022 | **Teste unitário** do caso de uso (Mockito) | `test/.../application/` | T021 | |

## Fase 3 — Infraestrutura

| # | Tarefa | Arquivo/alvo | Dep. | P |
|---|---|---|---|---|
| T030 | Entidade JPA `[...]Entity` | `infrastructure/persistence/entity/` | T002 | |
| T031 | Mapper domínio ↔ entidade | `infrastructure/persistence/mapper/` | T030 | |
| T032 | Adapter do repositório | `infrastructure/persistence/repository/` | T031, T012 | |
| T033 | **Teste de integração** do repositório | `test/.../persistence/` | T032 | |

## Fase 4 — Apresentação

| # | Tarefa | Arquivo/alvo | Dep. | P |
|---|---|---|---|---|
| T040 | Request/Response + Bean Validation | `presentation/request|response/` | T020 | [P] |
| T041 | Controller REST + anotações OpenAPI | `presentation/controller/` | T021, T040 | |
| T042 | **Teste de endpoint** (feliz + validação) | `test/.../controller/` | T041 | |
| T043 | **Teste de autorização** (role errada, dado de terceiro) | `test/.../security/` | T041 | |

## Fase 5 — Integração e eventos

| # | Tarefa | Arquivo/alvo | Dep. | P |
|---|---|---|---|---|
| T050 | Externalizar evento para Kafka | `infrastructure/messaging/` | T013 | |
| T051 | **Teste de módulo** (`ApplicationModules.verify()`) | `test/.../ModularityTest` | todas | |

## Definition of Done da feature

- [ ] Todos os RF da spec cobertos por tarefa concluída
- [ ] Build verde: compilação, testes, JaCoCo ≥ 80%, verificação Modulith
- [ ] Swagger exibindo o recurso conforme o contrato
- [ ] `docker compose up` continua subindo o ambiente sem passo manual
- [ ] Spec atualizada se o comportamento mudou durante a implementação
