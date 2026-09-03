-- Modulo iam: identidade, credenciais e perfis de autorizacao.
CREATE TABLE iam_usuario (
    id            UUID         PRIMARY KEY,
    nome          VARCHAR(150) NOT NULL,
    cpf           VARCHAR(11)  NOT NULL,
    email         VARCHAR(150) NOT NULL,
    senha         VARCHAR(100) NOT NULL,
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ  NOT NULL,
    atualizado_em TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_iam_usuario_cpf   UNIQUE (cpf),
    CONSTRAINT uk_iam_usuario_email UNIQUE (email)
);

CREATE INDEX idx_iam_usuario_ativo ON iam_usuario (ativo);
CREATE INDEX idx_iam_usuario_nome  ON iam_usuario (LOWER(nome));

CREATE TABLE iam_role (
    id   UUID        PRIMARY KEY,
    nome VARCHAR(30) NOT NULL,
    CONSTRAINT uk_iam_role_nome UNIQUE (nome),
    CONSTRAINT ck_iam_role_nome CHECK (nome IN ('ADMINISTRADOR', 'ATENDENTE', 'MEDICO', 'PACIENTE'))
);

CREATE TABLE iam_usuario_role (
    usuario_id UUID NOT NULL,
    role_id    UUID NOT NULL,
    CONSTRAINT pk_iam_usuario_role PRIMARY KEY (usuario_id, role_id),
    CONSTRAINT fk_iam_usuario_role_usuario FOREIGN KEY (usuario_id) REFERENCES iam_usuario (id),
    CONSTRAINT fk_iam_usuario_role_role    FOREIGN KEY (role_id)    REFERENCES iam_role (id)
);

CREATE INDEX idx_iam_usuario_role_role ON iam_usuario_role (role_id);
