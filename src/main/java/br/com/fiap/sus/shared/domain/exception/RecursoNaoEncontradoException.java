package br.com.fiap.sus.shared.domain.exception;

/**
 * Recurso inexistente ou de terceiro. Tambem e usada quando a propria existencia do
 * registro e informacao sensivel (Artigo IV.7): responde 404 em vez de 403.
 */
public class RecursoNaoEncontradoException extends DominioException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public static RecursoNaoEncontradoException de(String recurso) {
        return new RecursoNaoEncontradoException(recurso + " nao encontrado.");
    }
}
