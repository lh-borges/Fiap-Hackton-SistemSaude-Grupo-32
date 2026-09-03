---
description: Quebra o plano técnico em tarefas executáveis e ordenadas
argument-hint: <NNN-slug da feature>
---

Gere `specs/$ARGUMENTS/tasks.md` a partir de `specs/$ARGUMENTS/plan.md`.

Antes: leia a spec, o plano, `.specify/templates/tasks-template.md` e a constituição.

Regras:

- Ordem fixa por fases: contrato/migration → domínio → aplicação → infraestrutura →
  apresentação → integração/eventos.
- Cada tarefa é executável por uma pessoa em uma sessão e aponta **arquivo ou pacote
  alvo** concreto.
- Numere `T0xx` de forma contínua e declare dependências explícitas.
- Marque `[P]` apenas quando as tarefas tocam arquivos diferentes e não dependem entre si.
- Toda regra de negócio da spec tem uma tarefa de **teste** correspondente.
- Todo endpoint tem tarefa de teste de autorização negativa (role errada e paciente
  acessando dado de terceiro).
- Feche com a Definition of Done da feature.

Ao terminar, informe: total de tarefas, quantas são paralelizáveis e qual é o caminho
crítico.
