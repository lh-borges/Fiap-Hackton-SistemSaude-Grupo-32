# Tarefas — Usuários, autenticação e perfis

- **Spec:** `./spec.md` · **Plano:** `./plan.md` · **Status:** concluídas

## Fase 0 — Preparação

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T001 | Contrato OpenAPI | `contracts/iam.yaml` | — | [P] | [x] |
| T002 | Migration das tabelas | `V0001__iam_usuario_role.sql` | — | [P] | [x] |
| T003 | Migration de seed das roles | `V0002__iam_seed_roles.sql` | T002 | | [x] |

## Fase 1 — Domínio

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T010 | Enum `RoleNome` | `domain/enums` | — | [P] | [x] |
| T011 | VOs `Cpf` e `Email` | `shared/domain/vo` | — | [P] | [x] |
| T012 | Entidade `Role` | `domain/model` | T010 | | [x] |
| T013 | Entidade `Usuario` com invariantes | `domain/model` | T011, T012 | | [x] |
| T014 | Portas `UsuarioRepository` e `RoleRepository` | `domain/repository` | T013 | | [x] |
| T015 | Exceções de domínio | `domain/exception` | — | [P] | [x] |
| T016 | **Teste** das invariantes de `Usuario`, `Cpf` e `Email` | `test/.../domain` | T013 | | [x] |

## Fase 2 — Aplicação

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T020 | Portas `SenhaEncoderPort` e `TokenPort` | `application/port` | — | [P] | [x] |
| T021 | DTOs de entrada e saída | `application/dto` | T013 | [P] | [x] |
| T022 | `AutenticarUsuarioUseCase` | `application/usecase` | T014, T020 | | [x] |
| T023 | `CadastrarUsuarioUseCase` | `application/usecase` | T014, T021 | | [x] |
| T024 | Casos de uso de leitura (buscar, listar, logado, roles) | `application/usecase` | T014 | [P] | [x] |
| T025 | Casos de uso de alteração (atualizar, senha, roles, inativar) | `application/usecase` | T014 | | [x] |
| T026 | **Teste** de `AutenticarUsuarioUseCase` e `CadastrarUsuarioUseCase` | `test/.../application` | T022, T023 | | [x] |

## Fase 3 — Infraestrutura

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T030 | Entidades JPA `UsuarioEntity` e `RoleEntity` | `infrastructure/persistence/entity` | T002 | | [x] |
| T031 | Mappers domínio ↔ entidade | `infrastructure/persistence/mapper` | T030 | | [x] |
| T032 | Adapters dos repositórios | `infrastructure/persistence/repository` | T031, T014 | | [x] |
| T033 | `BCryptSenhaEncoderAdapter` e `JwtTokenAdapter` | `infrastructure/security` | T020 | | [x] |
| T034 | `SecurityConfig` e `JwtAuthenticationFilter` | `infrastructure/security` | T033 | | [x] |

## Fase 4 — Apresentação

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T040 | Requests e responses com Bean Validation | `presentation/request`, `response` | T021 | [P] | [x] |
| T041 | `AuthController` | `presentation/controller` | T022, T040 | | [x] |
| T042 | `UsuarioController` e `RoleController` | `presentation/controller` | T023–T025, T040 | | [x] |
| T043 | Anotações OpenAPI e esquema Bearer no Swagger | `shared/infrastructure/config` | T041 | | [x] |

## Fase 5 — Integração

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T050 | Porta pública `IamQuery` para outros módulos | `api/` | T032 | | [x] |
| T051 | **Teste** de arquitetura (ArchUnit) e de módulos (Modulith) | `test/.../arquitetura` | todas | | [x] |
| T052 | Seed do administrador de demonstração | `infrastructure/seed` | T032, T033 | | [x] |
| T053 | Coleção Postman da frente | `postman/` | T041, T042 | | [x] |

**Total:** 24 tarefas · paralelizáveis: 7 · caminho crítico: T002 → T013 → T014 → T022 → T034 → T041.
