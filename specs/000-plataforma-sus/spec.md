# Especificação — Plataforma Digital de Atendimento SUS (spec-mãe)

- **ID:** `000-plataforma-sus`
- **Módulo:** todos
- **Responsável:** Luis
- **Status:** Aprovada
- **Criada em:** 2026-09-03
- **Origem:** Relatório Técnico do Projeto — Hackathon FIAP, Grupo 32

> Esta é a especificação de nível de produto. As features (`001`…`009`) herdam daqui
> contexto, atores, linguagem ubíqua e requisitos não funcionais, e **não** os repetem.

---

## 1. Contexto e problema

O atendimento no SUS obriga o paciente a se deslocar para etapas que poderiam ser
digitais: agendar, retirar resultado de exame, buscar receita, obter um atestado. A
informação fica espalhada entre unidades, o histórico é difícil de reconstruir e o médico
nem sempre consegue analisar um resultado remotamente. Isso gera fila, retrabalho e perda
de rastreabilidade.

## 2. Objetivo

Centralizar em um único ambiente digital o ciclo de atendimento — agendamento, exame,
resultado, parecer, receita e documento — com acesso controlado por perfil, de forma que
paciente acompanhe seu atendimento e médico atue remotamente sobre resultados.

## 3. Princípio central

**O dado técnico produzido pelo exame é separado da interpretação clínica do médico.**
O resultado do exame é persistido de forma independente do parecer. O parecer nunca
altera o resultado. Isso preserva autoria, histórico e rastreabilidade.

## 4. Fora de escopo (MVP)

- Telemedicina, videochamada ou chat.
- Assinatura digital de receita e documento.
- DICOM e visualizador de imagem; a imagem é referenciada por URL/arquivo.
- Integração com sistemas oficiais de saúde, laboratórios ou farmácias.
- Notificação por push, e-mail ou SMS — a notificação é interna à plataforma.
- Tabela de histórico dedicada: o histórico é **derivado** dos registros existentes.
- Aplicativo mobile e dashboard administrativo.
- Faturamento, estoque, prontuário eletrônico completo.

## 5. Atores e permissões

| Ator | Alcance |
|---|---|
| **ADMINISTRADOR** | Acesso global. Gerencia usuários, perfis e cadastros auxiliares (especialidade, unidade de saúde, tipo de exame). Enxerga consultas e exames. |
| **ATENDENTE** | Atendimento administrativo. Cadastra paciente, agenda, remarca e cancela consultas e exames, registra a realização do exame. **Não** emite parecer, receita nem solicitação de exame. |
| **MEDICO** | Atua clinicamente. Consulta histórico e resultados dos seus pacientes, solicita exame, emite parecer, receita e documento médico. |
| **PACIENTE** | Acessa **apenas o que é seu**: dados cadastrais, consultas, exames, resultados, pareceres, receitas, documentos e notificações. Agenda, remarca e cancela suas consultas e exames. |

**RN global:** o `PACIENTE` nunca enxerga dado de outro paciente, em nenhuma listagem,
busca ou detalhe. Tentativa de acesso a registro de terceiro é tratada como recurso
inexistente.

## 6. Escopo funcional do MVP

| # | Módulo | Escopo | Feature |
|---|---|---|---|
| 1 | Usuários e segurança | Cadastro, autenticação, associação de perfis, controle de acesso | `001-iam-seguranca` |
| 2 | Cadastros | Paciente, médico, especialidade, unidade de saúde, tipo de exame | `002-cadastros` |
| 3 | Consultas | Agendamento, remarcação, cancelamento, acompanhamento | `003-consultas` |
| 4 | Exames | Solicitação médica, agendamento, registro de realização | `004-exames` |
| 5 | Resultados | Resultado de imagem e resultado laboratorial estruturado | `005-resultados` |
| 6 | Parecer médico | Interpretação clínica vinculada ao resultado | `006-pareceres` |
| 7 | Receitas | Emissão, renovação e consulta de receita e seus itens | `007-receitas` |
| 8 | Documentos | Atestado, laudo, relatório, encaminhamento, declaração | `008-documentos` |
| 9 | Notificações | Aviso ao usuário sobre eventos do seu atendimento | `009-notificacoes` |

