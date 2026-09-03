---
description: Gera o plano técnico (Clean Architecture + Spring Modulith) a partir da spec
argument-hint: <NNN-slug da feature>
---

Você vai produzir o **plano técnico** da feature `$ARGUMENTS`.

Pré-requisitos (verifique e pare se falhar):

1. `specs/$ARGUMENTS/spec.md` existe, está aprovada e **não tem** `[NEEDS CLARIFICATION]`
   pendente. Se tiver, liste as pendências e interrompa.
2. Leia `.specify/memory/constitution.md` e `specs/000-plataforma-sus/plan.md`
   (arquitetura de referência, stack e convenções globais).
3. Leia `.specify/templates/plan-template.md` — a estrutura é obrigatória.

Produza `specs/$ARGUMENTS/plan.md` contendo:

- O **portão constitucional** preenchido de verdade (marque o que está OK e explique o que
  não está).
- Estrutura de pacotes concreta do módulo, com nomes reais de classes.
- Tabela de casos de uso com entrada, saída, regra de autorização e evento publicado.
- Contrato REST completo, e o arquivo OpenAPI em `specs/$ARGUMENTS/contracts/*.yaml`.
- DDL da migration Flyway com número de sequência que não colida com as existentes
  (verifique os planos já escritos antes de escolher o número).
- Como o módulo conversa com os outros: **porta publicada** para leitura síncrona,
  **evento** para reação assíncrona. Nunca FK cruzando módulo.
- Estratégia de testes por nível.
- `Complexity Tracking` com qualquer exceção à constituição.

Não escreva código de produção nesta etapa. Ao terminar, aponte os pontos do plano que
merecem decisão humana antes do `/tasks`.
