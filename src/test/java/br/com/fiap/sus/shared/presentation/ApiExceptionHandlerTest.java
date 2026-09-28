package br.com.fiap.sus.shared.presentation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import static org.assertj.core.api.Assertions.*;

@DisplayName("ApiExceptionHandler (RFC 7807)")
class ApiExceptionHandlerTest {
    final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    @DisplayName("URL inexistente responde 404, nao 500")
    void rotaInexistenteResponde404() {
        var req = new MockHttpServletRequest("GET", "/swagger-ui/index.html.");
        var problema = handler.tratarRotaInexistente(
                new NoResourceFoundException(HttpMethod.GET, "swagger-ui/index.html."), req);
        assertThat(problema.getStatus()).isEqualTo(404);
        assertThat(problema.getTitle()).isEqualTo("Recurso nao encontrado");
        assertThat(problema.getType().toString()).endsWith("/nao-encontrado");
        assertThat(problema.getInstance().toString()).isEqualTo("/swagger-ui/index.html.");
    }

    @Test
    @DisplayName("erro inesperado responde 500 sem vazar a causa")
    void inesperadoResponde500SemDetalhe() {
        var req = new MockHttpServletRequest("GET", "/api/v1/qualquer");
        var problema = handler.tratarInesperado(new IllegalStateException("segredo interno"), req);
        assertThat(problema.getStatus()).isEqualTo(500);
        assertThat(problema.getDetail()).doesNotContain("segredo");
    }
}
