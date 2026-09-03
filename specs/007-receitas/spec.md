# Especificação — Receitas médicas

- **ID:** `007-receitas` · **Módulo:** `receitas` · **Responsável:** Thiago
- **Status:** Em revisão · **Criada em:** 2026-09-03
- **Herda de:** `specs/000-plataforma-sus/spec.md`

## 1. Contexto e problema

Receita de papel se perde, vence sem aviso e obriga o paciente a voltar à unidade só para
renovar uma medicação contínua. Digitalizar a prescrição e permitir a renovação rastreável
elimina deslocamento e preserva o histórico terapêutico.

## 2. Objetivo

Permitir que o médico emita e renove receitas com um ou mais medicamentos, e que o
paciente consulte suas receitas válidas e o histórico das anteriores.

## 3. Fora de escopo

- Assinatura digital e validade legal do documento.
- Integração com farmácia e controle de dispensação.
- Receituário de medicamento controlado (receita azul/amarela) e suas regras específicas.
- Base de medicamentos e checagem de interação medicamentosa.

## 4. Atores e permissões

| Ator | O que pode fazer |
|---|---|
| ADMINISTRADOR | Consultar receitas para fins administrativos |
| ATENDENTE | Nenhum acesso ao conteúdo da receita |
| MEDICO | Emitir, renovar e cancelar as próprias receitas; consultar as dos pacientes que atende |
| PACIENTE | Consultar as próprias receitas e itens |

## 5. Histórias de usuário

### HU-01 — Emitir receita
```gherkin
Dado que sou médico autenticado
E existe um paciente ativo
Quando emito uma receita com pelo menos um medicamento, dosagem, frequência e duração
Então a receita é criada como ativa, com data de emissão e validade
E o paciente é notificado

Quando tento emitir uma receita sem nenhum item
Então a operação é rejeitada
```

### HU-02 — Renovar
```gherkin
Dado que existe uma receita anterior emitida para o paciente
Quando o médico solicita a renovação
Então é criada uma nova receita com os mesmos itens, nova data de emissão e nova validade
E a nova receita aponta para a receita de origem
E a receita anterior é preservada e marcada como renovada
E o paciente é notificado
```

### HU-03 — Validade
```gherkin
Dado que existe uma receita cuja validade já passou
Quando o paciente a consulta
Então ela aparece como vencida
E não pode ser renovada por cópia sem nova avaliação médica
```

### HU-04 — Cancelar
```gherkin
Dado que sou o médico que emitiu a receita
Quando a cancelo informando o motivo
Então ela passa a cancelada e deixa de valer
E permanece no histórico do paciente
```

### HU-05 — Consulta pelo paciente
```gherkin
Dado que sou paciente autenticado
Quando consulto minhas receitas
Então vejo os medicamentos, dosagem, frequência, duração, orientação, validade e situação
E consigo separar as ativas das vencidas e canceladas
Quando tento consultar receita de outro paciente
Então o registro é tratado como inexistente
```

## 6. Requisitos funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | O sistema DEVE permitir ao médico emitir receita com um ou mais itens | Obrigatório |
| RF-02 | O sistema DEVE registrar, por item, medicamento, dosagem, frequência, duração e orientação | Obrigatório |
| RF-03 | O sistema DEVE permitir renovar uma receita, criando uma nova e preservando a original | Obrigatório |
| RF-04 | O sistema DEVE controlar a validade e apresentar a situação da receita | Obrigatório |
| RF-05 | O sistema DEVE permitir ao médico autor cancelar uma receita, com motivo | Obrigatório |
| RF-06 | O sistema DEVE listar receitas por paciente, por médico e por situação | Obrigatório |
| RF-07 | O sistema DEVE restringir a leitura do paciente às próprias receitas | Obrigatório |
| RF-08 | O sistema DEVE notificar o paciente na emissão e na renovação | Obrigatório |
| RF-09 | O sistema DEVE permitir vincular a receita a uma consulta | Desejável |

## 7. Regras de negócio

| ID | Regra |
|---|---|
| RN-01 | Somente médico emite, renova e cancela receita |
| RN-02 | Receita tem no mínimo um item |
| RN-03 | Situações: ativa, vencida, cancelada, renovada |
| RN-04 | Validade é posterior à data de emissão |
| RN-05 | Receita vencida ou cancelada não é renovada por cópia; exige nova emissão |
| RN-06 | Somente o médico autor cancela a receita |
| RN-07 | Receita emitida não tem seus itens alterados; correção exige nova receita |
| RN-08 | Se vinculada a uma consulta, a consulta é do mesmo paciente |
| RN-09 | Receita não é excluída fisicamente |

## 8. Fluxos de exceção

| ID | Situação | Comportamento |
|---|---|---|
| EX-01 | Não-médico tenta emitir ou renovar | Negar |
| EX-02 | Receita sem itens | Rejeitar por validação |
| EX-03 | Validade anterior ou igual à emissão | Rejeitar |
| EX-04 | Renovação de receita cancelada | Rejeitar por conflito |
| EX-05 | Cancelamento por médico que não é o autor | Negar |
| EX-06 | Paciente acessa receita de terceiro | Tratar como inexistente |

## 9. Conceitos de domínio

- **Receita:** prescrição emitida por um médico para um paciente. Consulta de origem
  (opcional), data de emissão, validade, observação, situação, receita de origem quando
  for renovação.
- **Item de receita:** medicamento, dosagem, frequência, duração, orientação.

## 10. Eventos produzidos

| Evento | Quando ocorre | Quem se interessa |
|---|---|---|
| Receita emitida | Nova receita criada | Paciente |
| Receita renovada | Renovação criada | Paciente |

## 11. Requisitos não funcionais

- Medicamento, dosagem e orientação são dado clínico: nunca em log nem em payload de evento.
- A situação "vencida" é derivada da data no momento da leitura, não depende de rotina
  agendada para ser correta.
- Listagem paginada, ordenada por data de emissão decrescente.

## 12. Definition of Done

- [ ] RF-01 a RF-08 implementados e testados (RF-09 se couber no prazo)
- [ ] Teste: receita sem item é rejeitada
- [ ] Teste: renovação preserva a receita original e cria vínculo entre elas
- [ ] Teste: paciente não lê receita de terceiro
- [ ] Teste: médico que não é o autor não cancela
- [ ] Eventos publicados sem conteúdo clínico
- [ ] Contrato OpenAPI publicado

## 13. Pendências

- [NEEDS CLARIFICATION: qual a validade padrão de uma receita quando o médico não informa? Sugestão: 30 dias.]
- [NEEDS CLARIFICATION: a renovação exige consulta recente do paciente com aquele médico? Se sim, qual o prazo?]
- [NEEDS CLARIFICATION: qualquer médico ativo pode renovar a receita de outro médico, ou só o autor?]

## 14. Dependências

- Depende de: `001`, `002`, `003`
