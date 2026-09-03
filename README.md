# Plataforma Digital de Atendimento SUS

Hackathon FIAP — Grupo 32 · Luis · Thiago · Juliana · Gilmar · Danilo

API que centraliza o ciclo de atendimento do paciente do SUS: agendamento de consultas,
solicitação e realização de exames, resultados, parecer médico, receitas, documentos e
notificações — com acesso controlado por perfil.

**Princípio central:** o dado técnico do exame é separado da interpretação clínica do
médico. O resultado é persistido de forma independente do parecer, preservando autoria,
histórico e rastreabilidade.

## Estado atual

| Módulo | Feature | Status |
|---|---|---|
| `shared` | tipos comuns, erro RFC 7807, contexto de segurança | ✅ implementado |
| `iam` | [001 — usuários, autenticação e perfis](specs/001-iam-seguranca/spec.md) | ✅ implementado |
| `cadastros` | [002 — paciente, médico e catálogos](specs/002-cadastros/spec.md) | ✅ implementado |
| `consultas` | [003](specs/003-consultas/spec.md) | 📄 especificado (Thiago) |
| `exames` · `resultados` | [004](specs/004-exames/spec.md) · [005](specs/005-resultados/spec.md) | 📄 especificado (Thiago) |
| `pareceres` · `documentos` | [006](specs/006-pareceres/spec.md) · [008](specs/008-documentos/spec.md) | 📄 especificado (Juliana) |
| `receitas` | [007](specs/007-receitas/spec.md) | 📄 especificado (Thiago) |
| `notificacoes` | [009](specs/009-notificacoes/spec.md) | 📄 especificado (Danilo) |

Build: **68 testes passando** (`mvn verify`), incluindo verificação de fronteiras do Spring
Modulith e da regra da dependência com ArchUnit.

## Como rodar

### Com Docker (recomendado)

```bash
cp .env.example .env      # opcional: ajuste segredo e credenciais
docker compose up --build # sobe Postgres, Kafka e a API
```

Se já houver um Postgres na 5432 na sua máquina, use outra porta:
`DB_PORT=5433 docker compose up --build` (para rodar a API fora do Compose nesse caso,
aponte `DB_URL=jdbc:postgresql://localhost:5433/sus`).

### Local (precisa de um Postgres na porta 5432)

```bash
mvn spring-boot:run
```

Na primeira subida o Flyway cria o schema, carrega os quatro perfis e os catálogos de
demonstração, e a aplicação cria o administrador inicial:

```
admin@sus.gov.br  /  Admin@123
```

Troque essa senha antes de qualquer uso real; desligue o seed com `SEED_ENABLED=false`.

## Documentação da API

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

Autentique em `POST /api/v1/auth/login`, copie o campo `token` e cole no botão
**Authorize** do Swagger (o prefixo `Bearer` é adicionado automaticamente).

## Postman

Em [`postman/`](postman/):

- `SUS-Plataforma.postman_collection.json` — coleção completa
- `SUS-Local.postman_environment.json` — environment apontando para `localhost:8080`

Importe os dois no Postman, selecione o environment **SUS - Local** e rode
**1. Autenticação › Login (administrador)**: o token é salvo automaticamente e usado pelas
demais requisições. Rode as pastas na ordem numérica — cada uma guarda os ids que a
seguinte utiliza.

A pasta **8. Testes de segurança** é executável e demonstra o controle de acesso:
`401` sem token, `403` por perfil insuficiente e `404` quando um paciente tenta ler o
cadastro de outro.

## Arquitetura

Monólito modular com **Spring Modulith**, onde cada módulo de negócio é uma
"microaplicação" com **Clean Architecture** própria, fronteira verificada em tempo de build
e comunicação por porta publicada (leitura síncrona) ou evento de domínio (reação
assíncrona via Kafka).

```
presentation → application → domain ← infrastructure
```

O `domain` é Java puro — sem Spring, sem JPA. Entidade de domínio não é entidade JPA.
Módulos se referenciam por `UUID` validado através da porta pública do módulo dono; não há
FK entre tabelas de módulos diferentes, o que mantém cada módulo extraível.

Stack: Java 21 · Spring Boot 3.5 · Spring Security (JWT) · Spring Data JPA · PostgreSQL 16
· Flyway · Apache Kafka · Docker Compose · JUnit 5 + Mockito + ArchUnit · springdoc-openapi.

## Como navegar neste repositório

| Onde | O que é |
|---|---|
| [`.specify/memory/constitution.md`](.specify/memory/constitution.md) | Regras não negociáveis do projeto |
| [`specs/README.md`](specs/README.md) | Índice de todas as especificações |
| [`specs/000-plataforma-sus/`](specs/000-plataforma-sus/) | Spec do produto, plano de arquitetura, modelo de dados, eventos |
| [`specs/NNN-*/`](specs/) | Uma pasta por feature: `spec.md`, `plan.md`, `tasks.md` |
| [`src/main/java/br/com/fiap/sus/`](src/main/java/br/com/fiap/sus/) | Um pacote por módulo |
| [`src/main/resources/db/migration/`](src/main/resources/db/migration/) | Migrations Flyway, faixa numérica reservada por módulo |
| [`postman/`](postman/) | Coleção e environment |
| [`docs/fluxo-sdd.md`](docs/fluxo-sdd.md) | Como a equipe trabalha no dia a dia |
| [`CLAUDE.md`](CLAUDE.md) | Guia de contexto para quem (ou o que) chega agora |

## Desenvolvimento

```bash
mvn verify              # compila, roda os 68 testes, valida fronteiras de módulo
mvn verify -Pquality    # acrescenta o gate de cobertura de 80% (hoje em 37%, ver abaixo)
```

Fluxo de trabalho (Spec-Driven Development):

```
/specify <feature>  →  /plan <NNN>  →  /tasks <NNN>  →  /implement <NNN>  →  /analyze <NNN>
```

Nenhum código de produção antes da spec aprovada. Detalhes em
[`docs/fluxo-sdd.md`](docs/fluxo-sdd.md).

## Dívida conhecida

- **Cobertura de testes em 37%**, abaixo dos 80% exigidos pelo Artigo VII da constituição.
  O domínio está coberto; faltam testes para os casos de uso. Por isso o gate roda em
  profile (`-Pquality`) e não no build padrão. Registrado em
  [`specs/000-plataforma-sus/plan.md`](specs/000-plataforma-sus/plan.md#13-complexity-tracking).
- Kafka sobe no Compose mas ainda não é usado: entra com a feature `009-notificacoes`.
- Pendências `[NEEDS CLARIFICATION]` das features `003` a `009` continuam abertas.
