# Tarefas — Histórico do paciente

- **Spec:** `./spec.md` · **Plano:** `./plan.md` · **Responsável:** Danilo
- **Convenção:** `[P]` = pode rodar em paralelo (arquivos distintos, sem dependência).
- **Ordem obrigatória:** contrato → portas nos módulos donos → domínio → aplicação →
  infraestrutura → apresentação → entrega.
- **Build:** `mvn verify -Pquality` com `JAVA_HOME` apontando para um JDK 21.

---

## Fase 0 — Preparação

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T1001 | Spec aprovada com as decisões da seção 12 | `spec.md` | — | | [x] |
| T1002 | Contrato OpenAPI | `contracts/openapi.yaml` | — | [P] | [x] |
| T1003 | Registrar no modelo de dados global que o histórico não tem tabela | `specs/000-plataforma-sus/data-model.md` | — | [P] | [x] |

## Fase 1 — Portas de leitura nos módulos donos

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T1010 | `consultas`: `ConsultaResumo` + `consultasDoPaciente`; query JPA; adapter | `consultas/api`, `consultas/infrastructure/persistence/repository` | — | [P] | [x] |
| T1011 | `exames`: `SolicitacaoExameResumo`, `ExameResumo` + `solicitacoesDoPaciente`, `examesDoPaciente`; queries JPA; adapter | `exames/api`, `exames/infrastructure/persistence/repository` | — | [P] | [x] |
| T1012 | `resultados`: `resultadosDoPaciente`; `listarPorPaciente` no repositório de domínio e no adapter | `resultados/api`, `resultados/domain/repository`, `resultados/application/service`, `resultados/infrastructure` | — | [P] | [x] |
| T1013 | `pareceres`: `ParecerResumo` + `pareceresDoPaciente`; `listarPorPaciente` | `pareceres/api`, `pareceres/domain/repository`, `pareceres/application/service`, `pareceres/infrastructure` | — | [P] | [x] |
| T1014 | `receitas`: pacote `api` novo (`package-info`, `ReceitaQuery`, `ReceitaResumo`); `ReceitaQueryService`; `listarPorPaciente` | `receitas/api`, `receitas/application/service`, `receitas/domain/repository`, `receitas/infrastructure` | — | [P] | [x] |
| T1015 | `documentos`: `DocumentoResumo` + `documentosDoPaciente`; `listarPorPaciente` | `documentos/api`, `documentos/domain/repository`, `documentos/application/service`, `documentos/infrastructure` | — | [P] | [x] |
| T1016 | **Testes** dos serviços de leitura (`resultados`, `pareceres`, `receitas`, `documentos`) com repositório mockado | `test/.../<modulo>/application/service` | T1012–T1015 | [P] | [x] |
| T1017 | **Testes** `@DataJpaTest` das queries novas de `consultas` e `exames` | `test/.../consultas/infrastructure`, `test/.../exames/infrastructure` | T1010, T1011 | [P] | [x] |

## Fase 2 — Domínio

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T1020 | `package-info.java` do módulo com `allowedDependencies` da seção 3 do plano | `historico/package-info.java` | — | [P] | [x] |
| T1021 | `TipoRegistro` com `recurso()` | `historico/domain/enums` | — | [P] | [x] |
| T1022 | `RegistroHistorico`, `Referencia`, `HistoricoFiltro` | `historico/domain/model` | T1021 | | [x] |
| T1023 | `LinhaDoTempo`: ordem (RN-02, RN-07), filtro, paginação | `historico/domain/model` | T1022 | | [x] |
| T1024 | Porta `FonteHistorico` | `historico/domain/port` | T1022 | [P] | [x] |
| T1025 | **Testes unitários** do domínio: obrigatórios, ordem e desempate, filtro por tipo e período, paginação, filtro inválido | `test/.../historico/domain` | T1023 | | [x] |

## Fase 3 — Aplicação

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T1030 | `AcessoHistoricoService`: paciente efetivo por perfil (seção 5 do plano) | `historico/application/service` | T1022 | | [x] |
| T1031 | `ConsultarHistoricoUseCase` com `@PreAuthorize`, junta as fontes, filtra e pagina | `historico/application/usecase` | T1023, T1024, T1030 | | [x] |
| T1032 | **Testes unitários**: HU-02, HU-03, EX-01, EX-02, EX-04; caso de uso combina fontes e aplica filtro | `test/.../historico/application` | T1031 | | [x] |

## Fase 4 — Infraestrutura (fontes)

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T1040 | `ConsultasFonte`, `ExamesFonte`, `ResultadosFonte`, `PareceresFonte`, `ReceitasFonte`, `DocumentosFonte` | `historico/infrastructure/fonte` | T1010–T1015, T1024 | [P] | [x] |
| T1041 | **Testes unitários** das fontes: data (RN-05), título, situação, origem; sem conteúdo clínico (RN-06) | `test/.../historico/infrastructure/fonte` | T1040 | | [x] |

## Fase 5 — Apresentação

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T1050 | `RegistroHistoricoResponse`, `ReferenciaResponse` | `historico/presentation/response` | T1022 | [P] | [x] |
| T1051 | `HistoricoController` com anotações OpenAPI | `historico/presentation/controller` | T1031, T1050 | | [x] |
| T1052 | **Teste de endpoint e autorização**: PACIENTE ignora `pacienteId`; MEDICO sem parâmetro → 400; ATENDENTE → 403; serialização com `recurso` e `origem`; `tamanho` > 100 → 400 | `test/.../historico/presentation` | T1051 | | [x] |
| T1053 | `ModularidadeTest` e `ArquiteturaTest` verdes com o módulo novo | build | T1051 | | [x] |

## Fase 6 — Entrega

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T1060 | Pasta **16. Historico do paciente** na coleção Postman (paciente, médico, admin, atendente 403) | `postman/SUS-Plataforma.postman_collection.json` | T1051 | [P] | [x] |
| T1061 | README: status do módulo e seção "Histórico do paciente" | `README.md` | T1051 | [P] | [x] |
| T1062 | Índice de specs e mapa de módulos do plano-mãe | `specs/README.md`, `specs/000-plataforma-sus/plan.md` | T1051 | [P] | [x] |
| T1063 | `mvn verify -Pquality` verde e validação no `docker compose` | build, ambiente | T1053 | | [x] |

## Resumo

- **Total:** 28 tarefas (T1001–T1063), todas concluídas.
- **Caminho crítico:** T1010–T1015 → T1023 → T1031 → T1040 → T1051 → T1063.

## Definition of Done da feature

- [x] HU-01 a HU-06 cobertas por teste automatizado
- [x] Nenhuma migration, tabela ou entidade JPA criada
- [x] Cada módulo clínico expõe a leitura por paciente apenas no seu pacote `api`
- [x] Contrato OpenAPI publicado e Swagger conforme
- [x] Build verde: `mvn verify -Pquality` (cobertura ≥ 80%, Modulith, ArchUnit)
- [x] Postman, README e índice de specs atualizados
