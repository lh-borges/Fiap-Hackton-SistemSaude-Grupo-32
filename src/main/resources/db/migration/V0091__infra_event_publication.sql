-- Registro transacional de publicacao de eventos do Spring Modulith (Artigo VI.1).
-- Copiado de schema-postgresql.sql do artefato spring-modulith-events-jdbc:1.4.3.
-- O Flyway e a unica fonte do schema (Artigo V.1); a inicializacao automatica da
-- biblioteca fica desligada em application.yml.
CREATE TABLE event_publication (
    id               UUID NOT NULL,
    listener_id      TEXT NOT NULL,
    event_type       TEXT NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date  TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);

CREATE INDEX event_publication_serialized_event_hash_idx ON event_publication USING hash (serialized_event);
CREATE INDEX event_publication_by_completion_date_idx ON event_publication (completion_date);
