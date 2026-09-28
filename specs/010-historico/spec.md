# Especificação — Histórico do paciente

- **ID:** `010-historico` · **Módulo:** `historico` · **Responsável:** Danilo (previsto para Thiago no plano-mãe; assumido na entrega final)
- **Status:** Aprovada · **Criada em:** 2026-09-28 · **Aprovada em:** 2026-09-28
- **Herda de:** `specs/000-plataforma-sus/spec.md` (RF-11, jornada J-05)

## 1. Contexto e problema

O atendimento de um paciente está espalhado em sete registros independentes: consultas,
solicitações de exame, exames, resultados, pareceres, receitas e documentos. Cada módulo
lista o que é seu, mas ninguém responde à pergunta "o que aconteceu com este paciente, em
ordem?". O médico que recebe um paciente novo, ou o próprio paciente, precisa abrir sete
listagens e montar a linha do tempo de cabeça.

## 2. Objetivo

Apresentar a linha do tempo do atendimento de um paciente, montada em tempo de leitura a
partir dos registros que já existem, sem copiar nem duplicar dado em uma tabela própria.

## 3. Fora de escopo

- Tabela, view materializada ou cache de histórico (a spec-mãe proíbe: o histórico é derivado).
- Conteúdo clínico no item da linha do tempo (laudo, descrição do parecer, itens da receita,
  texto do documento). O item aponta para o registro de origem; o detalhe é lido na rota do
  módulo dono, com a regra de acesso daquele módulo.
- Exportação (PDF, CSV) e busca textual.
- Histórico de ações administrativas (cadastro alterado, usuário inativado).
- Notificações na linha do tempo: são avisos, não atos do atendimento.

## 4. Atores e permissões

| Ator | O que pode fazer |
|---|---|
| PACIENTE | Consultar o próprio histórico. Qualquer identificação de paciente enviada na requisição é ignorada. |
| MEDICO | Consultar o histórico dos pacientes que atende (pacientes com consulta em que ele é o profissional). |
| ADMINISTRADOR | Consultar o histórico de qualquer paciente. |
| ATENDENTE | Não acessa. O histórico contém resultado, parecer e receita, que são dado clínico. |

Paciente que o médico não atende, ou que não existe, é tratado como recurso inexistente.

## 5. Histórias de usuário

### HU-01 — Paciente vê a própria linha do tempo
```gherkin
Dado que sou um paciente autenticado com consultas, exames, resultados e receitas
Quando consulto meu histórico
Então vejo todos esses registros em uma única lista, do mais recente para o mais antigo
E cada item informa o tipo, a data, a situação, o médico e o registro de origem
```

### HU-02 — Médico vê o histórico de quem atende
```gherkin
Dado que sou um médico autenticado com uma consulta agendada para um paciente
Quando consulto o histórico desse paciente
Então vejo a linha do tempo completa dele, incluindo registros feitos por outros médicos
```

### HU-03 — Médico não vê quem não atende
```gherkin
Dado que sou um médico autenticado sem nenhuma consulta com um paciente
Quando consulto o histórico desse paciente
Então a resposta é de recurso inexistente
```

### HU-04 — Filtro por tipo e período
```gherkin
Dado que um paciente tem registros de vários tipos ao longo de meses
Quando consulto o histórico filtrando por tipo e por período
Então vejo apenas os itens dos tipos pedidos cuja data cai dentro do período
```

### HU-05 — Nada é duplicado
```gherkin
Dado que um paciente tem registros de atendimento
Quando o histórico é consultado
Então nenhum dado é gravado em tabela de histórico
E o histórico reflete o estado atual dos registros, inclusive cancelamentos
```

### HU-06 — Navegação
```gherkin
Dado que vejo um item do histórico
Quando quero o detalhe (laudo, itens da receita, texto do documento)
Então o item identifica o recurso e o id para que eu consulte a rota do módulo dono
```

## 6. Requisitos funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | O sistema DEVE montar a linha do tempo a partir de consultas, solicitações de exame, exames, resultados, pareceres, receitas e documentos do paciente | Obrigatório |
| RF-02 | O sistema DEVE ordenar a linha do tempo pela data do ato, do mais recente para o mais antigo | Obrigatório |
| RF-03 | O sistema DEVE informar em cada item: tipo, identificador, recurso de origem, data, título, situação e médico responsável quando houver | Obrigatório |
| RF-04 | O sistema DEVE permitir filtrar por um ou mais tipos e por período | Obrigatório |
| RF-05 | O sistema DEVE paginar a resposta | Obrigatório |
| RF-06 | O sistema DEVE restringir o acesso conforme a seção 4 | Obrigatório |
| RF-07 | O sistema DEVE apontar o registro relacionado quando existir (exame da solicitação, resultado do parecer, receita de origem da renovação, consulta ou exame do documento) | Desejável |

