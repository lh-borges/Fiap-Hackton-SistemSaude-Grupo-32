# Modelo de Dados — visão global

Derivado do diagrama entidade-relacionamento do relatório, ajustado às regras da
constituição: PK `UUID`, prefixo de tabela por módulo, **sem FK entre módulos**.

Legenda: `PK` chave primária · `FK` chave estrangeira **dentro do mesmo módulo** ·
`REF` referência lógica a outro módulo (só o `UUID`, validada por porta no caso de uso).

---

## Módulo `iam` — V0001..V0009

### `iam_usuario`
| Coluna | Tipo | Regra |
|---|---|---|
| `id` | UUID | PK |
| `nome` | varchar(150) | obrigatório |
| `cpf` | varchar(11) | obrigatório, único |
| `email` | varchar(150) | obrigatório, único |
| `senha` | varchar(100) | hash BCrypt |
| `ativo` | boolean | default true |
| `criado_em` / `atualizado_em` | timestamptz | |

### `iam_role`
`id` UUID PK · `nome` varchar(30) único · valores: `ADMINISTRADOR`, `ATENDENTE`, `MEDICO`, `PACIENTE`.

### `iam_usuario_role`
`usuario_id` FK · `role_id` FK · PK composta.

---

## Módulo `cadastros` — V0010..V0019

### `cad_paciente`
`id` UUID PK · `usuario_id` REF→iam, único · `cartao_sus` varchar(15) único ·
`data_nascimento` date · `sexo` varchar(20) · `tipo_sanguineo` varchar(3) null ·
`ativo` boolean · auditoria.

### `cad_medico`
`id` UUID PK · `usuario_id` REF→iam, único · `crm` varchar(15) · `uf_crm` char(2) ·
`especialidade_id` FK→`cad_especialidade` · `ativo` boolean · auditoria.
Único composto: (`crm`, `uf_crm`).

### `cad_especialidade`
`id` UUID PK · `nome` varchar(100) único · `descricao` varchar(255) · `ativo`.

### `cad_unidade_saude`
`id` UUID PK · `nome` varchar(150) · `cnes` varchar(7) único · `telefone` varchar(20) ·
`ativo`.

### `cad_tipo_exame`
`id` UUID PK · `nome` varchar(150) único · `preparo` text · `categoria` varchar(20)
(`IMAGEM` | `LABORATORIAL`) · `ativo`.

> `categoria` é acréscimo ao ER original: permite validar, na criação do resultado, que o
> tipo de resultado corresponde ao tipo de exame solicitado.

---

## Módulo `consultas` — V0020..V0029

### `con_consulta`
`id` UUID PK · `paciente_id` REF · `medico_id` REF · `unidade_saude_id` REF ·
`data_hora` timestamptz · `status` varchar(20) · `motivo` varchar(255) ·
`observacoes` text · auditoria.

`status`: `AGENDADA` | `CONFIRMADA` | `REALIZADA` | `CANCELADA` | `REMARCADA`.
Índices: (`paciente_id`, `data_hora`), (`medico_id`, `data_hora`).

---

## Módulo `exames` — V0030..V0039

### `exa_solicitacao_exame`
`id` UUID PK · `consulta_id` REF null · `paciente_id` REF · `medico_id` REF ·
`tipo_exame_id` REF · `data_solicitacao` timestamptz · `status` varchar(20) ·
`justificativa` varchar(255) · auditoria.

`status`: `PENDENTE` | `AGENDADO` | `REALIZADO` | `CANCELADO`.

### `exa_exame`
`id` UUID PK · `solicitacao_exame_id` FK · `paciente_id` REF · `unidade_saude_id` REF ·
`data_agendada` timestamptz · `data_realizacao` timestamptz null · `status` varchar(20) ·
auditoria.

`status`: `AGENDADO` | `REALIZADO` | `CANCELADO`.

---

## Módulo `resultados` — V0040..V0049

### `res_resultado_exame`
`id` UUID PK · `exame_id` REF, **único** (1:1) · `paciente_id` REF ·
`tipo_resultado` varchar(15) (`IMAGEM` | `LABORATORIAL`) · `data_resultado` timestamptz ·
`observacao` text · auditoria.

### `res_resultado_imagem`
`id` UUID PK · `resultado_exame_id` FK único · `arquivo_url` varchar(500) ·
`descricao` text · `laudo` text.

