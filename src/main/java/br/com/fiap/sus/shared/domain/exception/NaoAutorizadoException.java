package br.com.fiap.sus.shared.domain.exception;

/** Falha de autenticacao: credencial ausente, invalida ou expirada. Responde 401. */
public class NaoAutorizadoException extends DominioException {

    public NaoAutorizadoException(String mensagem) {
        super(mensagem);
    }
}
