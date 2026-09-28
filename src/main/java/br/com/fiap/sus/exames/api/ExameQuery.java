package br.com.fiap.sus.exames.api;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de leitura publicada pelo modulo exames, seguindo o mesmo padrao de
 * CadastroQuery (Artigo III.3). O modulo resultados usa isto para validar
 * RN-01 (exame realizado) e RN-02 (categoria do tipo de exame), sem acessar
 * ExameRepository/SolicitacaoExameRepository diretamente.
 */
public interface ExameQuery {

    boolean exameRealizadoExiste(UUID exameId);

    Optional<Instant> dataRealizacaoDoExame(UUID exameId);

    Optional<UUID> pacienteIdDoExame(UUID exameId);

    /** Resolve, a partir do exame, o tipo de exame da solicitacao de origem. */
    Optional<UUID> tipoExameIdDoExame(UUID exameId);

    /** Medico que solicitou o exame; usado por resultados para notificar quem pediu (feature 009). */
    Optional<UUID> medicoSolicitanteIdDoExame(UUID exameId);

    /** Solicitacoes do paciente, inclusive canceladas, da mais recente para a mais antiga (feature 010). */
    List<SolicitacaoExameResumo> solicitacoesDoPaciente(UUID pacienteId);

    /** Exames do paciente (via solicitacao), inclusive cancelados, do mais recente para o mais antigo (feature 010). */
    List<ExameResumo> examesDoPaciente(UUID pacienteId);
}
