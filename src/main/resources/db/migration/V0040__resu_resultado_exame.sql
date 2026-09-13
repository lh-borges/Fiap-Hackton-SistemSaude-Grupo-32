CREATE TABLE resu_resultado_exame (
    id UUID PRIMARY KEY,
    exame_id UUID NOT NULL UNIQUE,
    paciente_id UUID NOT NULL,
    tipo_resultado VARCHAR(20) NOT NULL,
    data_resultado TIMESTAMPTZ NOT NULL,
    observacao VARCHAR(2000),
    arquivo_url VARCHAR(500),
    descricao VARCHAR(2000),
    laudo VARCHAR(5000)
);

CREATE INDEX idx_resu_resultado_paciente_id ON resu_resultado_exame (paciente_id);
CREATE INDEX idx_resu_resultado_data ON resu_resultado_exame (data_resultado);

CREATE TABLE resu_item_resultado_laboratorial (
    id UUID PRIMARY KEY,
    resultado_exame_id UUID NOT NULL,
    nome_parametro VARCHAR(200) NOT NULL,
    valor DOUBLE PRECISION,
    unidade VARCHAR(20),
    valor_minimo_referencia DOUBLE PRECISION,
    valor_maximo_referencia DOUBLE PRECISION,
    resultado_texto VARCHAR(200),
    situacao VARCHAR(20) NOT NULL,
    CONSTRAINT fk_resu_item_resultado FOREIGN KEY (resultado_exame_id)
        REFERENCES resu_resultado_exame (id)
);

CREATE INDEX idx_resu_item_resultado_id ON resu_item_resultado_laboratorial (resultado_exame_id);