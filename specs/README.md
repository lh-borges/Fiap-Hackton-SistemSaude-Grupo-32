# Índice de especificações

| ID | Feature | Módulo | Responsável | Status | Spec | Plano | Tarefas |
|---|---|---|---|---|---|---|---|
| 000 | Plataforma (spec-mãe) | todos | Luis | Aprovada | [spec](000-plataforma-sus/spec.md) | [plan](000-plataforma-sus/plan.md) | — |
| 001 | Usuários, autenticação e perfis | `iam` | Luis | ✅ Implementada | [spec](001-iam-seguranca/spec.md) | [plan](001-iam-seguranca/plan.md) | [tasks](001-iam-seguranca/tasks.md) |
| 002 | Cadastros e catálogos | `cadastros` | Luis | ✅ Implementada | [spec](002-cadastros/spec.md) | [plan](002-cadastros/plan.md) | [tasks](002-cadastros/tasks.md) |
| 003 | Consultas | `consultas` | Thiago | ✅ Implementada | [spec](003-consultas/spec.md) | — | — |
| 004 | Solicitação e realização de exames | `exames` | Thiago | ✅ Implementada | [spec](004-exames/spec.md) | — | — |
| 005 | Resultados de exame | `resultados` | Thiago | ✅ Implementada | [spec](005-resultados/spec.md) | — | — |
| 006 | Parecer médico | `pareceres` | Juliana | ✅ Implementada | [spec](006-pareceres/spec.md) | — | — |
| 007 | Receitas | `receitas` | Thiago | ✅ Implementada | [spec](007-receitas/spec.md) | — | — |
| 008 | Documentos médicos | `documentos` | Juliana | ✅ Implementada | [spec](008-documentos/spec.md) | — | — |
| 009 | Notificações | `notificacoes` | Danilo | ✅ Implementada | [spec](009-notificacoes/spec.md) | [plan](009-notificacoes/plan.md) | [tasks](009-notificacoes/tasks.md) |
| 010 | Histórico do paciente | `historico` | Danilo | ✅ Implementada | [spec](010-historico/spec.md) | [plan](010-historico/plan.md) | [tasks](010-historico/tasks.md) |

## Documentos transversais

- [Constituição do projeto](../.specify/memory/constitution.md)
- [Modelo de dados global](000-plataforma-sus/data-model.md)
- [Catálogo de eventos](000-plataforma-sus/events.md)
- [Fluxo de trabalho SDD](../docs/fluxo-sdd.md)

## Ordem de execução

```
001 ✅ → 002 ✅ → 003 ✅ → 004 ✅ → 005 ✅ → 006 ✅
                                ↓        ↓
                               007 ✅   008 ✅
009 ✅ consome os eventos de 003 a 008 (events.md)
010 ✅ lê as portas `api` de 002 a 008 (linha do tempo derivada)
```

## O que já está de pé

As dez features estão implementadas e verificadas por `mvn verify -Pquality`: fronteiras de
módulo (Spring Modulith), regra da dependência (ArchUnit), teste de módulo com eventos e gate
de cobertura de 80%. A aplicação sobe no `docker compose` com Postgres e Kafka.

Cada módulo clínico publica uma porta de leitura no seu pacote `api` (`CadastroQuery`,
`ConsultaQuery`, `ExameQuery`, `ResultadoQuery`, `ParecerQuery`, `ReceitaQuery`,
`DocumentoQuery`). É por elas que os módulos se validam entre si e que `010` monta o histórico,
sem FK e sem acessar tabela alheia.

## Pendências

As marcações `[NEEDS CLARIFICATION]` das specs `000` a `008` continuam registradas nas
próprias specs. As features foram implementadas com as premissas assumidas em cada uma
(indicadas ao lado da pergunta); `009` e `010` não têm pendência.
