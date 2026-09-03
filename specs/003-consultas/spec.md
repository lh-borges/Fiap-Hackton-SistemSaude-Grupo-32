# Especificação — Consultas

- **ID:** `003-consultas` · **Módulo:** `consultas` · **Responsável:** Thiago
- **Status:** Em revisão · **Criada em:** 2026-09-03
- **Herda de:** `specs/000-plataforma-sus/spec.md`

## 1. Contexto e problema

Marcar, remarcar ou cancelar uma consulta hoje custa deslocamento e ligação telefônica, e
o paciente raramente sabe o estado do seu agendamento. A consulta é também o ponto de
partida do restante do atendimento: solicitação de exame, receita e documento nascem dela.

## 2. Objetivo

Permitir agendar, remarcar, cancelar e acompanhar consultas entre paciente e médico em uma
unidade de saúde, com estado sempre visível para os envolvidos.

## 3. Fora de escopo

- Grade de horários e disponibilidade real do médico.
- Fila de espera e encaixe.
- Confirmação de presença por check-in.
- Teleconsulta.

## 4. Atores e permissões

| Ator | O que pode fazer |
|---|---|
| ADMINISTRADOR | Ver e gerenciar todas as consultas |
| ATENDENTE | Agendar, remarcar e cancelar consultas de qualquer paciente; listar por unidade e por data |
| MEDICO | Ver a própria agenda; registrar a realização e as observações da consulta; cancelar as suas |
| PACIENTE | Ver as próprias consultas; agendar, remarcar e cancelar as suas |

## 5. Histórias de usuário

### HU-01 — Agendar
```gherkin
Dado que existem paciente, médico e unidade de saúde ativos
Quando uma consulta é agendada para uma data e hora futuras
Então ela é criada com situação "agendada"
E paciente e médico são notificados

Quando a data e hora informadas já passaram
Então o agendamento é rejeitado
```

### HU-02 — Remarcar
```gherkin
Dado que existe uma consulta agendada
Quando a data e hora são alteradas para um momento futuro
Então a consulta passa a valer na nova data
E o registro guarda que houve remarcação
E os envolvidos são notificados

Dado que a consulta está cancelada ou já foi realizada
Quando se tenta remarcá-la
Então a operação é rejeitada
```

### HU-03 — Cancelar
```gherkin
Dado que existe uma consulta agendada
Quando ela é cancelada com um motivo
Então passa à situação "cancelada" e não é mais remarcável
E os envolvidos são notificados
E o registro permanece no histórico do paciente
```

### HU-04 — Registrar realização
```gherkin
Dado que sou o médico da consulta
E a data da consulta já ocorreu
Quando registro a realização com observações do atendimento
Então a consulta passa a "realizada"
E torna-se possível vincular a ela solicitação de exame, receita e documento
```

### HU-05 — Acompanhamento pelo paciente
```gherkin
Dado que sou paciente autenticado
Quando listo minhas consultas
Então vejo apenas as minhas, ordenadas da mais recente para a mais antiga
E consigo filtrar por situação e por período
```

## 6. Requisitos funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | O sistema DEVE permitir agendar consulta para paciente, médico, unidade, data e hora | Obrigatório |
| RF-02 | O sistema DEVE permitir remarcar consulta ainda não realizada nem cancelada | Obrigatório |
| RF-03 | O sistema DEVE permitir cancelar consulta com motivo | Obrigatório |
| RF-04 | O sistema DEVE permitir ao médico registrar a realização com observações | Obrigatório |
| RF-05 | O sistema DEVE listar consultas com filtro por paciente, médico, unidade, situação e período | Obrigatório |
| RF-06 | O sistema DEVE restringir a listagem do paciente às suas próprias consultas | Obrigatório |
| RF-07 | O sistema DEVE notificar os envolvidos em agendamento, remarcação e cancelamento | Obrigatório |
| RF-08 | O sistema DEVE informar a outros módulos se uma consulta existe e a quem pertence | Obrigatório |

## 7. Regras de negócio

| ID | Regra |
|---|---|
| RN-01 | Consulta só é agendada para data e hora futuras |
| RN-02 | Paciente, médico e unidade precisam existir e estar ativos no momento do agendamento |
| RN-03 | Situações possíveis: agendada, confirmada, realizada, cancelada. A remarcação mantém a consulta agendada e registra a alteração |
| RN-04 | Consulta realizada ou cancelada é imutável, exceto por observações do médico autor |
| RN-05 | Somente o médico da consulta registra sua realização |
| RN-06 | Cancelamento exige motivo |
| RN-07 | Consulta não é excluída fisicamente |

## 8. Fluxos de exceção

| ID | Situação | Comportamento |
|---|---|---|
| EX-01 | Data no passado | Rejeitar informando a regra |
| EX-02 | Paciente, médico ou unidade inexistente/inativo | Rejeitar apontando o campo |
| EX-03 | Remarcar consulta em situação final | Rejeitar por conflito de estado |
| EX-04 | Paciente acessa consulta de terceiro | Tratar como inexistente |
| EX-05 | Médico registra realização de consulta que não é sua | Negar |

## 9. Conceitos de domínio

- **Consulta:** encontro agendado. Paciente, médico, unidade, data e hora, situação,
  motivo, observações, autoria e datas de criação/alteração.

## 10. Eventos produzidos

| Evento | Quando ocorre | Quem se interessa |
|---|---|---|
| Consulta agendada | Nova consulta criada | Paciente e médico |
| Consulta remarcada | Data/hora alterada | Paciente e médico |
| Consulta cancelada | Consulta cancelada | Paciente e médico |

## 11. Requisitos não funcionais

- Listagem paginada, ordenação padrão por data e hora decrescente.
- Autoria de cada mudança de situação é rastreável.
- Motivo e observações da consulta não vão para log.

## 12. Definition of Done

- [ ] RF-01 a RF-08 implementados e testados
- [ ] Teste: agendamento no passado é rejeitado
- [ ] Teste: paciente não vê consulta de terceiro
- [ ] Teste: médico não registra realização de consulta alheia
- [ ] Eventos publicados nas três transições
- [ ] Contrato OpenAPI publicado

## 13. Pendências

- [NEEDS CLARIFICATION: existe verificação de conflito de horário do médico no MVP? Herdado da spec-mãe.]
- [NEEDS CLARIFICATION: paciente pode cancelar com qualquer antecedência, ou há prazo mínimo?]
- [NEEDS CLARIFICATION: a situação "confirmada" tem uso no MVP ou pode ser removida?]

## 14. Dependências

- Depende de: `001`, `002`
- Pré-requisito de: `004`, `007`, `008`
