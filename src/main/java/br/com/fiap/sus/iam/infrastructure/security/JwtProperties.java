package br.com.fiap.sus.iam.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Configuracao do token. O segredo vem de variavel de ambiente (Artigo IV.5). */
@Component
@ConfigurationProperties(prefix = "sus.security.jwt")
public class JwtProperties {

    /** Minimo de 32 caracteres para HS256. */
    private String secret;
    private long expiracaoMs = 28_800_000L;
    private String emissor = "sus-plataforma";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpiracaoMs() {
        return expiracaoMs;
    }

    public void setExpiracaoMs(long expiracaoMs) {
        this.expiracaoMs = expiracaoMs;
    }

    public String getEmissor() {
        return emissor;
    }

    public void setEmissor(String emissor) {
        this.emissor = emissor;
    }
}
