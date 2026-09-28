package br.com.fiap.sus.notificacoes.presentation.controller;

import br.com.fiap.sus.notificacoes.application.usecase.ConsultarMinhaNotificacaoUseCase;
import br.com.fiap.sus.notificacoes.application.usecase.ContarNaoLidasUseCase;
import br.com.fiap.sus.notificacoes.application.usecase.ListarMinhasNotificacoesUseCase;
import br.com.fiap.sus.notificacoes.application.usecase.MarcarNotificacaoComoLidaUseCase;
import br.com.fiap.sus.notificacoes.domain.enums.TipoNotificacao;
import br.com.fiap.sus.notificacoes.domain.model.Notificacao;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoFiltro;
import br.com.fiap.sus.notificacoes.domain.repository.NotificacaoRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import br.com.fiap.sus.shared.presentation.ApiExceptionHandler;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = NotificacaoControllerTest.Config.class)
@DisplayName("GET/PATCH /api/v1/notificacoes")
class NotificacaoControllerTest {
    static final String ROTA = "/api/v1/notificacoes";
    @Autowired NotificacaoController controller;
    @Autowired NotificacaoRepository repository;
    MockMvc mvc;
    final UUID usuario = UUID.randomUUID();
    final UUID outroUsuario = UUID.randomUUID();

    @Configuration
    @EnableMethodSecurity
    @Import({NotificacaoController.class, ListarMinhasNotificacoesUseCase.class, ConsultarMinhaNotificacaoUseCase.class,
            MarcarNotificacaoComoLidaUseCase.class, ContarNaoLidasUseCase.class, UsuarioAutenticadoProvider.class})
    static class Config {
        @Bean NotificacaoRepository repository() { return mock(NotificacaoRepository.class); }
    }

    @BeforeEach
    void configurar() {
        reset(repository);
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @AfterEach
    void limparContexto() { SecurityContextHolder.clearContext(); }

    void autenticar(String role) {
        var principal = new UsuarioAutenticado(usuario, "Usuario", Set.of(role));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, AuthorityUtils.createAuthorityList("ROLE_" + role)));
    }

    Notificacao notificacaoDe(UUID dono) {
        return Notificacao.criar(dono, UUID.randomUUID(), TipoNotificacao.RESULTADO_DISPONIVEL,
                "Resultado de exame disponivel", "O resultado do seu exame ja esta disponivel.", UUID.randomUUID());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PACIENTE", "MEDICO", "ATENDENTE", "ADMINISTRADOR"})
    @DisplayName("HU-02: qualquer perfil lista as proprias notificacoes, com filtro e paginacao")
    void listaAsProprias(String role) throws Exception {
        autenticar(role);
        var n = notificacaoDe(usuario);
        when(repository.listar(any(), eq(0), eq(20))).thenReturn(PaginaResultado.de(List.of(n), 0, 20, 1));

        mvc.perform(get(ROTA).param("lida", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].id").value(n.getId().toString()))
                .andExpect(jsonPath("$.conteudo[0].tipo").value("RESULTADO_DISPONIVEL"))
                .andExpect(jsonPath("$.conteudo[0].recurso").value("resultados-exame"))
                .andExpect(jsonPath("$.conteudo[0].referenciaId").value(n.getReferenciaId().toString()))
                .andExpect(jsonPath("$.conteudo[0].lida").value(false))
                .andExpect(jsonPath("$.conteudo[0].usuarioId").doesNotExist())
                .andExpect(jsonPath("$.conteudo[0].eventoId").doesNotExist());

        var filtro = ArgumentCaptor.forClass(NotificacaoFiltro.class);
        verify(repository).listar(filtro.capture(), eq(0), eq(20));
        assertThat(filtro.getValue().usuarioId()).isEqualTo(usuario);
        assertThat(filtro.getValue().lida()).isFalse();
    }

    @Test
    void tamanhoAcimaDoLimiteResponde400() throws Exception {
        autenticar("PACIENTE");
        mvc.perform(get(ROTA).param("tamanho", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("RF-05: contagem de nao lidas")
    void contagem() throws Exception {
        autenticar("MEDICO");
        when(repository.contarNaoLidas(usuario)).thenReturn(3L);
        mvc.perform(get(ROTA + "/nao-lidas/contagem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(3));
    }

    @Test
    @DisplayName("HU-06: detalhe com referencia e recurso de origem")
    void detalhe() throws Exception {
        autenticar("PACIENTE");
        var n = notificacaoDe(usuario);
        when(repository.buscarPorId(n.getId())).thenReturn(Optional.of(n));
        mvc.perform(get(ROTA + "/" + n.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(n.getId().toString()))
                .andExpect(jsonPath("$.recurso").value("resultados-exame"))
                .andExpect(jsonPath("$.titulo").value("Resultado de exame disponivel"));
    }

    @Test
    @DisplayName("RF-04: marca como lida e devolve a data de leitura")
    void marcaComoLida() throws Exception {
        autenticar("PACIENTE");
        var n = notificacaoDe(usuario);
        when(repository.buscarPorId(n.getId())).thenReturn(Optional.of(n));
        when(repository.salvar(n)).thenReturn(n);
        mvc.perform(patch(ROTA + "/" + n.getId() + "/leitura"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lida").value(true))
                .andExpect(jsonPath("$.dataLeitura").isNotEmpty());
        verify(repository).salvar(n);
    }

    @Test
    @DisplayName("HU-03/EX-04: notificacao de terceiro responde 404 em GET e PATCH, sem gravar")
    void deTerceiroResponde404() throws Exception {
        autenticar("ADMINISTRADOR");
        var alheia = notificacaoDe(outroUsuario);
        when(repository.buscarPorId(alheia.getId())).thenReturn(Optional.of(alheia));

        mvc.perform(get(ROTA + "/" + alheia.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mvc.perform(patch(ROTA + "/" + alheia.getId() + "/leitura"))
                .andExpect(status().isNotFound());
        verify(repository, never()).salvar(any());
        assertThat(alheia.isLida()).isFalse();
    }

    @Test
    @DisplayName("Artigo IV.3: sem autenticacao nenhuma rota responde")
    void semAutenticacaoResponde401() throws Exception {
        mvc.perform(get(ROTA)).andExpect(status().isUnauthorized());
        mvc.perform(get(ROTA + "/nao-lidas/contagem")).andExpect(status().isUnauthorized());
        mvc.perform(get(ROTA + "/" + UUID.randomUUID())).andExpect(status().isUnauthorized());
        mvc.perform(patch(ROTA + "/" + UUID.randomUUID() + "/leitura")).andExpect(status().isUnauthorized());
        verifyNoInteractions(repository);
    }
}
