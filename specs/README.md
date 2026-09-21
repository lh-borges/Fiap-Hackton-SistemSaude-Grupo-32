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
| 007 | Receitas | `receitas` | Thiago | ✅ Implementada | [spec](007-receitas/spec.md) | — | — |'
| 008 | Documentos médicos | `documentos` | Juliana | ✅ Implementada | [spec](008-documentos/spec.md) | — | — |
| 009 | Notificações | `notificacoes` | Danilo | Em revisão | [spec](009-notificacoes/spec.md) | — | — |

## Documentos transversais

- [Constituição do projeto](../.specify/memory/constitution.md)
- [Modelo de dados global](000-plataforma-sus/data-model.md)
- [Catálogo de eventos](000-plataforma-sus/events.md)
- [Fluxo de trabalho SDD](../docs/fluxo-sdd.md)

## Ordem de execução

```
001 ✅ → 002 ✅ → 003 → 004 → 005 → 006
                          ↓      ↓
                         007    008
009 em paralelo, contra events.md
```

## O que já está de pé

`001` e `002` estão implementados e verificados: aplicação sobe, Flyway aplica as 5
migrations, autenticação JWT funciona, autorização por perfil e por posse do dado foi
testada em execução (401 sem token, 403 por perfil, 404 em dado de terceiro).

O módulo `cadastros` publica a porta `CadastroQuery`, que é por onde `003`, `004` e `005`
devem validar paciente, médico, unidade e tipo de exame — sem FK e sem acessar as tabelas
de outro módulo.

## Bloqueios para as próximas features

As pendências `[NEEDS CLARIFICATION]` da spec-mãe continuam abertas e precisam ser
resolvidas antes de `/plan 003`. Duas mudam regra de negócio em `003`, `004`, `005` e `009`:

1. Quem pode agendar consulta e exame: o paciente, ou apenas atendente e administrador?
2. O resultado fica visível ao paciente imediatamente, ou só depois do parecer médico?
