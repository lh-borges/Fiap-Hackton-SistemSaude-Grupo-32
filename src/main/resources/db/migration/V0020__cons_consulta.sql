CREATE TABLE cons_consulta (
    id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL,
    medico_id UUID NOT NULL,
    unidade_saude_id UUID NOT NULL,
    data_hora TIMESTAMPTZ NOT NULL,
    situacao VARCHAR(20) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    observacoes VARCHAR(2000),
    motivo_cancelamento VARCHAR(500),
    remarcada BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL
);

-- Indices para os filtros de RF-05 (paciente, medico, unidade, situacao, periodo)
CREATE INDEX idx_cons_consulta_paciente_id ON cons_consulta (paciente_id);
CREATE INDEX idx_cons_consulta_medico_id ON cons_consulta (medico_id);
CREATE INDEX idx_cons_consulta_unidade_saude_id ON cons_consulta (unidade_saude_id);
CREATE INDEX idx_cons_consulta_situacao ON cons_consulta (situacao);
CREATE INDEX idx_cons_consulta_data_hora ON cons_consulta (data_hora);