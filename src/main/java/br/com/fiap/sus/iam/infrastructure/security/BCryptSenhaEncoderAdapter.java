package br.com.fiap.sus.iam.infrastructure.security;

import br.com.fiap.sus.iam.application.port.SenhaEncoderPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Implementacao da porta de senha com BCrypt. */
@Component
public class BCryptSenhaEncoderAdapter implements SenhaEncoderPort {

    private final PasswordEncoder encoder;

    public BCryptSenhaEncoderAdapter(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String codificar(String senhaEmTexto) {
        return encoder.encode(senhaEmTexto);
    }

    @Override
    public boolean confere(String senhaEmTexto, String hashArmazenado) {
        return encoder.matches(senhaEmTexto, hashArmazenado);
    }
}
