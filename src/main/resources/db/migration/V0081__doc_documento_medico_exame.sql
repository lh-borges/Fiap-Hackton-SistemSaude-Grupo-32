ALTER TABLE doc_documento_medico
    ADD COLUMN exame_id UUID;

CREATE INDEX idx_doc_documento_exame_id ON doc_documento_medico (exame_id);
