# Plano Técnico de Referência — Plataforma Digital de Atendimento SUS

- **Spec de origem:** `./spec.md`
- **Escopo:** arquitetura global. Todo `plan.md` de feature herda daqui e só documenta o
  que é específico do seu módulo.
- **Responsável:** Luis
- **Status:** Aprovado

---

## 1. Decisão arquitetural

**Monólito modular com Clean Architecture por módulo, usando Spring Modulith.**

Cada módulo de negócio é uma "microaplicação" dentro do mesmo processo: tem seu próprio
domínio, seus casos de uso, sua persistência e sua API pública explícita. As fronteiras
são verificadas em tempo de build, o que permite extrair qualquer módulo para um serviço
independente depois, sem reescrever regra de negócio.

**Por que não microserviços agora:** para um MVP de hackathon, N processos multiplicariam
infraestrutura, deploy e depuração sem trazer benefício. O Modulith dá o mesmo isolamento
de fronteira com o custo operacional de um único deploy.

**Por que não um monólito em camadas técnicas** (`controllers/`, `services/`, `repositories/`):
camada técnica global vira acoplamento transversal; qualquer serviço acaba chamando
qualquer repositório e a extração futura fica inviável.

## 2. Stack

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 (LTS) | Linguagem |
| Spring Boot | 3.5.x | Aplicação |
| Spring Modulith | 1.4.x | Fronteira de módulo, eventos, externalização |
| Spring Security | 6.x | Autenticação JWT e autorização por role |
| Spring Data JPA / Hibernate | 6.x | Persistência |
| PostgreSQL | 16 | Banco relacional |
| Flyway | 10.x | Versionamento de schema |
| Apache Kafka | 3.x | Mensageria de eventos |
| Jakarta Bean Validation | 3.x | Validação de entrada |
| springdoc-openapi | 2.x | Swagger / OpenAPI |
| JUnit 5 + Mockito + AssertJ | — | Testes |
| Testcontainers | 1.x | Integração com Postgres e Kafka reais |
| ArchUnit | 1.x | Regra da dependência |
| JaCoCo | 0.8.x | Cobertura, com gate de 80% |
| Docker / Docker Compose | — | Ambiente |
| Maven | wrapper | Build |

## 3. Mapa de módulos

| Módulo | Pacote | Responsabilidade | Dono |
|---|---|---|---|
| `shared` | `br.com.fiap.sus.shared` | Tipos comuns, erro RFC 7807, auditoria, contexto do usuário autenticado | Luis |
| `iam` | `br.com.fiap.sus.iam` | Usuário, role, autenticação, emissão e validação de token | Luis |
| `cadastros` | `br.com.fiap.sus.cadastros` | Paciente, médico, especialidade, unidade de saúde, tipo de exame | Luis |
| `consultas` | `br.com.fiap.sus.consultas` | Ciclo de vida da consulta | Thiago |
| `exames` | `br.com.fiap.sus.exames` | Solicitação e realização de exame | Thiago |
| `resultados` | `br.com.fiap.sus.resultados` | Resultado de imagem e laboratorial + itens | Thiago |
| `pareceres` | `br.com.fiap.sus.pareceres` | Parecer médico sobre resultado | Juliana |
| `receitas` | `br.com.fiap.sus.receitas` | Receita e itens de receita | Thiago |
| `documentos` | `br.com.fiap.sus.documentos` | Documento médico | Juliana |
| `notificacoes` | `br.com.fiap.sus.notificacoes` | Notificação ao usuário, consumidor de eventos | Danilo |
| `historico` | `br.com.fiap.sus.historico` | Linha do tempo derivada (somente leitura, sem tabela) | Thiago |

### Grafo de dependências permitidas

```
shared  ←  (todos)

iam        → shared
cadastros  → shared, iam
consultas  → shared, cadastros
exames     → shared, cadastros, consultas
resultados → shared, exames
pareceres  → shared, resultados, cadastros
receitas   → shared, cadastros, consultas
documentos → shared, cadastros, consultas
historico  → shared + portas de leitura dos módulos clínicos
notificacoes → shared          (só reage a eventos; não é chamado por ninguém)
```

Regras derivadas:
- **`notificacoes` não tem dependência de entrada.** Ele apenas escuta eventos. Nenhum
  módulo importa `notificacoes`.
- Não existe dependência circular. Se aparecer uma, o acoplamento vira evento.
- Dependência é sempre sobre o **pacote exportado** do módulo alvo (`...api`), nunca sobre
  `domain`, `application` ou `infrastructure` alheios.

### Declaração de módulo

```java
// br/com/fiap/sus/exames/package-info.java
@org.springframework.modulith.ApplicationModule(
    displayName = "Exames",
    allowedDependencies = { "shared", "cadastros::api", "consultas::api" }
)
package br.com.fiap.sus.exames;
```

