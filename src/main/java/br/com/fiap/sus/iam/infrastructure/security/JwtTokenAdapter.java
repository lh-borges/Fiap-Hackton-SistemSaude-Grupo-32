package br.com.fiap.sus.iam.infrastructure.security;

import br.com.fiap.sus.iam.application.port.TokenGerado;
import br.com.fiap.sus.iam.application.port.TokenPort;
import br.com.fiap.sus.iam.domain.model.Usuario;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Emissao e validacao do token JWT (HS256). */
@Component
public class JwtTokenAdapter implements TokenPort {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenAdapter.class);
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_NOME = "nome";

    private final SecretKey chave;
    private final long expiracaoMs;
    private final String emissor;

    public JwtTokenAdapter(JwtProperties propriedades) {
        String secret = propriedades.getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "sus.security.jwt.secret ausente ou menor que 32 caracteres. Defina JWT_SECRET.");
        }
        this.chave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = propriedades.getExpiracaoMs();
        this.emissor = propriedades.getEmissor();
    }

    @Override
    public TokenGerado gerarPara(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plusMillis(expiracaoMs);
        String token = Jwts.builder()
                .subject(usuario.getId().toString())
                .claim(CLAIM_NOME, usuario.getNome())
                .claim(CLAIM_ROLES, usuario.getRoles().stream().map(Enum::name).sorted().toList())
                .issuer(emissor)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiraEm))
                .signWith(chave)
                .compact();
        return new TokenGerado(token, expiraEm);
    }

    /** Devolve a identidade se o token for valido; vazio caso contrario. Nunca lanca para o filtro. */
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims corpo = Jwts.parser()
                    .verifyWith(chave)
                    .requireIssuer(emissor)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new UsuarioAutenticado(
                    UUID.fromString(corpo.getSubject()),
                    corpo.get(CLAIM_NOME, String.class),
                    extrairRoles(corpo)));
        } catch (JwtException | IllegalArgumentException ex) {
            // Sem dado do token no log: apenas o motivo.
            log.debug("Token rejeitado: {}", ex.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> extrairRoles(Claims corpo) {
        Object valor = corpo.get(CLAIM_ROLES);
        if (valor instanceof List<?> lista) {
            return new HashSet<>(((List<Object>) lista).stream().map(String::valueOf).toList());
        }
        return Set.of();
    }
}
