package br.com.fiap.sus.iam.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Erros levantados dentro da cadeia de filtros nao passam pelo @RestControllerAdvice.
 * Este componente garante que 401 e 403 tambem saiam em RFC 7807 (Artigo VIII.2).
 */
@Component
public class RespostaDeErroDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RespostaDeErroDeSeguranca(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest requisicao, HttpServletResponse resposta,
                         AuthenticationException excecao) throws IOException {
        escrever(requisicao, resposta, HttpStatus.UNAUTHORIZED, "Nao autenticado",
                "Informe um token valido no header Authorization.", "nao-autenticado");
    }

    @Override
    public void handle(HttpServletRequest requisicao, HttpServletResponse resposta,
                       AccessDeniedException excecao) throws IOException {
        escrever(requisicao, resposta, HttpStatus.FORBIDDEN, "Acesso negado",
                "Seu perfil nao permite esta operacao.", "acesso-negado");
    }

    private void escrever(HttpServletRequest requisicao, HttpServletResponse resposta, HttpStatus status,
                          String titulo, String detalhe, String tipo) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setType(URI.create("https://api.sus.local/erros/" + tipo));
        problema.setInstance(URI.create(requisicao.getRequestURI()));

        resposta.setStatus(status.value());
        resposta.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resposta.getOutputStream(), problema);
    }
}