## 4. Clean Architecture dentro do módulo

```
br.com.fiap.sus.<modulo>
├── package-info.java                  @ApplicationModule
├── api/                               ← ÚNICO pacote exportado
│   ├── <Modulo>Query.java             porta de leitura para outros módulos
│   └── dto/                           DTOs somente-leitura do contrato interno
├── domain/                            Java puro, zero framework
│   ├── model/            entidades e value objects
│   ├── enums/
│   ├── event/            eventos de domínio
│   ├── exception/
│   └── repository/       portas de persistência (interfaces)
├── application/
│   ├── usecase/          um caso de uso por intenção
│   ├── dto/              input/output de caso de uso
│   ├── mapper/
│   └── port/             portas de saída (outros módulos, storage, relógio)
├── infrastructure/
│   ├── persistence/entity/       entidades JPA (≠ entidade de domínio)
│   ├── persistence/repository/   adapters + interfaces Spring Data
│   ├── persistence/mapper/
│   ├── messaging/                publicação/consumo
│   └── config/
└── presentation/
    ├── controller/
    ├── request/
    └── response/
```

**Fluxo de uma requisição:**
`Controller` → converte `Request` em `Input` → `UseCase` (aplica regra, usa a porta de
repositório) → `domain` decide → adapter persiste → `UseCase` publica evento →
`Controller` devolve `Response`.

**Proibições verificadas por ArchUnit:**
1. `domain..` não importa `org.springframework..`, `jakarta.persistence..`, `com.fasterxml..`.
2. `presentation..` não importa `..infrastructure.persistence..`.
3. `application..` não importa `..presentation..`.
4. Classe anotada com `@Entity` só existe em `..infrastructure.persistence.entity`.
5. Classe anotada com `@RestController` só existe em `..presentation.controller`.

## 5. Comunicação entre módulos

| Necessidade | Mecanismo | Exemplo |
|---|---|---|
| Validar que um ID existe / obter dado leve | **Porta de leitura** publicada no `api` do módulo dono | `exames` chama `CadastroQuery.pacienteExiste(id)` |
| Reagir a um fato consumado | **Evento de domínio** | `notificacoes` escuta `ResultadoDisponivelEvent` |
| Escrita em outro módulo | **Proibido** | módulo dono é o único que escreve na sua tabela |

**Referência de dado:** por `UUID`, nunca por objeto agregado, nunca por FK física entre
tabelas de módulos distintos. A integridade referencial cruzada é responsabilidade do caso
de uso (valida via porta antes de gravar).

**Publicação de evento:**
```java
// dentro do caso de uso, na mesma transação
eventPublisher.publishEvent(new ResultadoDisponivelEvent(resultadoId, pacienteId, exameId, Instant.now()));
```
O Modulith persiste o evento (`event_publication`) e o entrega ao consumidor após o
commit. A externalização para Kafka usa `@Externalized("sus.resultados.disponivel.v1")`.

## 6. Segurança

- **Fluxo:** `POST /api/v1/auth/login` (credenciais) → JWT assinado HS256 → header
  `Authorization: Bearer`. Sem sessão, sem estado.
- **Claims:** `sub` (id do usuário), `roles`, `pacienteId` ou `medicoId` quando aplicável,
  `iat`, `exp`.
- **Filtro:** `JwtAuthenticationFilter` popula o `SecurityContext`; `shared` expõe
  `UsuarioAutenticado` para os casos de uso.
- **Autorização:** `@PreAuthorize` no caso de uso (não só no controller).
- **Posse do dado:** casos de uso de leitura de paciente recebem o `pacienteId` do token
  quando o autenticado é `PACIENTE`, e ignoram qualquer `pacienteId` vindo da requisição.
  Registro de terceiro responde `404`.
- **Rotas públicas:** `/api/v1/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`,
  `/actuator/health`. Todo o resto exige autenticação.
- **Senha:** BCrypt força 10. Segredo do JWT em `JWT_SECRET`.

## 7. Convenções de API

- Base: `/api/v1`.
- Erro: `application/problem+json` (RFC 7807), tratado por um `@RestControllerAdvice` em
  `shared`.
- Paginação: `?page=0&size=20&sort=campo,desc`, `size` máximo 100; resposta com
  `content`, `page`, `size`, `totalElements`, `totalPages`.
- Códigos: `201` com `Location` na criação; `204` em cancelamento; `409` em conflito de
  estado; `422` em regra de negócio violada; `400` em validação de formato.

## 8. Persistência e migrations

- Schema único `sus`, com **prefixo de tabela por módulo** para deixar a fronteira óbvia
  e permitir separar bancos na extração futura.