## 7. Jornadas principais

### J-01 — Paciente acompanha seu atendimento
```gherkin
Dado que sou um paciente autenticado
Quando acesso meu atendimento
Então vejo minhas consultas, exames, resultados liberados, pareceres, receitas e documentos
E não vejo nenhum registro de outro paciente
```

### J-02 — Do sintoma ao parecer
```gherkin
Dado que um paciente tem uma consulta realizada
Quando o médico solicita um exame
E a unidade agenda e registra a realização desse exame
E o resultado técnico é registrado
Então o paciente é notificado de que há resultado disponível
E o médico pode registrar um parecer sobre esse resultado
E o resultado técnico permanece inalterado após o parecer
```

### J-03 — Emissão de receita
```gherkin
Dado que sou um médico autenticado com um atendimento em andamento
Quando emito uma receita com um ou mais medicamentos
Então a receita fica disponível para o paciente
E o paciente é notificado
```

### J-04 — Renovação de receita
```gherkin
Dado que existe uma receita anterior emitida para o paciente
Quando o médico solicita a renovação
Então é criada uma nova receita, com nova data de emissão e nova validade
E a receita original é preservada para histórico
```

### J-05 — Histórico derivado
```gherkin
Dado que um paciente possui registros de atendimento
Quando o histórico é solicitado
Então é apresentada uma linha do tempo cronológica montada a partir de consultas,
  solicitações, exames, resultados, pareceres, receitas e documentos
E nenhum dado é duplicado em um registro de histórico próprio
```

## 8. Requisitos funcionais de produto

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | O sistema DEVE autenticar usuários e autorizar cada operação pelo perfil | Obrigatório |
| RF-02 | O sistema DEVE permitir cadastrar paciente e médico vinculados a um usuário | Obrigatório |
| RF-03 | O sistema DEVE permitir agendar, remarcar e cancelar consultas | Obrigatório |
| RF-04 | O sistema DEVE permitir que o médico solicite exame para um paciente | Obrigatório |
| RF-05 | O sistema DEVE permitir agendar e registrar a realização de um exame | Obrigatório |
| RF-06 | O sistema DEVE registrar resultado de imagem e resultado laboratorial estruturado | Obrigatório |
| RF-07 | O sistema DEVE permitir ao médico registrar parecer sobre um resultado | Obrigatório |
| RF-08 | O sistema DEVE permitir emitir e renovar receita com itens | Obrigatório |
| RF-09 | O sistema DEVE permitir emitir documento médico vinculado ao atendimento | Obrigatório |
| RF-10 | O sistema DEVE notificar o usuário nos eventos relevantes do seu atendimento | Obrigatório |
| RF-11 | O sistema DEVE apresentar o histórico do paciente derivado dos registros | Obrigatório |
| RF-12 | O sistema DEVE registrar autoria (quem) e data (quando) em todo ato clínico | Obrigatório |

## 9. Regras de negócio globais

| ID | Regra |
|---|---|
| RN-01 | Todo ato clínico (solicitação, resultado, parecer, receita, documento) tem autor identificado e data de criação imutável. |
| RN-02 | Registro clínico não é apagado fisicamente; é cancelado ou inativado, preservando o histórico. |
| RN-03 | Um paciente só acessa dados dos quais é titular. |
| RN-04 | Parecer médico não altera o resultado do exame ao qual se refere. |
| RN-05 | Só médico emite solicitação de exame, parecer, receita e documento médico. |
| RN-06 | Agendamento não pode ser feito para data e hora no passado. |
| RN-07 | Registro em estado final (cancelado, realizado) não aceita remarcação. |
| RN-08 | Um usuário possui um ou mais perfis; paciente e médico têm, cada um, no máximo um cadastro por usuário. |

