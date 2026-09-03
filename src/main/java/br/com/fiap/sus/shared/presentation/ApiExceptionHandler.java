package br.com.fiap.sus.shared.presentation;

import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import br.com.fiap.sus.shared.domain.exception.NaoAutorizadoException;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Tratamento unico de erro no formato RFC 7807 (Artigo VIII.2).
 * Nenhuma mensagem devolvida daqui revela dado pessoal ou clinico.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String BASE_TIPO = "https://api.sus.local/erros/";

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail tratarNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return problema(HttpStatus.NOT_FOUND, "Recurso nao encontrado", ex.getMessage(), "nao-encontrado", req);
    }

    @ExceptionHandler(ConflitoException.class)
    public ProblemDetail tratarConflito(ConflitoException ex, HttpServletRequest req) {
        return problema(HttpStatus.CONFLICT, "Conflito de dados", ex.getMessage(), "conflito", req);
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail tratarRegraDeNegocio(RegraDeNegocioException ex, HttpServletRequest req) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negocio violada", ex.getMessage(),
                "regra-de-negocio", req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErroCampo> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroCampo(erro.getField(), erro.getDefaultMessage()))
                .toList();
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Requisicao invalida",
                "Um ou mais campos estao invalidos.", "validacao", req);
        problema.setProperty("errors", erros);
        return problema;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class})
    public ProblemDetail tratarEntradaMalFormada(Exception ex, HttpServletRequest req) {
        return problema(HttpStatus.BAD_REQUEST, "Requisicao invalida",
                "Nao foi possivel interpretar a requisicao.", "entrada-invalida", req);
    }

    @ExceptionHandler(NaoAutorizadoException.class)
    public ProblemDetail tratarNaoAutorizado(NaoAutorizadoException ex, HttpServletRequest req) {
        return problema(HttpStatus.UNAUTHORIZED, "Nao autenticado", ex.getMessage(), "nao-autenticado", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail tratarAutenticacao(AuthenticationException ex, HttpServletRequest req) {
        return problema(HttpStatus.UNAUTHORIZED, "Nao autenticado",
                "Credenciais ausentes ou invalidas.", "nao-autenticado", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail tratarAcessoNegado(AccessDeniedException ex, HttpServletRequest req) {
        return problema(HttpStatus.FORBIDDEN, "Acesso negado",
                "Seu perfil nao permite esta operacao.", "acesso-negado", req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail tratarIntegridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violacao de integridade em {} {}", req.getMethod(), req.getRequestURI());
        return problema(HttpStatus.CONFLICT, "Conflito de dados",
                "A operacao viola uma restricao de unicidade ou de relacionamento.", "conflito", req);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarInesperado(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente.", "erro-interno", req);
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe, String tipo,
                                   HttpServletRequest req) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        problema.setType(URI.create(BASE_TIPO + tipo));
        problema.setInstance(URI.create(req.getRequestURI()));
        return problema;
    }
}
