# Especificação — Solicitação e realização de exames

- **ID:** `004-exames` · **Módulo:** `exames` · **Responsável:** Thiago
- **Status:** Em revisão · **Criada em:** 2026-09-03
- **Herda de:** `specs/000-plataforma-sus/spec.md`

## 1. Contexto e problema

Hoje o pedido de exame é um papel: some, não é rastreável e ninguém sabe se o exame foi
marcado ou realizado. Separar o **pedido** (ato médico) da **execução** (ato operacional
da unidade) é o que torna o fluxo auditável.

## 2. Objetivo

Permitir que o médico solicite exames e que a unidade agende e registre a realização,
mantendo pedido e execução como registros distintos e rastreáveis.

## 3. Fora de escopo

- Registro do resultado (é `005-resultados`).
- Integração com laboratório externo.
- Controle de vagas, agenda e capacidade da unidade.
- Autorização/regulação de procedimento de alto custo.

## 4. Atores e permissões

| Ator | O que pode fazer |
|---|---|
| ADMINISTRADOR | Ver todas as solicitações e exames; cancelar |
| ATENDENTE | Agendar exame a partir de uma solicitação; registrar realização; remarcar e cancelar |
| MEDICO | Criar solicitação para seus pacientes; acompanhar as próprias solicitações; cancelar solicitação ainda não agendada |
| PACIENTE | Ver as próprias solicitações e exames; consultar preparo e local |

## 5. Histórias de usuário

### HU-01 — Solicitar exame
```gherkin
Dado que sou médico autenticado
E existe um paciente ativo e um tipo de exame ativo
Quando crio uma solicitação de exame com justificativa
Então a solicitação é criada como "pendente" com minha autoria e a data
E o paciente é notificado do pedido

Quando quem tenta solicitar não é médico
Então a operação é negada
```

### HU-02 — Agendar o exame
```gherkin
Dado que existe uma solicitação pendente
Quando o exame é agendado em uma unidade para uma data futura
Então é criado um exame vinculado àquela solicitação
E a solicitação passa a "agendada"
E o paciente é notificado com data, local e instruções de preparo

Quando a solicitação já está agendada, realizada ou cancelada
Então o agendamento é rejeitado
```

### HU-03 — Registrar realização
```gherkin
Dado que existe um exame agendado
Quando a realização é registrada com data e hora
Então o exame passa a "realizado"
E a solicitação correspondente passa a "realizada"
E torna-se possível registrar o resultado desse exame

Quando a data de realização é futura
Então o registro é rejeitado
```

### HU-04 — Cancelar
```gherkin
Dado que existe uma solicitação pendente
Quando o médico autor ou o administrador a cancela com motivo
Então ela passa a "cancelada" e não pode mais ser agendada

Dado que existe um exame agendado ainda não realizado
Quando ele é cancelado
Então o exame passa a "cancelado" e a solicitação volta a "pendente"
```

### HU-05 — Acompanhamento do paciente
```gherkin
Dado que sou paciente autenticado
Quando consulto meus exames
Então vejo o tipo, a situação, a unidade, a data e o preparo necessário
E vejo apenas os meus
```

## 6. Requisitos funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | O sistema DEVE permitir ao médico criar solicitação de exame para um paciente, opcionalmente vinculada a uma consulta | Obrigatório |
| RF-02 | O sistema DEVE permitir agendar um exame a partir de uma solicitação, em uma unidade e data | Obrigatório |
| RF-03 | O sistema DEVE permitir registrar a realização do exame | Obrigatório |
| RF-04 | O sistema DEVE permitir cancelar solicitação e exame, com motivo | Obrigatório |
| RF-05 | O sistema DEVE listar solicitações e exames por paciente, médico, unidade, tipo, situação e período | Obrigatório |
| RF-06 | O sistema DEVE apresentar ao paciente as instruções de preparo do tipo de exame agendado | Obrigatório |
| RF-07 | O sistema DEVE notificar o paciente na solicitação e no agendamento | Obrigatório |
| RF-08 | O sistema DEVE informar a outros módulos a existência, o titular e a situação de um exame | Obrigatório |

## 7. Regras de negócio

| ID | Regra |
|---|---|
| RN-01 | Somente médico cria solicitação de exame |
| RN-02 | Um exame pertence a exatamente uma solicitação; uma solicitação tem no máximo um exame ativo |
| RN-03 | Situações da solicitação: pendente, agendada, realizada, cancelada |
| RN-04 | Situações do exame: agendado, realizado, cancelado |
| RN-05 | Data de agendamento é futura; data de realização não é futura |
| RN-06 | Solicitação cancelada não é agendável; exame realizado não é cancelável |
| RN-07 | Paciente, tipo de exame e unidade precisam estar ativos no momento da operação |
| RN-08 | Se a solicitação está vinculada a uma consulta, essa consulta é do mesmo paciente |

## 8. Fluxos de exceção

| ID | Situação | Comportamento |
|---|---|---|
| EX-01 | Não-médico tenta solicitar | Negar |
| EX-02 | Agendar solicitação em situação final | Rejeitar por conflito |
| EX-03 | Realização com data futura | Rejeitar |
| EX-04 | Cancelar exame já realizado | Rejeitar por conflito |
| EX-05 | Paciente acessa exame de terceiro | Tratar como inexistente |
| EX-06 | Consulta informada é de outro paciente | Rejeitar |

## 9. Conceitos de domínio

- **Solicitação de exame:** pedido médico. Paciente, médico solicitante, tipo de exame,
  consulta de origem (opcional), justificativa, data, situação.
- **Exame:** execução da solicitação. Unidade, data agendada, data de realização, situação.

## 10. Eventos produzidos

| Evento | Quando ocorre | Quem se interessa |
|---|---|---|
| Exame solicitado | Solicitação criada | Paciente |
| Exame agendado | Exame marcado | Paciente |
| Exame realizado | Realização registrada | Módulo de resultados |

## 11. Requisitos não funcionais

- Justificativa clínica não vai para log nem para payload de evento.
- Listagens paginadas com filtro combinado.

## 12. Definition of Done

- [ ] RF-01 a RF-08 implementados e testados
- [ ] Teste: atendente não consegue criar solicitação
- [ ] Teste: paciente não vê exame de terceiro
- [ ] Teste: transições de situação inválidas são rejeitadas
- [ ] Eventos publicados
- [ ] Contrato OpenAPI publicado

## 13. Pendências

- [NEEDS CLARIFICATION: uma solicitação pode conter vários tipos de exame (pedido múltiplo), ou é um tipo por solicitação? (Assumido: um por solicitação.)]
- [NEEDS CLARIFICATION: solicitação de exame tem prazo de validade para ser agendada?]
- [NEEDS CLARIFICATION: o paciente pode agendar o próprio exame ou só atendente/administrador?]

## 14. Dependências

- Depende de: `001`, `002`, `003`
- Pré-requisito de: `005`
