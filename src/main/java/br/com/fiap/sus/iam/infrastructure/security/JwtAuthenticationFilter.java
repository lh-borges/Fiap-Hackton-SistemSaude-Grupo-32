package br.com.fiap.sus.iam.infrastructure.security;

import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Le o token do header Authorization e popula o contexto de seguranca. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIXO = "Bearer ";

    private final JwtTokenAdapter tokens;

    public JwtAuthenticationFilter(JwtTokenAdapter tokens) {
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta,
                                    FilterChain cadeia) throws ServletException, IOException {
        String cabecalho = requisicao.getHeader(HEADER);
        if (cabecalho != null && cabecalho.startsWith(PREFIXO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            tokens.validar(cabecalho.substring(PREFIXO.length()).trim())
                    .ifPresent(usuario -> autenticar(usuario, requisicao));
        }
        cadeia.doFilter(requisicao, resposta);
    }

    private void autenticar(UsuarioAutenticado usuario, HttpServletRequest requisicao) {
        List<SimpleGrantedAuthority> autoridades = usuario.roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        UsernamePasswordAuthenticationToken autenticacao =
                new UsernamePasswordAuthenticationToken(usuario, null, autoridades);
        autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(requisicao));
        SecurityContextHolder.getContext().setAuthentication(autenticacao);
    }
}
