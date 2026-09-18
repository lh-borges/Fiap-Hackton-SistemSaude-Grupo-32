-- Nao altera V0050: preserva os checksums dos ambientes existentes.
ALTER TABLE par_parecer_medico ADD COLUMN criado_por_usuario_id UUID;
UPDATE par_parecer_medico p
SET criado_por_usuario_id = (SELECT m.usuario_id FROM cad_medico m WHERE m.id = p.medico_id);
-- Legados sem cadastro identificavel permanecem NULL; a aplicacao exige autor nas novas insercoes.
CREATE INDEX idx_par_paciente_data ON par_parecer_medico (paciente_id, data_parecer DESC);
