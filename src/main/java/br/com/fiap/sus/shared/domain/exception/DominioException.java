package br.com.fiap.sus.shared.domain.exception;

/** Raiz das excecoes de negocio. Nenhuma delas carrega dado clinico na mensagem. */
public abstract class DominioException extends RuntimeException {

    protected DominioException(String mensagem) {
        super(mensagem);
    }
}
