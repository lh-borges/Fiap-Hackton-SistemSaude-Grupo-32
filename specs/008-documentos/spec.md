# Especificação — Documentos médicos

- **ID:** `008-documentos` · **Módulo:** `documentos` · **Responsável:** Juliana
- **Status:** Em revisão · **Criada em:** 2026-09-03
- **Herda de:** `specs/000-plataforma-sus/spec.md`

## 1. Contexto e problema

Atestado, laudo, relatório, encaminhamento e declaração de comparecimento hoje são papéis
entregues na hora; se o paciente perde, precisa voltar à unidade. Digitalizados, ficam
disponíveis a qualquer momento e com autoria rastreável.

## 2. Objetivo

Permitir que o médico emita documentos vinculados ao atendimento e que o paciente os
consulte e recupere quando precisar.

## 3. Fora de escopo

- Assinatura digital e validação por terceiros (empregador, escola).
- Modelos configuráveis e editor de texto rico.
- Geração de PDF com identidade visual (avaliar apenas se sobrar tempo).
- Código de verificação público do documento.

## 4. Atores e permissões

| Ator | O que pode fazer |
|---|---|
| ADMINISTRADOR | Consultar documentos para fins administrativos |
| ATENDENTE | Nenhum acesso ao conteúdo |
| MEDICO | Emitir e cancelar os próprios documentos; consultar os dos pacientes que atende |
| PACIENTE | Consultar e recuperar os próprios documentos |

## 5. Histórias de usuário

### HU-01 — Emitir documento
```gherkin
Dado que sou médico autenticado
E existe um paciente ativo
Quando emito um documento informando o tipo e o conteúdo
Então o documento é criado com minha autoria e a data de emissão
E o paciente é notificado
```

### HU-02 — Tipos suportados
```gherkin
Dado que sou médico
Quando escolho o tipo do documento
Então posso emitir atestado, laudo, relatório, encaminhamento ou declaração
Quando informo um tipo fora dessa lista
Então a emissão é rejeitada
```

### HU-03 — Vínculo com o atendimento
```gherkin
Dado que existe uma consulta realizada do paciente
Quando emito um documento vinculado a essa consulta
Então o documento aparece no histórico daquele atendimento

Quando a consulta informada é de outro paciente
Então a emissão é rejeitada
```

### HU-04 — Imutabilidade e cancelamento
```gherkin
Dado que um documento foi emitido
Quando se tenta alterar o conteúdo
Então a operação é negada
Quando o médico autor o cancela informando o motivo
Então o documento passa a cancelado e permanece no histórico
```

### HU-05 — Consulta pelo paciente
```gherkin
Dado que sou paciente autenticado
Quando consulto meus documentos
Então vejo o tipo, o médico responsável, a data e o conteúdo
Quando tento consultar documento de outro paciente
Então o registro é tratado como inexistente
```

## 6. Requisitos funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | O sistema DEVE permitir ao médico emitir documento de um dos tipos previstos | Obrigatório |
| RF-02 | O sistema DEVE registrar autor, paciente, tipo, conteúdo e data de emissão | Obrigatório |
| RF-03 | O sistema DEVE permitir vincular o documento a uma consulta | Obrigatório |
| RF-04 | O sistema DEVE impedir alteração do conteúdo após a emissão | Obrigatório |
| RF-05 | O sistema DEVE permitir ao médico autor cancelar o documento, com motivo | Obrigatório |
| RF-06 | O sistema DEVE listar documentos por paciente, por médico, por tipo e por período | Obrigatório |
| RF-07 | O sistema DEVE restringir a leitura do paciente aos próprios documentos | Obrigatório |
| RF-08 | O sistema DEVE notificar o paciente na emissão | Obrigatório |
| RF-09 | O sistema PODE disponibilizar o documento em formato para impressão | Desejável |

## 7. Regras de negócio

| ID | Regra |
|---|---|
| RN-01 | Somente médico emite e cancela documento |
| RN-02 | Tipos válidos: atestado, laudo, relatório, encaminhamento, declaração |
| RN-03 | Conteúdo obrigatório, mínimo de 10 e máximo de 10000 caracteres |
| RN-04 | Documento emitido é imutável; correção exige novo documento |
| RN-05 | Somente o médico autor cancela |
| RN-06 | Se vinculado a uma consulta, a consulta é do mesmo paciente |
| RN-07 | Data de emissão é gerada pelo sistema |
| RN-08 | Documento não é excluído fisicamente |

## 8. Fluxos de exceção

| ID | Situação | Comportamento |
|---|---|---|
| EX-01 | Não-médico tenta emitir | Negar |
| EX-02 | Tipo inválido | Rejeitar listando os tipos válidos |
| EX-03 | Conteúdo vazio ou curto demais | Rejeitar por validação |
| EX-04 | Tentativa de alteração | Negar com explicação da imutabilidade |
| EX-05 | Consulta de outro paciente | Rejeitar |
| EX-06 | Paciente acessa documento de terceiro | Tratar como inexistente |

## 9. Conceitos de domínio

- **Documento médico:** registro formal emitido no atendimento. Tipo, conteúdo, paciente,
  médico responsável, consulta de origem (opcional), data de emissão, situação, referência
  de arquivo (opcional).

## 10. Eventos produzidos

| Evento | Quando ocorre | Quem se interessa |
|---|---|---|
| Documento emitido | Novo documento disponível | Paciente |

## 11. Requisitos não funcionais

- O conteúdo do documento é dado clínico: nunca em log nem em payload de evento.
- A listagem retorna metadados sem o conteúdo; o conteúdo vem só no detalhe.
- Data de emissão imutável, gerada pelo sistema.

## 12. Definition of Done

- [ ] RF-01 a RF-08 implementados e testados (RF-09 se couber no prazo)
- [ ] Teste: atendente e paciente não emitem documento
- [ ] Teste: tentativa de alteração é negada
- [ ] Teste: vínculo com consulta de outro paciente é rejeitado
- [ ] Teste: paciente não lê documento de terceiro
- [ ] Evento publicado sem conteúdo clínico
- [ ] Contrato OpenAPI publicado

## 13. Pendências

- [NEEDS CLARIFICATION: o atestado precisa de campos estruturados (dias de afastamento, CID), ou o conteúdo é texto livre no MVP?]
- [NEEDS CLARIFICATION: geração de PDF entra no MVP ou fica como evolução?]

## 14. Dependências

- Depende de: `001`, `002`, `003`
