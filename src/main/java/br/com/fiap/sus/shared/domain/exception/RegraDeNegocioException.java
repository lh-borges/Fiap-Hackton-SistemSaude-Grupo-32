package br.com.fiap.sus.shared.domain.exception;

/** Entrada sintaticamente valida que viola uma regra de negocio. Responde 422. */
public class RegraDeNegocioException extends DominioException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
