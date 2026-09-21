CREATE TABLE doc_documento_medico (
    id UUID PRIMARY KEY,
    tipo_documento VARCHAR(20) NOT NULL,
    conteudo VARCHAR(10000) NOT NULL,
    paciente_id UUID NOT NULL,
    medico_id UUID NOT NULL,
    consulta_id UUID,
    data_emissao TIMESTAMPTZ NOT NULL,
    arquivo_url VARCHAR(500),
    situacao VARCHAR(20) NOT NULL,
    motivo_cancelamento VARCHAR(1000),
    CONSTRAINT ck_doc_tipo CHECK (tipo_documento IN ('ATESTADO', 'LAUDO', 'RELATORIO', 'ENCAMINHAMENTO', 'DECLARACAO')),
    CONSTRAINT ck_doc_situacao CHECK (situacao IN ('EMITIDO', 'CANCELADO')),
    CONSTRAINT ck_doc_motivo_cancelamento CHECK (situacao <> 'CANCELADO' OR motivo_cancelamento IS NOT NULL)
);

CREATE INDEX idx_doc_documento_paciente_id ON doc_documento_medico (paciente_id);
CREATE INDEX idx_doc_documento_medico_id ON doc_documento_medico (medico_id);
CREATE INDEX idx_doc_documento_consulta_id ON doc_documento_medico (consulta_id);
CREATE INDEX idx_doc_documento_tipo ON doc_documento_medico (tipo_documento);
CREATE INDEX idx_doc_documento_data_emissao ON doc_documento_medico (data_emissao);
