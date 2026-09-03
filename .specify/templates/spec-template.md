# Especificação — [NOME DA FEATURE]

- **ID:** `[NNN-slug]`
- **Módulo:** `[iam | cadastros | consultas | exames | resultados | pareceres | receitas | documentos | notificacoes]`
- **Responsável:** [nome]
- **Status:** Rascunho | Em revisão | Aprovada | Implementada
- **Criada em:** [AAAA-MM-DD]

> **Regra:** este documento descreve **o que** o sistema faz e **por quê**.
> É proibido citar classe, framework, tabela, endpoint ou biblioteca aqui — isso é `plan.md`.
> Escreva para quem entende do SUS, não para quem entende de Java.

---

## 1. Contexto e problema

[2-4 frases: qual dor do atendimento essa feature resolve e para quem.]

## 2. Objetivo

[1 frase mensurável. "Permitir que X faça Y para obter Z."]

## 3. Fora de escopo

- [O que explicitamente NÃO entra, para conter o escopo do MVP]

## 4. Atores e permissões

| Ator | O que pode fazer nesta feature |
|---|---|
| ADMINISTRADOR | |
| ATENDENTE | |
| MEDICO | |
| PACIENTE | |

## 5. Histórias de usuário

### HU-01 — [título]
**Como** [ator] **quero** [ação] **para que** [benefício].

**Critérios de aceite** (Gherkin):
```gherkin
Dado que [contexto/estado inicial]
Quando [ação]
Então [resultado observável]
```

### HU-02 — [título]
...

## 6. Requisitos funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | O sistema DEVE ... | Obrigatório |
| RF-02 | O sistema DEVE ... | Obrigatório |
| RF-03 | O sistema PODE ... | Desejável |

## 7. Regras de negócio

| ID | Regra |
|---|---|
| RN-01 | |
| RN-02 | |

## 8. Fluxos de exceção

| ID | Situação | Comportamento esperado |
|---|---|---|
| EX-01 | | |

## 9. Conceitos de domínio

[Entidades de negócio em linguagem ubíqua, com os atributos que o negócio enxerga.
Sem tipo de coluna, sem chave estrangeira.]

## 10. Eventos de negócio produzidos

| Evento | Quando ocorre | Quem se interessa |
|---|---|---|
| | | |

## 11. Requisitos não funcionais

- **Segurança:** [posse do dado, sigilo, o que não pode ser logado]
- **Desempenho:** [ex.: listagem paginada responde em < 500 ms]
- **Auditoria:** [o que precisa ser rastreável: autor, data]

## 12. Critérios de aceite da feature (Definition of Done)

- [ ] Todos os RF implementados e cobertos por teste
- [ ] Teste de autorização negativa por role e por posse do dado
- [ ] Contrato OpenAPI publicado
- [ ] Migration Flyway aplicada e validada
- [ ] Sem `[NEEDS CLARIFICATION]` pendente

## 13. Pendências

- [NEEDS CLARIFICATION: pergunta aberta que bloqueia o plano]

## 14. Dependências

- Depende de: `[NNN-slug]`
- É pré-requisito de: `[NNN-slug]`
