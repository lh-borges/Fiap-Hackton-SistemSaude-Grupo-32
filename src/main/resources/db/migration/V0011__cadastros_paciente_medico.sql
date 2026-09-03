-- Modulo cadastros: dados de dominio de paciente e medico.
-- usuario_id referencia o modulo iam de forma logica (Artigo III.3: sem FK entre modulos).
CREATE TABLE cad_paciente (
    id               UUID        PRIMARY KEY,
    usuario_id       UUID        NOT NULL,
    cartao_sus       VARCHAR(15) NOT NULL,
    data_nascimento  DATE        NOT NULL,
    sexo             VARCHAR(15) NOT NULL,
    tipo_sanguineo   VARCHAR(3),
    ativo            BOOLEAN     NOT NULL DEFAULT TRUE,
    criado_em        TIMESTAMPTZ NOT NULL,
    atualizado_em    TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_cad_paciente_usuario    UNIQUE (usuario_id),
    CONSTRAINT uk_cad_paciente_cartao_sus UNIQUE (cartao_sus),
    CONSTRAINT ck_cad_paciente_sexo CHECK (sexo IN ('MASCULINO', 'FEMININO', 'OUTRO', 'NAO_INFORMADO'))
);

CREATE INDEX idx_cad_paciente_ativo ON cad_paciente (ativo);

CREATE TABLE cad_medico (
    id               UUID        PRIMARY KEY,
    usuario_id       UUID        NOT NULL,
    crm              VARCHAR(15) NOT NULL,
    uf_crm           VARCHAR(2)  NOT NULL,
    especialidade_id UUID        NOT NULL,
    ativo            BOOLEAN     NOT NULL DEFAULT TRUE,
    criado_em        TIMESTAMPTZ NOT NULL,
    atualizado_em    TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_cad_medico_usuario UNIQUE (usuario_id),
    CONSTRAINT uk_cad_medico_crm_uf  UNIQUE (crm, uf_crm),
    CONSTRAINT fk_cad_medico_especialidade FOREIGN KEY (especialidade_id) REFERENCES cad_especialidade (id)
);

CREATE INDEX idx_cad_medico_especialidade ON cad_medico (especialidade_id);
CREATE INDEX idx_cad_medico_ativo         ON cad_medico (ativo);
