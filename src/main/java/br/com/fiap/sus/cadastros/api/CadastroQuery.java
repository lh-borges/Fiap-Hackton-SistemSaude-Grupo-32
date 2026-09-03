package br.com.fiap.sus.cadastros.api;

import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.cadastros.api.PacienteResumo;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de leitura publicada pelo modulo cadastros. E por aqui que consultas, exames,
 * resultados, receitas e documentos validam suas referencias, ja que nao existe FK
 * entre tabelas de modulos diferentes (Artigo III.3).
 */
public interface CadastroQuery {

    boolean pacienteAtivoExiste(UUID pacienteId);

    boolean medicoAtivoExiste(UUID medicoId);

    boolean unidadeAtivaExiste(UUID unidadeSaudeId);

    boolean tipoExameAtivoExiste(UUID tipoExameId);

    /** Devolve IMAGEM ou LABORATORIAL; usado pelo modulo de resultados (feature 005, RN-02). */
    Optional<String> categoriaDoTipoExame(UUID tipoExameId);

    /** Resolve o vinculo usuario -> paciente, base da verificacao de posse do dado. */
    Optional<UUID> pacienteIdDoUsuario(UUID usuarioId);

    Optional<UUID> medicoIdDoUsuario(UUID usuarioId);

    Optional<PacienteResumo> resumoDoPaciente(UUID pacienteId);

    Optional<MedicoResumo> resumoDoMedico(UUID medicoId);
}
