# Tarefas — Cadastros: paciente, médico e apoio

- **Spec:** `./spec.md` · **Plano:** `./plan.md` · **Status:** concluídas

## Fase 0 — Preparação

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T101 | Contrato OpenAPI | `contracts/cadastros.yaml` | — | [P] | [x] |
| T102 | Migration dos catálogos | `V0010__cadastros_catalogos.sql` | — | [P] | [x] |
| T103 | Migration de paciente e médico | `V0011__cadastros_paciente_medico.sql` | T102 | | [x] |
| T104 | Seed de demonstração dos catálogos | `V0090__seed_demo_catalogos.sql` | T102 | | [x] |

## Fase 1 — Domínio

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T110 | Enums `Sexo` e `CategoriaExame` | `domain/enums` | — | [P] | [x] |
| T111 | `Especialidade`, `UnidadeSaude`, `TipoExame` | `domain/model` | T110 | [P] | [x] |
| T112 | `Paciente` com invariantes (cartão SUS, nascimento) | `domain/model` | — | [P] | [x] |
| T113 | `Medico` com invariantes (CRM + UF) | `domain/model` | T111 | | [x] |
| T114 | Portas de repositório dos cinco agregados | `domain/repository` | T111–T113 | | [x] |
| T115 | **Teste** das invariantes de `Paciente`, `Medico` e `TipoExame` | `test/.../domain` | T112, T113 | | [x] |

## Fase 2 — Aplicação

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T120 | DTOs de entrada e saída | `application/dto` | T111–T113 | [P] | [x] |
| T121 | Casos de uso de paciente (6) | `application/usecase` | T114, T120 | | [x] |
| T122 | Casos de uso de médico (6) | `application/usecase` | T114, T120 | | [x] |
| T123 | Casos de uso dos catálogos (3 por agregado) | `application/usecase` | T114, T120 | [P] | [x] |
| T124 | **Teste** de `CadastrarPacienteUseCase` e da regra de posse em `BuscarPacientePorId` | `test/.../application` | T121 | | [x] |

## Fase 3 — Infraestrutura

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T130 | Entidades JPA dos cinco agregados | `infrastructure/persistence/entity` | T102, T103 | | [x] |
| T131 | Mappers domínio ↔ entidade | `infrastructure/persistence/mapper` | T130 | | [x] |
| T132 | Adapters dos repositórios | `infrastructure/persistence/repository` | T131, T114 | | [x] |

## Fase 4 — Apresentação

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T140 | Requests e responses com Bean Validation | `presentation/request`, `response` | T120 | [P] | [x] |
| T141 | `PacienteController` e `MedicoController` | `presentation/controller` | T121, T122, T140 | | [x] |
| T142 | Controllers dos catálogos | `presentation/controller` | T123, T140 | [P] | [x] |
| T143 | Anotações OpenAPI nos controllers | `presentation/controller` | T141, T142 | | [x] |

## Fase 5 — Integração

| # | Tarefa | Alvo | Dep. | P | Feito |
|---|---|---|---|---|---|
| T150 | Porta pública `CadastroQuery` | `api/` | T132 | | [x] |
| T151 | Consumo de `IamQuery` na validação de role do usuário | `application/usecase` | T121, T122 | | [x] |
| T152 | Coleção Postman dos cadastros | `postman/` | T141, T142 | | [x] |

**Total:** 22 tarefas · paralelizáveis: 8 · caminho crítico: T103 → T113 → T114 → T122 → T141.
