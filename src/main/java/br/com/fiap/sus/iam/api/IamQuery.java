package br.com.fiap.sus.iam.api;

import br.com.fiap.sus.iam.api.UsuarioResumo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de leitura publicada pelo modulo iam (Artigo III.3). E o unico caminho pelo qual
 * outro modulo pode consultar dados de usuario; nao existe FK para iam_usuario.
 */
public interface IamQuery {

    Optional<UsuarioResumo> resumoDoUsuario(UUID usuarioId);

    boolean usuarioAtivoExiste(UUID usuarioId);

    /** Usado por cadastros para aplicar a RN-04: paciente exige role PACIENTE, medico exige MEDICO. */
    boolean usuarioAtivoPossuiRole(UUID usuarioId, String role);

    /**
     * Ids de usuarios cujo nome, e-mail ou CPF casam com o termo. Permite que outros
     * modulos ofereçam busca textual sem fazer join com as tabelas de iam.
     */
    List<UUID> idsDeUsuariosPorTermo(String termo, int limite);
}
