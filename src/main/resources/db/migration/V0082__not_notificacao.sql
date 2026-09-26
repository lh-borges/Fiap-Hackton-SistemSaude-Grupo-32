-- Modulo notificacoes (feature 009). Faixa V0080-V0089; V0080 e V0081 foram ocupadas
-- por documentos, por isso a numeracao comeca em V0082.
-- usuario_id referencia iam_usuario e evento_id referencia o evento produtor apenas
-- por identificador: sem FK entre modulos (Artigo III.3).

CREATE TABLE not_notificacao (
    id             UUID PRIMARY KEY,
    usuario_id     UUID NOT NULL,
    evento_id      UUID NOT NULL,
    tipo           VARCHAR(40) NOT NULL,
    titulo         VARCHAR(150) NOT NULL,
    mensagem       VARCHAR(500) NOT NULL,
    referencia_id  UUID,
    lida           BOOLEAN NOT NULL DEFAULT FALSE,
    data_leitura   TIMESTAMPTZ,
    criado_em      TIMESTAMPTZ NOT NULL,
    atualizado_em  TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_not_tipo CHECK (tipo IN (
        'CONSULTA_AGENDADA', 'CONSULTA_REMARCADA', 'CONSULTA_CANCELADA',
        'EXAME_SOLICITADO', 'EXAME_AGENDADO',
        'RESULTADO_DISPONIVEL', 'PARECER_CRIADO',
        'RECEITA_EMITIDA', 'RECEITA_RENOVADA',
        'DOCUMENTO_EMITIDO')),
    CONSTRAINT ck_not_leitura CHECK (
        (lida = FALSE AND data_leitura IS NULL) OR (lida = TRUE AND data_leitura IS NOT NULL)),
    CONSTRAINT uk_not_evento_usuario UNIQUE (evento_id, usuario_id)
);

-- Listagem do proprio usuario, da mais recente para a mais antiga.
CREATE INDEX idx_not_notificacao_usuario_criado ON not_notificacao (usuario_id, criado_em DESC);
-- Contagem de nao lidas e consultada com frequencia (RNF): indice parcial.
CREATE INDEX idx_not_notificacao_usuario_nao_lida ON not_notificacao (usuario_id) WHERE lida = FALSE;

-- Marcador de idempotencia do consumidor (Artigo VI.3).
CREATE TABLE not_evento_processado (
    evento_id      UUID PRIMARY KEY,
    tipo_evento    VARCHAR(80) NOT NULL,
    processado_em  TIMESTAMPTZ NOT NULL
);
