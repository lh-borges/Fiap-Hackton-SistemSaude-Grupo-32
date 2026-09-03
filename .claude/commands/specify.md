---
description: Cria ou atualiza a spec de negócio de uma feature (fase 1 do SDD)
argument-hint: <NNN-slug ou descrição da feature>
---

Você vai produzir a **especificação de negócio** da feature: `$ARGUMENTS`

Passos obrigatórios:

1. Leia `.specify/memory/constitution.md` e obedeça ao Artigo I.
2. Leia `.specify/templates/spec-template.md` — a estrutura é obrigatória.
3. Leia `specs/000-plataforma-sus/spec.md` para herdar contexto, atores e linguagem
   ubíqua. Não repita o que já está lá; referencie.
4. Se a feature já tem pasta em `specs/`, **atualize** o `spec.md` existente em vez de
   criar outro. Caso contrário crie `specs/<NNN-slug>/spec.md` usando o próximo número
   livre.
5. Escreva em **português**, em linguagem de negócio.

Regras de conteúdo:

- **Proibido** citar Java, Spring, JPA, nome de tabela, nome de classe ou rota HTTP.
- Toda decisão que você não conseguir derivar do relatório do projeto ou da spec-mãe
  vira `[NEEDS CLARIFICATION: pergunta objetiva]`. Não invente regra clínica.
- Todo requisito funcional precisa ser verificável: nada de "rápido", "amigável",
  "seguro" sem critério.
- Cada história de usuário tem critério de aceite em Gherkin.
- Preencha a matriz de atores com as quatro roles, mesmo que alguma fique "sem acesso".

Ao terminar, responda com: caminho do arquivo, resumo em até 5 linhas e a lista de
`[NEEDS CLARIFICATION]` que bloqueiam o `/plan`.
