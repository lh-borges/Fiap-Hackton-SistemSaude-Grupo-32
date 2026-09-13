CREATE TABLE rece_receita (
    id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL,
    medico_id UUID NOT NULL,
    consulta_id UUID,
    data_emissao TIMESTAMPTZ NOT NULL,
    validade TIMESTAMPTZ NOT NULL,
    observacao VARCHAR(1000),
    situacao VARCHAR(20) NOT NULL,
    receita_origem_id UUID,
    motivo_cancelamento VARCHAR(500),
    CONSTRAINT fk_rece_receita_origem FOREIGN KEY (receita_origem_id)
        REFERENCES rece_receita (id)
);

CREATE INDEX idx_rece_receita_paciente_id ON rece_receita (paciente_id);
CREATE INDEX idx_rece_receita_medico_id ON rece_receita (medico_id);
CREATE INDEX idx_rece_receita_situacao ON rece_receita (situacao);
CREATE INDEX idx_rece_receita_origem_id ON rece_receita (receita_origem_id);

CREATE TABLE rece_item_receita (
    id UUID PRIMARY KEY,
    receita_id UUID NOT NULL,
    medicamento VARCHAR(200) NOT NULL,
    dosagem VARCHAR(100) NOT NULL,
    frequencia VARCHAR(100) NOT NULL,
    duracao VARCHAR(100) NOT NULL,
    orientacao VARCHAR(500),
    CONSTRAINT fk_rece_item_receita FOREIGN KEY (receita_id)
        REFERENCES rece_receita (id)
);

CREATE INDEX idx_rece_item_receita_id ON rece_item_receita (receita_id);