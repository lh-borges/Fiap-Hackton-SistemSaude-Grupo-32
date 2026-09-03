---
description: Audita consistência entre constituição, spec, plano, tarefas e código
argument-hint: [NNN-slug da feature | all]
---

Faça uma auditoria de consistência do SDD para: `$ARGUMENTS` (use `all` para o repositório
inteiro).

Verifique e reporte, em tabela, cada divergência encontrada:

1. **Cobertura spec → plano:** todo RF e RN da spec tem tratamento no plano?
2. **Cobertura plano → tarefas:** todo caso de uso, endpoint e migration do plano tem
   tarefa?
3. **Cobertura tarefas → código:** tarefa marcada como concluída tem arquivo
   correspondente no repositório?
4. **Constituição:** procure violações concretas —
   - import de `org.springframework` ou `jakarta.persistence` em `domain/`
   - repositório JPA usado direto no controller
   - acesso a pacote interno de outro módulo
   - FK entre tabelas de módulos diferentes nas migrations
   - endpoint sem regra de autorização
   - endpoint sem teste de autorização negativa
   - migration já aplicada que foi editada
   - CPF, cartão SUS ou conteúdo clínico em log
5. **Numeração:** IDs de migration duplicados entre features; IDs de tarefa repetidos.
6. **Pendências:** `[NEEDS CLARIFICATION]` em spec cujo plano já foi escrito.

Para cada achado informe: severidade (bloqueante/atenção), artigo ou seção violada,
arquivo e linha, e a correção mínima. **Não corrija nada** — este comando só reporta.