## 7. Regras de negócio

| ID | Regra |
|---|---|
| RN-01 | O histórico é derivado: nenhuma tabela, view materializada ou cache o persiste |
| RN-02 | O `pacienteId` do PACIENTE vem do token; o da requisição é ignorado |
| RN-03 | O médico só vê pacientes com quem tem consulta não cancelada (mesma regra já usada por `documentos`) |
| RN-04 | Registros cancelados aparecem no histórico com a situação `CANCELADA`/`CANCELADO`; cancelar não apaga o passado |
| RN-05 | A data do item é a data do ato: horário da consulta, criação da solicitação, realização do exame (ou agendamento, se ainda não realizado), data do resultado, do parecer, da emissão da receita e do documento |
| RN-06 | O item não carrega conteúdo clínico: sem laudo, descrição, itens, texto ou arquivo |
| RN-07 | Empate de data é desempatado pelo tipo, na ordem natural do atendimento (consulta, solicitação, exame, resultado, parecer, receita, documento), e depois pelo identificador |
| RN-08 | Tamanho de página entre 1 e 100; período com início posterior ao fim é entrada inválida |

## 8. Fluxos de exceção

| ID | Situação | Comportamento |
|---|---|---|
| EX-01 | MEDICO ou ADMINISTRADOR não informa o paciente | `400` entrada inválida |
| EX-02 | Paciente inexistente (ADMINISTRADOR) ou não atendido pelo médico (MEDICO) | `404` |
| EX-03 | ATENDENTE consulta histórico | `403` |
| EX-04 | Usuário PACIENTE sem cadastro de paciente vinculado | `404` |
| EX-05 | Paciente sem nenhum registro | `200` com página vazia |

## 9. Conceitos de domínio

| Conceito | Definição |
|---|---|
| **Registro do histórico** | Item da linha do tempo: tipo, identificador, data, título, situação, médico e referência ao registro relacionado. Imutável; é uma projeção, não uma entidade persistida. |
| **Tipo de registro** | `CONSULTA`, `SOLICITACAO_EXAME`, `EXAME`, `RESULTADO_EXAME`, `PARECER`, `RECEITA`, `DOCUMENTO`. Cada tipo conhece o recurso REST onde o detalhe é lido. |
| **Linha do tempo** | Coleção ordenada de registros de um paciente, com filtro por tipo e período e paginação em memória. |
| **Fonte de histórico** | Porta de saída: cada módulo clínico é uma fonte que devolve seus registros para um paciente. |

## 10. Requisitos não funcionais

- Leitura sem escrita: nenhuma transação de escrita é aberta.
- Sem acesso a tabela de outro módulo: cada fonte usa apenas a porta `api` do módulo dono.
- Sem conteúdo clínico em log.
- Volume do MVP (dezenas de registros por paciente) permite ordenar e paginar em memória;
  ver risco no plano.

## 11. Definition of Done

- [ ] HU-01 a HU-06 cobertas por teste automatizado.
- [ ] Nenhuma migration, tabela ou entidade JPA criada por esta feature.
- [ ] Cada módulo clínico expõe a leitura por paciente apenas no seu pacote `api`.
- [ ] Contrato OpenAPI publicado e Swagger conforme.
- [ ] `mvn verify -Pquality` verde: cobertura, Modulith e ArchUnit.
- [ ] Postman, README e índice de specs atualizados.

## 12. Pendências

Nenhuma. Decisões tomadas nesta spec, com a origem:

- ATENDENTE sem acesso: o histórico expõe dado clínico e a spec-mãe reserva ao atendente o
  papel administrativo.
- Regra de acesso do médico igual à de `documentos` (consulta não cancelada), para não
  criar uma terceira definição de "meu paciente" no sistema.
- Ordem decrescente por padrão: o uso principal é "o que aconteceu por último".

## 13. Dependências

- `002-cadastros`: paciente e médico do usuário autenticado; existência do paciente.
- `003` a `008`: portas de leitura por paciente em cada módulo (novas nesta feature).
- Não depende de `009-notificacoes` e não publica evento.
