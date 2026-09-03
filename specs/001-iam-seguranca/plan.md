# Plano Técnico — Usuários, autenticação e perfis

- **Spec de origem:** `./spec.md`
- **Módulo:** `br.com.fiap.sus.iam`
- **Responsável:** Luis · **Status:** Implementado

---

## 1. Portão constitucional

| Artigo | Verificação | OK? |
|---|---|---|
| I — SDD | Spec escrita; pendências resolvidas na seção 11 deste plano | [x] |
| II — Clean Arch | `domain` sem Spring/JPA; `UsuarioRepository` é porta | [x] |
| III — Modulith | `iam` exporta apenas `iam.api`; sem dependência de saída | [x] |
| IV — Segurança | JWT; `@PreAuthorize` no caso de uso; senha BCrypt; sem CPF em log | [x] |
| V — Dados | `V0001`–`V0002`; PK UUID; sem exclusão física | [x] |
| VI — Eventos | Nenhum evento no MVP | n/a |
| VII — Testes | Casos de uso testáveis sem Spring | [x] |
| VIII — API | `/api/v1`; RFC 7807; listagem paginada | [x] |
| X — Simplicidade | Sem abstração especulativa | [x] |

## 2. Estrutura de pacotes

```
br.com.fiap.sus.iam
├── package-info.java                  @ApplicationModule(allowedDependencies = "shared")
├── api/
│   ├── IamQuery.java                  porta de leitura para outros módulos
│   └── dto/UsuarioResumo.java
├── domain/
│   ├── model/{Usuario, Role}
│   ├── enums/RoleNome
│   ├── exception/{CredenciaisInvalidasException, UsuarioInativoException}
│   └── repository/{UsuarioRepository, RoleRepository}
├── application/
│   ├── dto/                           inputs e outputs de caso de uso
│   ├── port/{SenhaEncoderPort, TokenPort}
│   └── usecase/                       ver seção 4
├── infrastructure/
│   ├── persistence/{entity, repository, mapper}
│   ├── security/{SecurityConfig, JwtAuthenticationFilter, JwtTokenAdapter,
│   │              BCryptSenhaEncoderAdapter}
│   └── seed/AdministradorSeed
└── presentation/
    ├── controller/{AuthController, UsuarioController, RoleController}
    ├── request/  ·  response/
```

## 3. Modelo de domínio

| Objeto | Tipo | Atributos | Invariantes |
|---|---|---|---|
| `Usuario` | Entidade | id, nome, cpf, email, senhaHash, ativo, roles, criadoEm, atualizadoEm | nome obrigatório; ao menos uma role; `inativar()` não exclui |
| `Role` | Entidade | id, nome (`RoleNome`) | nome pertence ao enum |
| `RoleNome` | Enum | ADMINISTRADOR, ATENDENTE, MEDICO, PACIENTE | — |
| `Cpf` | VO (shared) | 11 dígitos | dígitos verificadores válidos (RN-02) |
| `Email` | VO (shared) | endereço | formato válido, normalizado em minúsculas |

RN-07 (administrador não remove o próprio perfil ADMINISTRADOR) é aplicada em
`AtribuirRolesUseCase`, comparando o id alvo com o id do autenticado.

## 4. Casos de uso

| Caso de uso | Entrada | Saída | Autorização | Regra principal |
|---|---|---|---|---|
| `AutenticarUsuarioUseCase` | email, senha | token, expiração, resumo | público | EX-01: mesma resposta para e-mail inexistente e senha errada; RN-05 nega inativo |
| `BuscarUsuarioLogadoUseCase` | — | `UsuarioOutput` | autenticado | id vem do contexto de segurança |
| `CadastrarUsuarioUseCase` | nome, cpf, email, senha, roles | `UsuarioOutput` | ADMINISTRADOR | RN-01 unicidade, RN-03 força da senha, RN-04 ao menos uma role |
| `ListarUsuariosUseCase` | filtro, paginação | página de `UsuarioOutput` | ADMINISTRADOR | busca por nome/e-mail/CPF, filtro por role e situação |
| `BuscarUsuarioUseCase` | id | `UsuarioOutput` | ADMINISTRADOR ou o próprio | id alheio para não-admin responde 404 |
| `AtualizarUsuarioUseCase` | id, nome, email | `UsuarioOutput` | ADMINISTRADOR ou o próprio | e-mail continua único |
| `AlterarSenhaUseCase` | id, senha atual, nova senha | — | o próprio; ADMINISTRADOR dispensa a senha atual | RN-03 |
| `AtribuirRolesUseCase` | id, roles | `UsuarioOutput` | ADMINISTRADOR | RN-04 e RN-07 |
| `InativarUsuarioUseCase` | id | — | ADMINISTRADOR | RN-05: inativa, não exclui; não pode inativar a si mesmo |
| `ListarRolesUseCase` | — | lista de roles | autenticado | catálogo fixo |

