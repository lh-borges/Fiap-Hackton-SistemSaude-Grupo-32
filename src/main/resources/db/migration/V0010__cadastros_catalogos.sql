-- Modulo cadastros: catalogos de apoio.
CREATE TABLE cad_especialidade (
    id            UUID         PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL,
    descricao     VARCHAR(255),
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ  NOT NULL,
    atualizado_em TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_cad_especialidade_nome UNIQUE (nome)
);

CREATE TABLE cad_unidade_saude (
    id            UUID         PRIMARY KEY,
    nome          VARCHAR(150) NOT NULL,
    cnes          VARCHAR(7)   NOT NULL,
    telefone      VARCHAR(20),
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ  NOT NULL,
    atualizado_em TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_cad_unidade_saude_cnes UNIQUE (cnes)
);

CREATE TABLE cad_tipo_exame (
    id            UUID         PRIMARY KEY,
    nome          VARCHAR(150) NOT NULL,
    categoria     VARCHAR(15)  NOT NULL,
    preparo       TEXT,
    ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em     TIMESTAMPTZ  NOT NULL,
    atualizado_em TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_cad_tipo_exame_nome UNIQUE (nome),
    CONSTRAINT ck_cad_tipo_exame_categoria CHECK (categoria IN ('IMAGEM', 'LABORATORIAL'))
);
