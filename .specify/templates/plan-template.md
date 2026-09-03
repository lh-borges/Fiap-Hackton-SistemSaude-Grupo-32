# Plano Técnico — [NOME DA FEATURE]

- **Spec de origem:** `./spec.md`
- **Módulo Spring Modulith:** `br.com.fiap.sus.[modulo]`
- **Responsável:** [nome]
- **Status:** Rascunho | Aprovado | Implementado

---

## 1. Portão constitucional (preencher ANTES de detalhar)

| Artigo | Verificação | OK? |
|---|---|---|
| I — SDD | `spec.md` aprovada e sem `[NEEDS CLARIFICATION]` | [ ] |
| II — Clean Arch | `domain` sem dependência de framework; repositório é porta | [ ] |
| III — Modulith | Sem acesso a internals de outro módulo; sem FK cruzando módulo | [ ] |
| IV — Segurança | Regra de role e de posse definida por caso de uso | [ ] |
| V — Dados | Migration Flyway nomeada; PK UUID; enum como texto | [ ] |
| VI — Eventos | Evento publicado no caso de uso; payload sem dado clínico | [ ] |
| VII — Testes | Casos de uso testáveis sem Spring; meta de 80% | [ ] |
| VIII — API | Rotas `/api/v1`; erro RFC 7807; listagem paginada | [ ] |
| X — Simplicidade | Nenhuma abstração sem segundo implementador | [ ] |

## 2. Estrutura de pacotes

```
br.com.fiap.sus.[modulo]
├── package-info.java              @ApplicationModule(allowedDependencies = {...})
├── domain/
│   ├── model/                     entidades e VOs (Java puro)
│   ├── enums/
│   ├── event/                     eventos de domínio
│   ├── exception/
│   └── repository/                portas (interfaces)
├── application/
│   ├── usecase/                   um caso de uso por intenção
│   ├── dto/
│   ├── mapper/
│   └── port/                      portas de saída (outros módulos, arquivos, etc.)
├── infrastructure/
│   ├── persistence/entity/
│   ├── persistence/repository/    adapters JPA
│   ├── persistence/mapper/
│   ├── config/
│   └── messaging/
└── presentation/
    ├── controller/
    ├── request/
    └── response/
```

## 3. Modelo de domínio

| Objeto | Tipo | Atributos | Invariantes |
|---|---|---|---|
| | Entidade / VO / Enum | | |

## 4. Casos de uso

| Caso de uso | Entrada | Saída | Autorização | Evento publicado |
|---|---|---|---|---|
| `XxxUseCase` | `XxxInput` | `XxxOutput` | `hasRole('MEDICO')` | `XxxEvent` |

## 5. Contrato REST

| Método | Rota | Auth | Request | Response | Códigos |
|---|---|---|---|---|---|
| POST | `/api/v1/...` | MEDICO | `XxxRequest` | `XxxResponse` | 201, 400, 401, 403, 404, 409 |

Contrato completo em `./contracts/[recurso].yaml`.

## 6. Persistência

### Tabelas

```sql
-- V[NNNN]__[modulo]_[descricao].sql
```

| Tabela | Colunas-chave | Índices | Observações |
|---|---|---|---|

### Referências entre módulos

| Campo | Módulo dono | Como é validado (porta/evento) |
|---|---|---|

## 7. Integração entre módulos

- **Depende de (leitura):** [módulo → porta usada]
- **Publica:** [eventos]
- **Consome:** [eventos]

## 8. Estratégia de testes

| Nível | Alvo | Ferramenta |
|---|---|---|
| Unitário | regras de domínio e casos de uso | JUnit 5 + Mockito |
| Integração | repositório e endpoint | SpringBootTest + Testcontainers |
| Segurança | role errada, dado de terceiro | MockMvc + `@WithMockUser` |
| Arquitetura | fronteiras de módulo | `ApplicationModules.verify()` / ArchUnit |

## 9. Riscos e decisões

| Decisão | Alternativa descartada | Motivo |
|---|---|---|

## 10. Complexity Tracking (exceções à constituição)

| Artigo violado | Por quê é necessário | Alternativa mais simples rejeitada e o motivo |
|---|---|---|
| — | — | — |
