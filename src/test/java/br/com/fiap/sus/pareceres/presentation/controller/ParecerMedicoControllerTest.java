package br.com.fiap.sus.pareceres.presentation.controller;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.iam.api.UsuarioResumo;
import br.com.fiap.sus.pareceres.application.service.ParecerLeituraService;
import br.com.fiap.sus.pareceres.application.usecase.ConsultarParecerUseCase;
import br.com.fiap.sus.pareceres.application.usecase.ListarPareceresUseCase;
import br.com.fiap.sus.pareceres.application.usecase.RegistrarParecerUseCase;
import br.com.fiap.sus.pareceres.application.usecase.RejeitarAlteracaoParecerUseCase;
import br.com.fiap.sus.pareceres.domain.model.ParecerMedico;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoFiltro;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoRepository;
import br.com.fiap.sus.resultados.api.ResultadoQuery;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import br.com.fiap.sus.shared.presentation.ApiExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ParecerMedicoControllerTest.Config.class)
class ParecerMedicoControllerTest {
    static final String ROTA = "/api/v1/pareceres";
    @Autowired ParecerMedicoController controller;
    @Autowired ParecerMedicoRepository repository;
    @Autowired CadastroQuery cadastros;
    @Autowired ResultadoQuery resultados;
    @Autowired ConsultaQuery consultas;
    @Autowired IamQuery iam;
    MockMvc mvc;
    final ObjectMapper json = new ObjectMapper();
    final UUID usuario = UUID.randomUUID();
    final UUID paciente = UUID.randomUUID();
    final UUID medico = UUID.randomUUID();
    final UUID resultado = UUID.randomUUID();
    final UUID especialidade = UUID.randomUUID();
    final ParecerMedico parecer = ParecerMedico.emitir(resultado, paciente, medico, "Interpretacao clinica registrada.");

    @Configuration
    @EnableMethodSecurity
    @Import({ParecerMedicoController.class, RegistrarParecerUseCase.class, ConsultarParecerUseCase.class,
            ListarPareceresUseCase.class, RejeitarAlteracaoParecerUseCase.class,
            ParecerLeituraService.class, UsuarioAutenticadoProvider.class})
    static class Config {
        @Bean ParecerMedicoRepository repository() { return mock(ParecerMedicoRepository.class); }
        @Bean CadastroQuery cadastros() { return mock(CadastroQuery.class); }
        @Bean ResultadoQuery resultados() { return mock(ResultadoQuery.class); }
        @Bean ConsultaQuery consultas() { return mock(ConsultaQuery.class); }
        @Bean IamQuery iam() { return mock(IamQuery.class); }
    }

