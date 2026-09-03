package br.com.fiap.sus.iam.domain.exception;

import br.com.fiap.sus.shared.domain.exception.NaoAutorizadoException;

/**
 * EX-01: a mesma resposta e devolvida para e-mail inexistente, senha errada e usuario
 * inativo, para nao revelar quais e-mails existem na base.
 */
public class CredenciaisInvalidasException extends NaoAutorizadoException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha invalidos.");
    }
}
