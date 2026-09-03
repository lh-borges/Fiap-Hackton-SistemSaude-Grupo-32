package br.com.fiap.sus.iam.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracao de seguranca (Artigo IV).
 * Nega por padrao: apenas login, documentacao e health sao publicos.
 * A autorizacao fina fica no caso de uso, com @PreAuthorize.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] ROTAS_PUBLICAS = {
            "/api/v1/auth/login",
            "/v3/api-docs", "/v3/api-docs/**",
            "/swagger-ui.html", "/swagger-ui/**",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                                           RespostaDeErroDeSeguranca erros) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(ROTAS_PUBLICAS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint(erros)
                        .accessDeniedHandler(erros))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
