package br.com.fiap.sus.exames.api;

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

    /** Resolve, a partir do exame, o tipo de exame da solicitacao de origem. */
    Optional<UUID> tipoExameIdDoExame(UUID exameId);
}