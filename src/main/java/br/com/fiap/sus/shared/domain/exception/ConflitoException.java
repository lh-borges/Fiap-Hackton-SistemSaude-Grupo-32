package br.com.fiap.sus.shared.domain.exception;

/** Violacao de unicidade ou de estado esperado do recurso. Responde 409. */
public class ConflitoException extends DominioException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
