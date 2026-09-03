# Plano Técnico — Cadastros: paciente, médico e apoio

- **Spec de origem:** `./spec.md`
- **Módulo:** `br.com.fiap.sus.cadastros`
- **Responsável:** Luis · **Status:** Implementado

---

## 1. Portão constitucional

| Artigo | Verificação | OK? |
|---|---|---|
| I — SDD | Spec escrita; pendências resolvidas na seção 10 | [x] |
| II — Clean Arch | `domain` sem framework; repositórios são portas | [x] |
| III — Modulith | Depende de `shared` e `iam::api`; sem FK para `iam_usuario` | [x] |
| IV — Segurança | Autorização por role e por posse; CPF e cartão SUS fora do log | [x] |
| V — Dados | `V0010`–`V0011`; PK UUID; inativação lógica | [x] |
| VI — Eventos | Nenhum evento no MVP | n/a |
| VII — Testes | Regras testadas sem Spring | [x] |
| VIII — API | `/api/v1`; RFC 7807; listagens paginadas | [x] |
| X — Simplicidade | CRUD direto, sem camada de serviço genérica | [x] |

## 2. Estrutura de pacotes

```
br.com.fiap.sus.cadastros
├── package-info.java        @ApplicationModule(allowedDependencies = {"shared", "iam::api"})
├── api/
│   ├── CadastroQuery.java   porta consumida por consultas, exames, resultados, etc.
│   └── dto/{PacienteResumo, MedicoResumo}
├── domain/
│   ├── model/{Paciente, Medico, Especialidade, UnidadeSaude, TipoExame}
│   ├── enums/{Sexo, CategoriaExame}
│   └── repository/  (uma porta por agregado)
├── application/
│   ├── dto/  ·  usecase/
├── infrastructure/persistence/{entity, repository, mapper}
└── presentation/{controller, request, response}
```

## 3. Modelo de domínio

| Objeto | Invariantes principais |
|---|---|
| `Paciente` | cartão SUS com 15 dígitos e único; data de nascimento não futura (RN-05); um por usuário (RN-01) |
| `Medico` | CRM + UF único (RN-03); especialidade ativa; um por usuário |
| `Especialidade` | nome único e obrigatório |
| `UnidadeSaude` | CNES com 7 dígitos e único |
| `TipoExame` | nome único; categoria `IMAGEM` ou `LABORATORIAL`; preparo opcional |
| `Sexo` | `MASCULINO`, `FEMININO`, `OUTRO`, `NAO_INFORMADO` |

## 4. Casos de uso

| Agregado | Casos de uso | Autorização |
|---|---|---|
| Paciente | Cadastrar, Atualizar, BuscarPorId, BuscarMeuCadastro, Listar, Inativar | Cadastrar/Atualizar/Inativar: ADMIN ou ATENDENTE · Buscar/Listar: ADMIN, ATENDENTE, MEDICO · `me`: PACIENTE |
| Médico | Cadastrar, Atualizar, BuscarPorId, BuscarMeuCadastro, Listar, Inativar | Escrita: ADMIN · Leitura: autenticado · `me`: MEDICO |
| Especialidade | Cadastrar, Listar, Inativar | Escrita: ADMIN · Leitura: autenticado |
| Unidade de saúde | Cadastrar, Listar, Inativar | Escrita: ADMIN · Leitura: autenticado |
| Tipo de exame | Cadastrar, Listar, Inativar | Escrita: ADMIN · Leitura: autenticado |

**Posse (RN da spec-mãe):** `BuscarPacientePorIdUseCase` recebe o usuário autenticado; se
ele é `PACIENTE` e o paciente consultado não é o seu, responde como inexistente (404).

**Validação cruzada com `iam`:** `CadastrarPacienteUseCase` e `CadastrarMedicoUseCase`
usam `IamQuery.usuarioAtivoPossuiRole(usuarioId, role)` para garantir RN-04. Nenhuma FK
física para `iam_usuario`.

## 5. Contrato REST

| Método | Rota | Auth |
|---|---|---|
| POST · GET | `/api/v1/pacientes` | ADMIN, ATENDENTE · leitura também MEDICO |
| GET | `/api/v1/pacientes/me` | PACIENTE |
| GET · PUT · DELETE | `/api/v1/pacientes/{id}` | conforme seção 4 |
| POST · GET | `/api/v1/medicos` | ADMIN · leitura autenticado |
| GET | `/api/v1/medicos/me` | MEDICO |
| GET · PUT · DELETE | `/api/v1/medicos/{id}` | conforme seção 4 |
| POST · GET | `/api/v1/especialidades` | ADMIN · leitura autenticado |
| DELETE | `/api/v1/especialidades/{id}` | ADMIN |
| POST · GET | `/api/v1/unidades-saude` | ADMIN · leitura autenticado |
| DELETE | `/api/v1/unidades-saude/{id}` | ADMIN |
| POST · GET | `/api/v1/tipos-exame` | ADMIN · leitura autenticado |
| DELETE | `/api/v1/tipos-exame/{id}` | ADMIN |

`DELETE` significa **inativação** (204), nunca exclusão física. Contrato completo em
`./contracts/cadastros.yaml`.

## 6. Persistência

- `V0010__cadastros_catalogos.sql` — `cad_especialidade`, `cad_unidade_saude`, `cad_tipo_exame`.
- `V0011__cadastros_paciente_medico.sql` — `cad_paciente`, `cad_medico`.
- `V0090__seed_demo_catalogos.sql` — carga de demonstração com UUID determinístico.

`cad_medico.especialidade_id` é FK real (mesmo módulo). `usuario_id` é referência lógica a
`iam`, com índice único e **sem** FK (Artigo III.3).

## 7. Integração entre módulos

- **Consome:** `iam::api → IamQuery` (existência do usuário e verificação de role).
- **Publica:** `CadastroQuery` com `pacienteAtivo`, `medicoAtivo`, `unidadeAtiva`,
  `tipoExameAtivo`, `categoriaDoTipoExame`, `pacienteIdDoUsuario`, `medicoIdDoUsuario`.
  É por essa porta que `consultas`, `exames` e `resultados` validarão referências.

## 8. Testes

| Nível | Alvo |
|---|---|
| Unitário | invariantes de `Paciente` (cartão SUS, data futura), `Medico` (CRM+UF), `TipoExame` |
| Unitário | `CadastrarPacienteUseCase` (usuário sem role PACIENTE, cartão duplicado, usuário já com cadastro) |
| Unitário | `BuscarPacientePorIdUseCase` (paciente lendo cadastro de terceiro responde inexistente) |

## 9. Decisões

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| `CadastroQuery` como porta única do módulo | Uma porta por agregado | Uma interface pequena é mais simples de manter e é o que os outros módulos precisam (Artigo X.1) |
| `categoria` em `TipoExame` | Deduzir a categoria do resultado no módulo `resultados` | Deixa a validação de coerência resultado × exame possível e barata |
| `DELETE` inativa | Exclusão física | RN-06/RN-07 e Artigo V.6 |

## 10. Pendências da spec resolvidas

| Pendência | Decisão |
|---|---|
| Paciente altera o próprio cartão SUS e data de nascimento? | **Não.** O paciente não tem endpoint de escrita no MVP; correção cadastral passa por atendente ou administrador |
| Categoria do tipo de exame restringe o tipo de resultado? | **Sim.** Confirmado; `resultados` validará por `CadastroQuery.categoriaDoTipoExame` |

## 11. Complexity Tracking

Nenhuma exceção.
