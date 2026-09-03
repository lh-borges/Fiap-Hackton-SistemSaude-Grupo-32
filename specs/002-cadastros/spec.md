# Especificação — Cadastros: paciente, médico e apoio

- **ID:** `002-cadastros` · **Módulo:** `cadastros` · **Responsável:** Luis
- **Status:** Em revisão · **Criada em:** 2026-09-03
- **Herda de:** `specs/000-plataforma-sus/spec.md`

## 1. Contexto e problema

O usuário do `001` é só identidade de acesso. O atendimento precisa saber quem é o paciente
(cartão SUS, nascimento, sexo) e quem é o médico (CRM, especialidade), além dos catálogos
que sustentam agendamento e exames: especialidade, unidade de saúde e tipo de exame.

## 2. Objetivo

Manter os dados de domínio de pacientes e médicos e os catálogos de apoio, de modo que
consultas, exames e documentos possam referenciá-los de forma confiável.

## 3. Fora de escopo

- Endereço, contato completo e dados socioeconômicos do paciente.
- Agenda e disponibilidade de horários do médico.
- Vínculo de médico com unidade de saúde.
- Importação de base do CNES ou do CADSUS.

## 4. Atores e permissões

| Ator | O que pode fazer |
|---|---|
| ADMINISTRADOR | Tudo: cadastrar/editar/inativar paciente, médico, especialidade, unidade e tipo de exame |
| ATENDENTE | Cadastrar e editar paciente; consultar médicos, unidades e tipos de exame |
| MEDICO | Consultar pacientes que atende; consultar catálogos; ver e editar o próprio cadastro |
| PACIENTE | Ver e atualizar os próprios dados; consultar catálogos públicos (especialidade, unidade, tipo de exame) |

## 5. Histórias de usuário

### HU-01 — Cadastrar paciente
```gherkin
Dado que sou atendente autenticado
E existe um usuário sem cadastro de paciente
Quando registro cartão SUS, data de nascimento e sexo para esse usuário
Então o paciente é criado e passa a poder ter consultas e exames

Quando tento cadastrar um segundo paciente para o mesmo usuário
Então o cadastro é rejeitado
```

### HU-02 — Cadastrar médico
```gherkin
Dado que sou administrador
Quando registro CRM, UF do CRM e especialidade para um usuário com perfil MEDICO
Então o médico é criado

Quando o usuário informado não possui o perfil MEDICO
Então o cadastro é rejeitado
```

### HU-03 — Catálogo de tipos de exame
```gherkin
Dado que sou administrador
Quando cadastro um tipo de exame com nome, categoria (imagem ou laboratorial) e preparo
Então ele fica disponível para solicitação médica
```

### HU-04 — Paciente vê seus dados
```gherkin
Dado que sou paciente autenticado
Quando consulto meu cadastro
Então vejo meus próprios dados
Quando tento consultar o cadastro de outro paciente
Então o registro é tratado como inexistente
```

### HU-05 — Inativação preserva histórico
```gherkin
Dado que um médico possui pareceres e receitas emitidos
Quando ele é inativado
Então ele não pode mais ser escolhido em novos agendamentos
E os registros que ele produziu continuam íntegros e visíveis
```

## 6. Requisitos funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | O sistema DEVE permitir cadastrar, consultar, editar e inativar paciente | Obrigatório |
| RF-02 | O sistema DEVE permitir cadastrar, consultar, editar e inativar médico | Obrigatório |
| RF-03 | O sistema DEVE manter catálogo de especialidades | Obrigatório |
| RF-04 | O sistema DEVE manter catálogo de unidades de saúde | Obrigatório |
| RF-05 | O sistema DEVE manter catálogo de tipos de exame, com categoria e preparo | Obrigatório |
| RF-06 | O sistema DEVE permitir listar pacientes com busca por nome, CPF ou cartão SUS | Obrigatório |
| RF-07 | O sistema DEVE permitir listar médicos filtrando por especialidade | Obrigatório |
| RF-08 | O sistema DEVE informar a outros módulos se um paciente/médico/unidade/tipo existe e está ativo | Obrigatório |

## 7. Regras de negócio

| ID | Regra |
|---|---|
| RN-01 | Um usuário tem no máximo um cadastro de paciente e no máximo um de médico |
| RN-02 | Cartão SUS é único e tem 15 dígitos |
| RN-03 | A dupla CRM + UF é única |
| RN-04 | Médico só pode ser cadastrado para usuário que possui o perfil MEDICO; paciente, para usuário com perfil PACIENTE |
| RN-05 | Data de nascimento não pode ser futura |
| RN-06 | Registro inativo não pode ser referenciado em novo agendamento, solicitação ou emissão |
| RN-07 | Item de catálogo em uso não é excluído; é inativado |
| RN-08 | Nome de especialidade, CNES da unidade e nome de tipo de exame são únicos |

## 8. Fluxos de exceção

| ID | Situação | Comportamento |
|---|---|---|
| EX-01 | Usuário já tem cadastro de paciente/médico | Rejeitar com conflito |
| EX-02 | Cartão SUS ou CRM duplicado | Rejeitar com conflito, apontando o campo |
| EX-03 | Especialidade informada não existe ou está inativa | Rejeitar |
| EX-04 | Paciente tenta ler cadastro alheio | Tratar como inexistente |

## 9. Conceitos de domínio

- **Paciente:** pessoa atendida. Cartão SUS, data de nascimento, sexo, tipo sanguíneo
  (opcional), situação, vínculo com um usuário.
- **Médico:** profissional habilitado. CRM, UF do CRM, especialidade, situação, vínculo com
  um usuário.
- **Especialidade:** área de atuação médica. Nome, descrição.
- **Unidade de saúde:** local de atendimento. Nome, CNES, telefone.
- **Tipo de exame:** o que pode ser solicitado. Nome, categoria (imagem ou laboratorial),
  preparo.

## 10. Eventos produzidos

Nenhum no MVP.

## 11. Requisitos não funcionais

- CPF e cartão SUS nunca em log.
- Busca por nome com no mínimo 3 caracteres, resultado paginado.
- A consulta de existência usada por outros módulos responde abaixo de 100 ms.

## 12. Definition of Done

- [ ] RF-01 a RF-08 implementados e testados
- [ ] Teste: paciente não lê cadastro de terceiro
- [ ] Teste: cadastro de médico para usuário sem perfil MEDICO é rejeitado
- [ ] Catálogos com carga inicial (seed) para demonstração
- [ ] Contrato OpenAPI publicado

## 13. Pendências

- [NEEDS CLARIFICATION: o paciente pode alterar o próprio cartão SUS e data de nascimento, ou só atendente/administrador?]
- [NEEDS CLARIFICATION: a categoria do tipo de exame deve restringir o tipo de resultado aceito? (Assumido: sim — ver `data-model.md`.)]

## 14. Dependências

- Depende de: `001-iam-seguranca`
- Pré-requisito de: `003`, `004`, `006`, `007`, `008`
