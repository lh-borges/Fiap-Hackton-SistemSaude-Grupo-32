-- Carga de demonstracao. UUID deterministico (apenas digitos hexadecimais) para uso
-- direto na colecao do Postman.
INSERT INTO cad_especialidade (id, nome, descricao, ativo, criado_em, atualizado_em) VALUES
    ('00000000-0000-0000-0000-0000000000e1', 'Clinica Geral',   'Atendimento clinico geral',             TRUE, NOW(), NOW()),
    ('00000000-0000-0000-0000-0000000000e2', 'Cardiologia',     'Doencas do coracao e sistema vascular', TRUE, NOW(), NOW()),
    ('00000000-0000-0000-0000-0000000000e3', 'Ortopedia',       'Sistema musculoesqueletico',            TRUE, NOW(), NOW()),
    ('00000000-0000-0000-0000-0000000000e4', 'Pediatria',       'Atendimento infantil',                  TRUE, NOW(), NOW());

INSERT INTO cad_unidade_saude (id, nome, cnes, telefone, ativo, criado_em, atualizado_em) VALUES
    ('00000000-0000-0000-0000-0000000000b1', 'UBS Vila Mariana',         '1234567', '1130001000', TRUE, NOW(), NOW()),
    ('00000000-0000-0000-0000-0000000000b2', 'Hospital Municipal Leste', '7654321', '1130002000', TRUE, NOW(), NOW());

INSERT INTO cad_tipo_exame (id, nome, categoria, preparo, ativo, criado_em, atualizado_em) VALUES
    ('00000000-0000-0000-0000-0000000000c1', 'Hemograma completo',         'LABORATORIAL', 'Jejum de 8 horas.',                TRUE, NOW(), NOW()),
    ('00000000-0000-0000-0000-0000000000c2', 'Glicemia de jejum',          'LABORATORIAL', 'Jejum de 8 a 12 horas.',           TRUE, NOW(), NOW()),
    ('00000000-0000-0000-0000-0000000000c3', 'Raio-X de torax',            'IMAGEM',       'Retirar objetos metalicos.',       TRUE, NOW(), NOW()),
    ('00000000-0000-0000-0000-0000000000c4', 'Ultrassonografia abdominal', 'IMAGEM',       'Jejum de 6 horas e bexiga cheia.', TRUE, NOW(), NOW());