## 5. Contrato REST

| Método | Rota | Auth | Códigos |
|---|---|---|---|
| POST | `/api/v1/auth/login` | público | 200, 400, 401 |
| GET | `/api/v1/auth/me` | autenticado | 200, 401 |
| POST | `/api/v1/usuarios` | ADMINISTRADOR | 201, 400, 401, 403, 409 |
| GET | `/api/v1/usuarios` | ADMINISTRADOR | 200, 401, 403 |
| GET | `/api/v1/usuarios/{id}` | ADMIN ou próprio | 200, 401, 403, 404 |
| PUT | `/api/v1/usuarios/{id}` | ADMIN ou próprio | 200, 400, 404, 409 |
| PUT | `/api/v1/usuarios/{id}/senha` | próprio ou ADMIN | 204, 400, 401, 403, 404 |
| PUT | `/api/v1/usuarios/{id}/roles` | ADMINISTRADOR | 200, 400, 403, 404, 422 |
| DELETE | `/api/v1/usuarios/{id}` | ADMINISTRADOR | 204, 403, 404, 422 |
| GET | `/api/v1/roles` | autenticado | 200, 401 |

Contrato completo: `./contracts/iam.yaml`.

## 6. Persistência

- `V0001__iam_usuario_role.sql` — tabelas `iam_usuario`, `iam_role`, `iam_usuario_role`.
- `V0002__iam_seed_roles.sql` — carga das quatro roles com UUID determinístico.

| Tabela | Chaves e índices |
|---|---|
| `iam_usuario` | PK `id`; único `cpf`; único `email`; índice em `ativo` |
| `iam_role` | PK `id`; único `nome`; `CHECK` no conjunto de valores |
| `iam_usuario_role` | PK (`usuario_id`, `role_id`); FKs internas ao módulo |

## 7. Segurança

- JWT HS256. Claims: `sub` (id do usuário), `nome`, `roles`, `iat`, `exp`.
- **O token não carrega `pacienteId` nem `medicoId`.** Isso faria `iam` depender de
  `cadastros` e criaria dependência circular. O cliente obtém o vínculo em
  `GET /api/v1/pacientes/me` e `GET /api/v1/medicos/me`.
- `JwtAuthenticationFilter` valida o token e popula o `SecurityContext` com autoridades
  `ROLE_<NOME>`.
- Rotas públicas: `/api/v1/auth/login`, `/swagger-ui/**`, `/v3/api-docs/**`,
  `/actuator/health`. Todo o resto exige autenticação.
- `401` sem token ou token inválido; `403` com token válido e role insuficiente;
  `404` quando o recurso é de terceiro e a existência é informação sensível.

## 8. Testes

| Nível | Alvo |
|---|---|
| Unitário | `Cpf`, `Email`, `Usuario` (invariantes, inativação, troca de senha) |
| Unitário | `AutenticarUsuarioUseCase` (senha errada, usuário inativo, sucesso), `CadastrarUsuarioUseCase` (CPF duplicado, senha fraca, sem role) |
| Arquitetura | ArchUnit: `domain` sem Spring/JPA; `@Entity` só em `infrastructure.persistence.entity` |
| Módulo | `ApplicationModules.verify()` |

## 9. Decisões

| Decisão | Alternativa descartada | Motivo |
|---|---|---|
| Token sem vínculo paciente/médico | Enriquecer o token consultando `cadastros` | Criaria o ciclo `iam ↔ cadastros` (Artigo III) |
| `Cpf` e `Email` como VO em `shared` | Validar só por anotação no DTO | Regra de negócio não pode viver apenas na camada de entrada (Artigo VIII.3) |
| Roles em tabela, com UUID fixo no seed | Enum sem tabela | O ER do relatório prevê a tabela `ROLE`; UUID fixo simplifica seed e Postman |
| Senha alterada em endpoint próprio | `PUT /usuarios/{id}` aceitando senha | Evita troca de senha acidental em atualização de cadastro |

## 10. Complexity Tracking

Nenhuma exceção.

## 11. Pendências da spec resolvidas

| Pendência | Decisão |
|---|---|
| Validade da credencial | 8 horas (`JWT_EXPIRATION=28800000` ms), configurável por ambiente |
| Auto-cadastro de paciente | **Não** no MVP: todo usuário nasce por ADMINISTRADOR. Reduz superfície de ataque e mantém o fluxo demonstrável |