    @BeforeEach
    void configurar() {
        reset(repository, cadastros, resultados, consultas, iam);
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @AfterEach
    void limparContexto() { SecurityContextHolder.clearContext(); }

    void autenticar(String role) {
        var principal = new UsuarioAutenticado(usuario, "Dra. Maria", Set.of(role));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, AuthorityUtils.createAuthorityList("ROLE_" + role)));
    }

    void configurarAutor() {
        when(cadastros.medicoIdDoUsuario(usuario)).thenReturn(Optional.of(medico));
        when(cadastros.resumoDoMedico(medico)).thenReturn(Optional.of(
                new MedicoResumo(medico, usuario, "Dra. Maria", "teste@example.org",
                        "12345", "SP", especialidade, "SP", true)));
        when(iam.resumoDoUsuario(usuario)).thenReturn(Optional.of(
                new UsuarioResumo(usuario, "Dra. Maria", "111.444.777-35",
                        "teste@example.org", true, Set.of("MEDICO"))));
    }

    void configurarPaciente() {
        autenticar("PACIENTE");
        when(cadastros.pacienteIdDoUsuario(usuario)).thenReturn(Optional.of(paciente));
        when(repository.buscarPorId(parecer.getId())).thenReturn(Optional.of(parecer));
    }

    String requestValido() throws Exception {
        return json.writeValueAsString(Map.of("resultadoExameId", resultado, "descricao", "Interpretacao clinica registrada."));
    }

    @Test
    void criaParecerComLocationSemAceitarTitularAutorOuDataDoCliente() throws Exception {
        autenticar("MEDICO");
        configurarAutor();
        when(resultados.pacienteIdDoResultado(resultado)).thenReturn(Optional.of(paciente));
        when(repository.salvar(any(ParecerMedico.class), eq(usuario))).thenAnswer(invocation -> invocation.getArgument(0));
        String body = json.writeValueAsString(Map.of("resultadoExameId", resultado, "descricao", "Interpretacao clinica registrada.",
                "pacienteId", UUID.randomUUID(), "medicoId", UUID.randomUUID(), "dataParecer", "2000-01-01T00:00:00Z"));
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith(ROTA + "/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.pacienteId").value(paciente.toString()))
                .andExpect(jsonPath("$.medicoId").value(medico.toString()))
                .andExpect(jsonPath("$.resultadoExameId").value(resultado.toString()))
                .andExpect(jsonPath("$.medicoNome").value("Dra. Maria"))
                .andExpect(jsonPath("$.medicoCrm").value("12345"))
                .andExpect(jsonPath("$.dataParecer").exists());
        var salvo = ArgumentCaptor.forClass(ParecerMedico.class);
        verify(repository).salvar(salvo.capture(), eq(usuario));
        assertThat(salvo.getValue().getDataParecer()).isAfter(java.time.Instant.parse("2000-01-01T00:00:00Z"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PACIENTE", "ATENDENTE", "ADMINISTRADOR"})
    void perfilNaoMedicoNaoEmite(String role) throws Exception {
        autenticar(role);
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(requestValido()))
                .andExpect(status().isForbidden());
        verifyNoInteractions(repository, resultados, cadastros);
    }

    @Test
    void semAutenticacaoNaoConsulta() throws Exception {
        mvc.perform(get(ROTA + "/" + parecer.getId())).andExpect(status().isUnauthorized());
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaEntradaSemResultadoEDescricao() throws Exception {
        autenticar("MEDICO");
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors").isArray());
        verifyNoInteractions(repository, resultados);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "curta"})
    void rejeitaDescricaoInvalida(String descricao) throws Exception {
        autenticar("MEDICO");
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("resultadoExameId", resultado, "descricao", descricao))))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaDescricaoAcimaDoLimite() throws Exception {
        autenticar("MEDICO");
        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("resultadoExameId", resultado, "descricao", "a".repeat(5001)))))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void pacienteConsultaProprioParecerComRespostaCompleta() throws Exception {
        configurarPaciente();
        configurarAutor();
        mvc.perform(get(ROTA + "/" + parecer.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value(parecer.getDescricao()))
                .andExpect(jsonPath("$.medicoNome").value("Dra. Maria"))
                .andExpect(jsonPath("$.idEspecialidade").value(especialidade.toString()));
    }

    @Test
    void pacienteDeTerceiroRecebe404() throws Exception {
        configurarPaciente();
        when(cadastros.pacienteIdDoUsuario(usuario)).thenReturn(Optional.of(UUID.randomUUID()));
        mvc.perform(get(ROTA + "/" + parecer.getId())).andExpect(status().isNotFound());
    }

    @Test
    void administradorRecebeMetadadosSemDescricao() throws Exception {
        autenticar("ADMINISTRADOR");
        configurarAutor();
        when(repository.buscarPorId(parecer.getId())).thenReturn(Optional.of(parecer));
        mvc.perform(get(ROTA + "/" + parecer.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(parecer.getId().toString()))
                .andExpect(jsonPath("$.descricao").doesNotExist());
    }

    @Test
    void atendenteNaoConsultaNemLista() throws Exception {
        autenticar("ATENDENTE");
        mvc.perform(get(ROTA)).andExpect(status().isForbidden());
        mvc.perform(get(ROTA + "/" + parecer.getId())).andExpect(status().isForbidden());
        verifyNoInteractions(repository);
    }

    @Test
    void listaPaginadaComFiltrosRestringePacienteAutenticado() throws Exception {
        configurarPaciente();
        configurarAutor();
        when(repository.listar(any(), eq(1), eq(5))).thenReturn(PaginaResultado.de(List.of(parecer), 1, 5, 6));
        mvc.perform(get(ROTA).param("pacienteId", UUID.randomUUID().toString()).param("medicoId", medico.toString())
                        .param("resultadoExameId", resultado.toString()).param("pagina", "1").param("tamanho", "5")
                        .param("periodoInicio", "2026-09-01T00:00:00Z").param("periodoFim", "2026-09-30T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].medicoNome").value("Dra. Maria"))
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.tamanho").value(5))
                .andExpect(jsonPath("$.totalElementos").value(6))
                .andExpect(jsonPath("$.totalPaginas").value(2));
        var filtro = ArgumentCaptor.forClass(ParecerMedicoFiltro.class);
        verify(repository).listar(filtro.capture(), eq(1), eq(5));
        assertThat(filtro.getValue().pacienteId()).isEqualTo(paciente);
        assertThat(filtro.getValue().medicoId()).isEqualTo(medico);
        assertThat(filtro.getValue().resultadoExameId()).isEqualTo(resultado);
        assertThat(filtro.getValue().periodoInicio()).isEqualTo(java.time.Instant.parse("2026-09-01T00:00:00Z"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "101"})
    void rejeitaTamanhoInvalido(String tamanho) throws Exception {
        autenticar("PACIENTE");
        mvc.perform(get(ROTA).param("tamanho", tamanho)).andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaPaginaNegativaEPeriodoInvertido() throws Exception {
        autenticar("PACIENTE");
        mvc.perform(get(ROTA).param("pagina", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get(ROTA).param("periodoInicio", "2026-09-30T00:00:00Z").param("periodoFim", "2026-09-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUT", "PATCH", "DELETE"})
    void rejeitaMutacoesComExplicacao(String metodo) throws Exception {
        configurarPaciente();
        mvc.perform(request(HttpMethod.valueOf(metodo), ROTA + "/" + parecer.getId()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("imutável")));
        verify(repository, never()).salvar(any(ParecerMedico.class), any(UUID.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUT", "PATCH", "DELETE"})
    void mutacaoDeTerceiroNaoRevelaExistencia(String metodo) throws Exception {
        configurarPaciente();
        when(cadastros.pacienteIdDoUsuario(usuario)).thenReturn(Optional.of(UUID.randomUUID()));
        mvc.perform(request(HttpMethod.valueOf(metodo), ROTA + "/" + parecer.getId())).andExpect(status().isNotFound());
        verify(repository, never()).salvar(any(ParecerMedico.class), any(UUID.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUT", "PATCH", "DELETE"})
    void atendenteNaoAcessaRotasDeMutacao(String metodo) throws Exception {
        autenticar("ATENDENTE");
        mvc.perform(request(HttpMethod.valueOf(metodo), ROTA + "/" + parecer.getId())).andExpect(status().isForbidden());
        verifyNoInteractions(repository);
    }
}
