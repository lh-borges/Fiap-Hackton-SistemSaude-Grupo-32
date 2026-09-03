---
description: Implementa as tarefas de uma feature seguindo spec, plano e constituição
argument-hint: <NNN-slug da feature> [Txxx..Tyyy]
---

Implemente as tarefas de `specs/$ARGUMENTS`.

Antes de escrever qualquer código:

1. Leia `.specify/memory/constitution.md`, a `spec.md`, o `plan.md` e o `tasks.md` da
   feature.
2. Se um intervalo de tarefas foi informado, restrinja-se a ele. Senão, execute a próxima
   fase incompleta inteira.
3. Confirme que a fase anterior está concluída. Não pule fase.

Durante:

- Respeite a regra da dependência (Artigo II). `domain` sem import de framework.
- Respeite a fronteira de módulo (Artigo III). Nada de acessar internals de outro módulo,
  nada de FK cruzando módulo.
- Autorização no caso de uso, com verificação de posse do dado (Artigo IV).
- Escreva o teste junto com a implementação, não depois.
- Se o código exigir algo que a spec não prevê, **pare e atualize a spec primeiro**.

Depois:

- Rode `mvn verify`. Reporte o resultado real, incluindo falhas.
- Marque as tarefas concluídas em `tasks.md`.
- Liste qualquer divergência entre o implementado e o planejado.