### `res_resultado_laboratorial`
`id` UUID PK · `resultado_exame_id` FK único.

### `res_item_resultado_laboratorial`
`id` UUID PK · `resultado_laboratorial_id` FK · `nome_parametro` varchar(100) ·
`valor` numeric(12,4) null · `unidade` varchar(20) null ·
`valor_minimo_referencia` numeric(12,4) null · `valor_maximo_referencia` numeric(12,4) null ·
`resultado_texto` varchar(100) null · `status` varchar(25).

`status`: `NORMAL` | `ABAIXO_REFERENCIA` | `ACIMA_REFERENCIA` | `NAO_APLICAVEL`.

**Invariante:** ou (`valor` e `unidade`) preenchidos, ou `resultado_texto` preenchido.
Nunca ambos vazios. Faixa de referência só se aplica a resultado quantitativo.

**Invariante 1:1:** um `res_resultado_exame` tem exatamente um filho — imagem **ou**
laboratorial, coerente com `tipo_resultado`.

---

## Módulo `pareceres` — V0050..V0059

### `par_parecer_medico`
`id` UUID PK · `resultado_exame_id` REF · `medico_id` REF · `paciente_id` REF ·
`descricao` text · `data_parecer` timestamptz · auditoria.

Um resultado pode receber mais de um parecer (segunda opinião); o parecer é imutável após
criado. Índice: (`resultado_exame_id`), (`paciente_id`, `data_parecer`).

---

## Módulo `receitas` — V0060..V0069

### `rec_receita`
`id` UUID PK · `consulta_id` REF null · `paciente_id` REF · `medico_id` REF ·
`data_emissao` timestamptz · `validade` date · `observacao` text · `status` varchar(20) ·
`receita_origem_id` FK null (renovação aponta para a receita anterior) · auditoria.

`status`: `ATIVA` | `VENCIDA` | `CANCELADA` | `RENOVADA`.

### `rec_item_receita`
`id` UUID PK · `receita_id` FK · `medicamento` varchar(200) · `dosagem` varchar(100) ·
`frequencia` varchar(100) · `duracao` varchar(100) · `orientacao` text.

Invariante: receita tem no mínimo um item.

---

## Módulo `documentos` — V0070..V0079

### `doc_documento_medico`
`id` UUID PK · `consulta_id` REF null · `paciente_id` REF · `medico_id` REF ·
`tipo_documento` varchar(25) · `conteudo` text · `arquivo_url` varchar(500) null ·
`data_emissao` timestamptz · auditoria.

`tipo_documento`: `ATESTADO` | `LAUDO` | `RELATORIO` | `ENCAMINHAMENTO` | `DECLARACAO`.

---

## Módulo `notificacoes` — V0080..V0089

### `not_notificacao`
`id` UUID PK · `usuario_id` REF · `tipo` varchar(40) · `titulo` varchar(150) ·
`mensagem` varchar(500) · `referencia_id` UUID null · `lida` boolean default false ·
`data_leitura` timestamptz null · `criado_em` timestamptz.

`referencia_id` aponta para o registro de origem (consulta, exame, resultado…) apenas para
navegação; sem FK.

### `not_evento_processado`
`evento_id` UUID PK · `processado_em` timestamptz — deduplicação para idempotência do
consumidor.

---

## Infraestrutura — V0090..V0099

### `event_publication`
Tabela do Spring Modulith para publicação transacional de eventos (schema fornecido pela
biblioteca; a migration apenas a cria explicitamente em vez de deixar o `ddl-auto` fazer).

---

## Divergências propositais em relação ao ER do relatório

| Ponto | ER original | Aqui | Motivo |
|---|---|---|---|
| Chaves | `id` numérico implícito | `UUID` | Referência entre módulos sem sequência compartilhada; Artigo V.4 |
| FK entre domínios | `consulta.paciente_id` como FK física | referência lógica `REF` | Artigo III.3 — permite extrair módulo |
| Nome de tabela | `CONSULTA`, `EXAME` | prefixo por módulo (`con_`, `exa_`) | Fronteira visível no banco |
| `tipo_exame` | nome e preparo | acrescido `categoria` | Validar coerência resultado × exame |
| `receita` | sem vínculo de renovação | `receita_origem_id` | Rastrear renovação (J-04) preservando a original |
| Notificação | sem controle de reentrega | `not_evento_processado` | Artigo VI.3 — idempotência |
