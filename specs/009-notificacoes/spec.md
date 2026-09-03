# Especificação — Notificações

- **ID:** `009-notificacoes` · **Módulo:** `notificacoes` · **Responsável:** Danilo
- **Status:** Em revisão · **Criada em:** 2026-09-03
- **Herda de:** `specs/000-plataforma-sus/spec.md`

## 1. Contexto e problema

O paciente só descobre que o exame foi marcado, que o resultado saiu ou que a consulta foi
cancelada se alguém ligar para ele. A plataforma precisa avisar sozinha — e sem que a
geração do aviso trave ou dependa do fluxo clínico que a originou.

## 2. Objetivo

Registrar e disponibilizar avisos ao usuário sobre os fatos relevantes do seu atendimento,
gerados de forma desacoplada dos módulos que os produzem.

## 3. Fora de escopo

- Envio por e-mail, SMS, push ou WhatsApp.
- Preferências de canal e de opt-out por tipo.
- Agendamento de lembrete ("sua consulta é amanhã").
- Notificação em tempo real por websocket.

## 4. Atores e permissões

| Ator | O que pode fazer |
|---|---|
| ADMINISTRADOR | Consultar as próprias notificações |
| ATENDENTE | Consultar as próprias notificações |
| MEDICO | Consultar as próprias notificações |
| PACIENTE | Consultar as próprias notificações e marcá-las como lidas |

Ninguém lê a notificação de outra pessoa — nem o administrador.

## 5. Histórias de usuário

### HU-01 — Receber aviso
```gherkin
Dado que um fato relevante ocorreu no meu atendimento
Quando o sistema processa esse fato
Então uma notificação é registrada para mim, com título, mensagem, tipo e data
E ela aparece como não lida
```

### HU-02 — Listar e marcar como lida
```gherkin
Dado que sou um usuário autenticado com notificações
Quando listo minhas notificações
Então vejo as minhas, da mais recente para a mais antiga
E consigo filtrar apenas as não lidas
Quando marco uma como lida
Então ela deixa de contar como não lida e registra a data de leitura
```

### HU-03 — Isolamento
```gherkin
Dado que sou um usuário autenticado
Quando tento ler ou marcar como lida a notificação de outra pessoa
Então a operação é tratada como registro inexistente
```

### HU-04 — Não repetir aviso
```gherkin
Dado que o mesmo fato é entregue mais de uma vez ao processador de notificações
Quando ele é processado novamente
Então nenhuma notificação duplicada é criada
```

### HU-05 — Independência do fluxo clínico
```gherkin
Dado que o canal de mensageria está indisponível
Quando uma consulta é agendada
Então o agendamento é concluído normalmente
E a notificação é gerada assim que o processamento do fato ocorre
```

### HU-06 — Navegação
```gherkin
Dado que recebi uma notificação sobre um resultado disponível
Quando abro a notificação
Então ela identifica o registro de origem para que eu possa consultá-lo
```

## 6. Requisitos funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | O sistema DEVE registrar notificação para o usuário destinatário a partir dos fatos do atendimento | Obrigatório |
| RF-02 | O sistema DEVE registrar tipo, título, mensagem, referência de origem, situação de leitura e data | Obrigatório |
| RF-03 | O sistema DEVE listar as notificações do próprio usuário, com filtro por lidas/não lidas | Obrigatório |
| RF-04 | O sistema DEVE permitir marcar notificação como lida | Obrigatório |
| RF-05 | O sistema DEVE informar a quantidade de não lidas | Obrigatório |
| RF-06 | O sistema DEVE ignorar o reprocessamento de um fato já processado | Obrigatório |
| RF-07 | O sistema DEVE gerar notificação sem impedir ou reverter a operação de origem | Obrigatório |
| RF-08 | O sistema DEVE cobrir os fatos: consulta agendada, remarcada e cancelada; exame solicitado e agendado; resultado disponível; parecer criado; receita emitida e renovada; documento emitido | Obrigatório |

## 7. Regras de negócio

| ID | Regra |
|---|---|
| RN-01 | Toda notificação pertence a exatamente um usuário destinatário |
| RN-02 | Um fato pode gerar notificação para mais de um destinatário (por exemplo, paciente e médico) |
| RN-03 | O mesmo fato não gera duas notificações para o mesmo destinatário |
| RN-04 | Notificação nasce não lida; a leitura é irreversível |
| RN-05 | A mensagem não contém dado clínico: informa que existe um registro, não o seu conteúdo |
| RN-06 | Falha ao gerar notificação não desfaz a operação clínica que a originou |
| RN-07 | Notificação não é excluída pelo usuário no MVP |

## 8. Fluxos de exceção

| ID | Situação | Comportamento |
|---|---|---|
| EX-01 | Fato duplicado | Descartar silenciosamente e registrar o descarte |
| EX-02 | Destinatário inexistente ou inativo | Descartar e registrar; não falhar o processamento |
| EX-03 | Canal de mensageria indisponível | Continuar operando; o fluxo clínico não é afetado |
| EX-04 | Usuário marca notificação de terceiro como lida | Tratar como inexistente |
| EX-05 | Falha no processamento | Permitir nova tentativa sem gerar duplicata |

## 9. Conceitos de domínio

- **Notificação:** aviso interno destinado a um usuário. Tipo, título, mensagem,
  referência ao registro de origem, situação de leitura, data de criação e de leitura.
- **Fato processado:** marca de que um fato de negócio já foi tratado, para evitar
  duplicidade.

## 10. Eventos consumidos

| Fato | Origem | Destinatários |
|---|---|---|
| Consulta agendada / remarcada / cancelada | Consultas | Paciente e médico |
| Exame solicitado | Exames | Paciente |
| Exame agendado | Exames | Paciente |
| Resultado disponível | Resultados | Paciente e médico solicitante |
| Parecer criado | Pareceres | Paciente |
| Receita emitida / renovada | Receitas | Paciente |
| Documento emitido | Documentos | Paciente |

Contrato detalhado em `specs/000-plataforma-sus/events.md`.

## 11. Requisitos não funcionais

- A mensagem da notificação nunca contém valor de exame, laudo, diagnóstico ou medicamento.
- O processamento de um fato ocorre depois da confirmação da operação de origem.
- A listagem de notificações responde em menos de 300 ms.
- A contagem de não lidas é consultada com frequência: precisa ser barata.

## 12. Definition of Done

- [ ] RF-01 a RF-08 implementados e testados
- [ ] Teste: reprocessamento do mesmo fato não duplica notificação
- [ ] Teste: usuário não lê nem marca notificação de terceiro
- [ ] Teste: fluxo clínico conclui com o canal de mensageria indisponível
- [ ] Teste: mensagem gerada não contém conteúdo clínico
- [ ] Cobertura dos onze fatos previstos
- [ ] Contrato OpenAPI publicado

## 13. Pendências

- [NEEDS CLARIFICATION: o médico deve ser notificado do agendamento e do cancelamento das suas consultas, ou só o paciente? (Assumido: ambos.)]
- [NEEDS CLARIFICATION: se o resultado só ficar visível ao paciente após o parecer, a notificação de "resultado disponível" vai só para o médico? Depende da pendência da spec-mãe.]

## 14. Dependências

- Depende de: `001` (destinatário) e do contrato de eventos.
- Pode ser desenvolvido em paralelo aos produtores, contra `events.md`.