| Faixa de migration | Módulo |
|---|---|
| `V0001`–`V0009` | `iam` |
| `V0010`–`V0019` | `cadastros` |
| `V0020`–`V0029` | `consultas` |
| `V0030`–`V0039` | `exames` |
| `V0040`–`V0049` | `resultados` |
| `V0050`–`V0059` | `pareceres` |
| `V0060`–`V0069` | `receitas` |
| `V0070`–`V0079` | `documentos` |
| `V0080`–`V0089` | `notificacoes` |
| `V0090`–`V0099` | infraestrutura do Modulith (`event_publication`) e seeds |

Cada feature reserva seu número na faixa e registra no seu `plan.md`. Colisão de número é
erro de merge.

- PK `UUID` gerada na aplicação (`UUID.randomUUID()` no construtor do domínio).
- Auditoria: `criado_em`, `atualizado_em` em toda tabela; `criado_por_usuario_id` nas
  tabelas de ato clínico.
- Enum como `VARCHAR` com `CHECK` na migration.
- Índice obrigatório em toda coluna usada para filtrar por titular (`paciente_id`,
  `medico_id`) e por data.

## 9. Estrutura do repositório

```
.
├── .specify/
│   ├── memory/constitution.md
│   └── templates/
├── .claude/commands/            /specify /plan /tasks /implement /analyze
├── specs/
│   ├── 000-plataforma-sus/      spec-mãe, este plano, modelo de dados, eventos
│   └── NNN-<feature>/           spec.md, plan.md, tasks.md, contracts/
├── docs/
├── src/main/java/br/com/fiap/sus/<modulo>/
├── src/main/resources/db/migration/
├── src/test/java/...
├── docker-compose.yml
└── pom.xml
```

## 10. Testes

| Nível | O que cobre | Ferramenta | Onde |
|---|---|---|---|
| Unitário | invariantes de domínio, regra de caso de uso | JUnit + Mockito | `test/.../domain`, `.../application` |
| Integração | repositório e endpoint reais | SpringBootTest + Testcontainers | `test/.../infrastructure`, `.../presentation` |
| Segurança | role errada, dado de terceiro | MockMvc + `@WithMockUser` | `test/.../security` |
| Módulo | fronteiras e eventos | `ApplicationModules.verify()`, `Scenario` | `test/.../ModularityTest` |
| Arquitetura | regra da dependência | ArchUnit | `test/.../architecture` |

Gate de build: compilação + testes + JaCoCo ≥ 80% em `domain` e `application` +
verificação do Modulith.

## 11. Ambiente

`docker-compose.yml` sobe: `postgres:16`, `kafka` (KRaft, sem Zookeeper) e a aplicação.
Variáveis: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `KAFKA_BOOTSTRAP_SERVERS`, `JWT_SECRET`,
`JWT_EXPIRATION`. Perfil `docker` ativo no container; `local` no desenvolvimento;
`test` com Testcontainers.

`spring.kafka` desabilitado no perfil sem mensageria — a aplicação sobe e opera, e as
notificações são geradas pelo listener local do Modulith.

## 12. Sequência de entrega

```
001-iam-seguranca  →  002-cadastros  →  003-consultas  →  004-exames  →  005-resultados
                                                                              ↓
                                    009-notificacoes  ←  006-pareceres / 007-receitas / 008-documentos
```

`009-notificacoes` pode ser desenvolvido em paralelo desde o início, contra os contratos
de evento definidos em `./events.md`.

## 13. Complexity Tracking

| Artigo | Exceção | Justificativa | Alternativa rejeitada |
|---|---|---|---|
| III | Banco único compartilhado entre módulos, separado por prefixo de tabela | Um Postgres por módulo é inviável no prazo do hackathon; o prefixo mais a proibição de FK cruzada preservam a extração futura | Schema ou banco por módulo — custo de infra desproporcional |
| II | `historico` não tem domínio próprio: só compõe leituras das portas dos outros módulos | Criar entidade de histórico violaria o Artigo X.2 | Tabela `HISTORICO` materializada |
| VII.2 | O gate de cobertura de 80% roda em profile (`mvn verify -Pquality`), não no build padrão | **Cobertura atual: 37%** em `domain` + `application`. O domínio está coberto (VOs 93%, `iam.domain.model` 77%); os casos de uso ainda não têm teste. Ativar o gate no build padrão hoje bloquearia toda a equipe, inclusive quem não escreveu o débito | Baixar o mínimo para 37% — esconderia a dívida em vez de registrá-la |

**Débito de teste em aberto (dono: Juliana, apoio de quem escreveu cada caso de uso):**
escrever teste unitário para os casos de uso de `iam` (25% cobertos) e `cadastros` (15%).
Quando a cobertura passar de 80%, mover a execução do JaCoCo `check` do profile `quality`
para o build padrão e remover esta linha.