## 10. Linguagem ubíqua

| Termo | Significado no domínio |
|---|---|
| **Consulta** | Encontro agendado entre paciente e médico em uma unidade de saúde. |
| **Solicitação de exame** | Pedido médico para que um tipo de exame seja realizado por um paciente. |
| **Exame** | Execução agendada e realizada de uma solicitação, em uma unidade de saúde. |
| **Resultado de exame** | Dado técnico produzido pela realização do exame. Imagem ou laboratorial. |
| **Item de resultado laboratorial** | Um parâmetro medido: valor, unidade, faixa de referência e status. |
| **Parecer médico** | Interpretação clínica que um médico registra sobre um resultado. |
| **Receita** | Documento de prescrição emitido pelo médico, com um ou mais itens. |
| **Documento médico** | Atestado, laudo, relatório, encaminhamento ou declaração. |
| **Notificação** | Aviso interno da plataforma sobre um fato do atendimento do usuário. |
| **Unidade de saúde** | Local físico onde consulta e exame ocorrem (identificada por CNES). |
| **Tipo de exame** | Catálogo do que pode ser solicitado, com instruções de preparo. |

## 11. Eventos de negócio do produto

| Evento | Quando ocorre | Quem se interessa |
|---|---|---|
| Consulta agendada | Nova consulta criada | Paciente, médico |
| Consulta remarcada | Data/hora alterada | Paciente, médico |
| Consulta cancelada | Consulta cancelada | Paciente, médico |
| Exame agendado | Exame marcado em uma unidade | Paciente |
| Resultado disponível | Resultado técnico registrado | Paciente, médico solicitante |
| Parecer criado | Médico conclui a análise | Paciente |
| Receita emitida | Nova receita disponibilizada | Paciente |
| Documento emitido | Novo documento médico disponível | Paciente |

## 12. Requisitos não funcionais

- **Segurança:** autenticação obrigatória em toda rota exceto login e documentação;
  autorização por perfil e por posse do dado; senha nunca trafega nem é armazenada em
  texto puro.
- **Privacidade:** CPF, cartão SUS, resultado, laudo e diagnóstico nunca aparecem em log.
- **Auditoria:** autor e data de criação em todo registro clínico.
- **Disponibilidade da mensageria:** a indisponibilidade do canal de notificação não pode
  impedir o fluxo clínico.
- **Desempenho:** listagens paginadas respondem em menos de 500 ms com volume de demo.
- **Reprodutibilidade:** ambiente sobe com um único comando.
- **Documentação:** API navegável e testável via Swagger.

## 13. Critérios de sucesso do MVP

- [ ] Autenticação funcionando com autorização por perfil.
- [ ] Fluxo demonstrável: consulta → solicitação de exame → exame → resultado.
- [ ] Resultado de imagem e resultado laboratorial estruturado suportados.
- [ ] Parecer, receita e documento corretamente vinculados ao paciente.
- [ ] Ambiente reproduzível com Docker e banco versionado por Flyway.
- [ ] Notificações desacopladas por evento quando a mensageria está habilitada.
- [ ] Paciente não consegue, por nenhuma rota, ler dado de outro paciente.

## 14. Pendências

- [NEEDS CLARIFICATION: o paciente pode agendar consulta diretamente, ou somente atendente e administrador agendam? O relatório afirma que o paciente "realiza agendamentos", mas também atribui a gestão de agendamentos ao atendente.]
- [NEEDS CLARIFICATION: o resultado do exame fica visível ao paciente imediatamente após o registro, ou só depois de haver parecer médico?]
- [NEEDS CLARIFICATION: existe regra de conflito de agenda (mesmo médico, mesmo horário) no MVP, ou o agendamento é livre?]
- [NEEDS CLARIFICATION: qual a validade padrão de uma receita, e a renovação exige consulta recente?]
- [NEEDS CLARIFICATION: o arquivo de imagem é enviado para a plataforma ou apenas referenciado por URL externa?]
