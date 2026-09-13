CREATE TABLE exam_solicitacao_exame (
    id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL,
    medico_id UUID NOT NULL,
    tipo_exame_id UUID NOT NULL,
    consulta_id UUID,
    justificativa VARCHAR(1000) NOT NULL,
    situacao VARCHAR(20) NOT NULL,
    motivo_cancelamento VARCHAR(500),
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_exam_solicitacao_paciente_id ON exam_solicitacao_exame (paciente_id);
CREATE INDEX idx_exam_solicitacao_medico_id ON exam_solicitacao_exame (medico_id);
CREATE INDEX idx_exam_solicitacao_tipo_exame_id ON exam_solicitacao_exame (tipo_exame_id);
CREATE INDEX idx_exam_solicitacao_situacao ON exam_solicitacao_exame (situacao);

CREATE TABLE exam_exame (
    id UUID PRIMARY KEY,
    solicitacao_exame_id UUID NOT NULL,
    unidade_saude_id UUID NOT NULL,
    data_agendada TIMESTAMPTZ NOT NULL,
    data_realizacao TIMESTAMPTZ,
    situacao VARCHAR(20) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_exam_exame_solicitacao FOREIGN KEY (solicitacao_exame_id)
        REFERENCES exam_solicitacao_exame (id)
);

CREATE INDEX idx_exam_exame_solicitacao_id ON exam_exame (solicitacao_exame_id);
CREATE INDEX idx_exam_exame_unidade_saude_id ON exam_exame (unidade_saude_id);
CREATE INDEX idx_exam_exame_situacao ON exam_exame (situacao);