-- Referencias entre modulos sao validadas pelas portas publicas, sem FKs cruzadas.
-- Sem UNIQUE em resultado_exame_id: um resultado admite varios pareceres.
CREATE TABLE par_parecer_medico (
    id UUID PRIMARY KEY,
    resultado_exame_id UUID NOT NULL,
    medico_id UUID NOT NULL,
    paciente_id UUID NOT NULL,
    descricao VARCHAR(5000) NOT NULL,
    data_parecer TIMESTAMP WITH TIME ZONE NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_par_descricao CHECK (CHAR_LENGTH(TRIM(descricao)) BETWEEN 10 AND 5000)
);

CREATE INDEX idx_par_resultado ON par_parecer_medico (resultado_exame_id);
CREATE INDEX idx_par_medico ON par_parecer_medico (medico_id);
CREATE INDEX idx_par_paciente ON par_parecer_medico (paciente_id);
CREATE INDEX idx_par_data ON par_parecer_medico (data_parecer);
