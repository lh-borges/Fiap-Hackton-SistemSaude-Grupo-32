package br.com.fiap.sus.documentos.presentation.controller;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.cadastros.api.PacienteResumo;
import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.documentos.application.service.DocumentoLeituraService;
import br.com.fiap.sus.documentos.application.service.DocumentoPdfService;
import br.com.fiap.sus.documentos.application.usecase.CancelarDocumentoUseCase;
import br.com.fiap.sus.documentos.application.usecase.ConsultarDocumentoUseCase;
import br.com.fiap.sus.documentos.application.usecase.EmitirDocumentoUseCase;
import br.com.fiap.sus.documentos.application.usecase.ListarDocumentosUseCase;
import br.com.fiap.sus.documentos.domain.enums.SituacaoDocumento;
import br.com.fiap.sus.documentos.domain.enums.TipoDocumento;
import br.com.fiap.sus.documentos.domain.model.DocumentoMedico;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoFiltro;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoRepository;
import br.com.fiap.sus.exames.api.ExameQuery;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import br.com.fiap.sus.shared.presentation.ApiExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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
import org.springframework.context.ApplicationEventPublisher;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DocumentoMedicoControllerTest.Config.class)
class DocumentoMedicoControllerTest {
    static final String ROTA = "/api/v1/documentos";

    @Autowired DocumentoMedicoController controller;
    @Autowired DocumentoMedicoRepository repository;
    @Autowired CadastroQuery cadastros;
    @Autowired ConsultaQuery consultas;
    @Autowired ExameQuery exames;
    @Autowired DocumentoPdfService pdfs;
    @Autowired ApplicationEventPublisher eventos;

    MockMvc mvc;
    final ObjectMapper json = new ObjectMapper();
    final UUID usuario = UUID.randomUUID();
    final UUID paciente = UUID.randomUUID();
    final UUID medico = UUID.randomUUID();
    final UUID consulta = UUID.randomUUID();
    final UUID exame = UUID.randomUUID();
    final UUID usuarioPaciente = UUID.randomUUID();
    final UUID usuarioMedico = UUID.randomUUID();
    final UUID especialidade = UUID.randomUUID();
    final DocumentoMedico documento = DocumentoMedico.emitir(TipoDocumento.ATESTADO,
            "Conteudo clinico registrado.", paciente, medico, consulta);

    @Configuration
    @EnableMethodSecurity
    @Import({DocumentoMedicoController.class, EmitirDocumentoUseCase.class, ConsultarDocumentoUseCase.class,
            ListarDocumentosUseCase.class, CancelarDocumentoUseCase.class, DocumentoLeituraService.class,
            UsuarioAutenticadoProvider.class})
    static class Config {
        @Bean DocumentoMedicoRepository repository() { return mock(DocumentoMedicoRepository.class); }
        @Bean CadastroQuery cadastros() { return mock(CadastroQuery.class); }
        @Bean ConsultaQuery consultas() { return mock(ConsultaQuery.class); }
        @Bean ExameQuery exames() { return mock(ExameQuery.class); }
        @Bean DocumentoPdfService pdfs() { return mock(DocumentoPdfService.class); }
        @Bean ApplicationEventPublisher eventos() { return mock(ApplicationEventPublisher.class); }
    }

    @BeforeEach
    void configurar() {
        reset(repository, cadastros, consultas, exames, pdfs, eventos);
        when(cadastros.resumoDoPaciente(paciente)).thenReturn(Optional.of(
                new PacienteResumo(paciente, usuarioPaciente, "Maria Souza", "111.444.777-35",
                        "maria.souza@sus.gov.br", true)));
        when(cadastros.resumoDoMedico(medico)).thenReturn(Optional.of(
                new MedicoResumo(medico, usuarioMedico, "Carlos Lima", "carlos.lima@sus.gov.br",
                        "123456", "SP", especialidade, "Clinica Geral", true)));
        when(consultas.dataHoraDaConsulta(consulta)).thenReturn(Optional.of(Instant.parse("2026-09-20T13:30:00Z")));
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiExceptionHandler()).build();
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    void autenticar(String role) {
        var principal = new UsuarioAutenticado(usuario, "Usuario Teste", Set.of(role));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, AuthorityUtils.createAuthorityList("ROLE_" + role)));
    }

    void configurarMedico() {
        autenticar("MEDICO");
        when(cadastros.medicoIdDoUsuario(usuario)).thenReturn(Optional.of(medico));
    }

    void configurarPaciente() {
        autenticar("PACIENTE");
        when(cadastros.pacienteIdDoUsuario(usuario)).thenReturn(Optional.of(paciente));
    }

    String emitirRequest() throws Exception {
        return json.writeValueAsString(Map.of(
                "pacienteId", paciente,
                "tipo", "ATESTADO",
                "conteudo", "Conteudo clinico registrado.",
                "consultaId", consulta
        ));
    }

    @Test
    void medicoEmiteDocumentoComLocationSemAceitarAutorOuDataDoCliente() throws Exception {
        configurarMedico();
        when(cadastros.pacienteAtivoExiste(paciente)).thenReturn(true);
        when(consultas.pacienteIdDaConsulta(consulta)).thenReturn(Optional.of(paciente));
        when(repository.salvar(any(DocumentoMedico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String body = json.writeValueAsString(Map.of(
                "pacienteId", paciente,
                "tipo", "ATESTADO",
                "conteudo", "Conteudo clinico registrado.",
                "consultaId", consulta,
                "medicoId", UUID.randomUUID(),
                "dataEmissao", "2000-01-01T00:00:00Z"
        ));

        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith(ROTA + "/")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.pacienteId").doesNotExist())
                .andExpect(jsonPath("$.pacienteNome").value("Maria Souza"))
                .andExpect(jsonPath("$.pacienteCpf").value("111.444.777-35"))
                .andExpect(jsonPath("$.medicoId").doesNotExist())
                .andExpect(jsonPath("$.medicoNome").value("Carlos Lima"))
                .andExpect(jsonPath("$.medicoEspecialidade").value("Clinica Geral"))
                .andExpect(jsonPath("$.medicoCrm").value("123456/SP"))
                .andExpect(jsonPath("$.consultaId").doesNotExist())
                .andExpect(jsonPath("$.dataConsulta").value("20/09/2026 10:30"))
                .andExpect(jsonPath("$.dadosEspecificos.tipo").value("ATESTADO"))
                .andExpect(jsonPath("$.dadosEspecificos.consultaId").doesNotExist())
                .andExpect(jsonPath("$.dadosEspecificos.dataConsulta").value("20/09/2026 10:30"))
                .andExpect(jsonPath("$.tipoDocumento").value("ATESTADO"))
                .andExpect(jsonPath("$.conteudo").value("Conteudo clinico registrado."))
                .andExpect(jsonPath("$.dataEmissao").exists())
                .andExpect(jsonPath("$.situacao").value("EMITIDO"));

        var salvo = ArgumentCaptor.forClass(DocumentoMedico.class);
        verify(repository).salvar(salvo.capture());
        assertThat(salvo.getValue().getMedicoId()).isEqualTo(medico);
        assertThat(salvo.getValue().getDataEmissao()).isAfter(Instant.parse("2000-01-01T00:00:00Z"));
    }

    @Test
    void medicoEmiteLaudoVinculadoAoExameComDataDeRealizacao() throws Exception {
        configurarMedico();
        when(cadastros.pacienteAtivoExiste(paciente)).thenReturn(true);
        when(consultas.pacienteIdDaConsulta(consulta)).thenReturn(Optional.of(paciente));
        when(exames.pacienteIdDoExame(exame)).thenReturn(Optional.of(paciente));
        when(exames.exameRealizadoExiste(exame)).thenReturn(true);
        when(exames.dataRealizacaoDoExame(exame)).thenReturn(Optional.of(Instant.parse("2026-09-20T12:00:00Z")));
        when(exames.tipoExameIdDoExame(exame)).thenReturn(Optional.of(UUID.fromString("00000000-0000-0000-0000-0000000000c3")));
        when(cadastros.nomeDoTipoExame(UUID.fromString("00000000-0000-0000-0000-0000000000c3")))
                .thenReturn(Optional.of("Raio-X de torax"));
        when(repository.salvar(any(DocumentoMedico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String body = json.writeValueAsString(Map.of(
                "pacienteId", paciente,
                "tipo", "LAUDO",
                "conteudo", "Laudo medico emitido com base no exame realizado.",
                "consultaId", consulta,
                "exameId", exame
        ));

        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoDocumento").value("LAUDO"))
                .andExpect(jsonPath("$.exameId").doesNotExist())
                .andExpect(jsonPath("$.tipoExameId").doesNotExist())
                .andExpect(jsonPath("$.tipoExameNome").value("Raio-X de torax"))
                .andExpect(jsonPath("$.dataConsulta").value("20/09/2026 10:30"))
                .andExpect(jsonPath("$.dataRealizacaoExame").value("20/09/2026 09:00"))
                .andExpect(jsonPath("$.dadosEspecificos.tipo").value("LAUDO_EXAME"))
                .andExpect(jsonPath("$.dadosEspecificos.exameId").doesNotExist())
                .andExpect(jsonPath("$.dadosEspecificos.tipoExameId").doesNotExist())
                .andExpect(jsonPath("$.dadosEspecificos.tipoExameNome").value("Raio-X de torax"))
                .andExpect(jsonPath("$.dadosEspecificos.dataConsulta").value("20/09/2026 10:30"))
                .andExpect(jsonPath("$.dadosEspecificos.dataRealizacaoExame").value("20/09/2026 09:00"));

        var salvo = ArgumentCaptor.forClass(DocumentoMedico.class);
        verify(repository).salvar(salvo.capture());
        assertThat(salvo.getValue().getExameId()).isEqualTo(exame);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PACIENTE", "ATENDENTE", "ADMINISTRADOR"})
    void perfilNaoMedicoNaoEmite(String role) throws Exception {
        autenticar(role);

        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(emitirRequest()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaConteudoInvalido() throws Exception {
        configurarMedico();
        String body = json.writeValueAsString(Map.of("pacienteId", paciente, "tipo", "ATESTADO",
                "conteudo", "curto"));

        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors").isArray());

        verifyNoInteractions(repository);
    }

    @Test
    void rejeitaTipoInvalidoListandoTiposValidos() throws Exception {
        configurarMedico();
        when(cadastros.pacienteAtivoExiste(paciente)).thenReturn(true);
        String body = json.writeValueAsString(Map.of("pacienteId", paciente, "tipo", "INVALIDO",
                "conteudo", "Conteudo clinico registrado."));

        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("ATESTADO")))
                .andExpect(jsonPath("$.detail", containsString("DECLARACAO")));
    }

    @Test
    void rejeitaConsultaDeOutroPaciente() throws Exception {
        configurarMedico();
        when(cadastros.pacienteAtivoExiste(paciente)).thenReturn(true);
        when(consultas.pacienteIdDaConsulta(consulta)).thenReturn(Optional.of(UUID.randomUUID()));

        mvc.perform(post(ROTA).contentType(MediaType.APPLICATION_JSON).content(emitirRequest()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("outro paciente")));
    }

    @Test
    void pacienteConsultaProprioDocumentoComConteudo() throws Exception {
        configurarPaciente();
        when(repository.buscarPorId(documento.getId())).thenReturn(Optional.of(documento));

        mvc.perform(get(ROTA + "/" + documento.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(documento.getId().toString()))
                .andExpect(jsonPath("$.pacienteNome").value("Maria Souza"))
                .andExpect(jsonPath("$.pacienteCpf").value("111.444.777-35"))
                .andExpect(jsonPath("$.medicoNome").value("Carlos Lima"))
                .andExpect(jsonPath("$.medicoEspecialidade").value("Clinica Geral"))
                .andExpect(jsonPath("$.medicoCrm").value("123456/SP"))
                .andExpect(jsonPath("$.conteudo").value(documento.getConteudo()))
                .andExpect(jsonPath("$.tipoDocumento").value("ATESTADO"));
    }

    @Test
    void pacienteDeTerceiroRecebe404() throws Exception {
        configurarPaciente();
        when(cadastros.pacienteIdDoUsuario(usuario)).thenReturn(Optional.of(UUID.randomUUID()));
        when(repository.buscarPorId(documento.getId())).thenReturn(Optional.of(documento));

        mvc.perform(get(ROTA + "/" + documento.getId())).andExpect(status().isNotFound());
    }

    @Test
    void listagemRetornaMetadadosSemConteudoEFiltraPacienteAutenticado() throws Exception {
        configurarPaciente();
        when(repository.listar(any(), org.mockito.ArgumentMatchers.eq(1), org.mockito.ArgumentMatchers.eq(5)))
                .thenReturn(PaginaResultado.de(List.of(documento), 1, 5, 6));

        mvc.perform(get(ROTA)
                        .param("pacienteId", UUID.randomUUID().toString())
                        .param("medicoId", medico.toString())
                        .param("tipo", "ATESTADO")
                        .param("periodoInicio", "2026-09-01T00:00:00Z")
                        .param("periodoFim", "2026-09-30T00:00:00Z")
                        .param("pagina", "1")
                        .param("tamanho", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].id").value(documento.getId().toString()))
                .andExpect(jsonPath("$.conteudo[0].conteudo").doesNotExist())
                .andExpect(jsonPath("$.conteudo[0].tipoDocumento").value("ATESTADO"))
                .andExpect(jsonPath("$.pagina").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(2));

        var filtro = ArgumentCaptor.forClass(DocumentoMedicoFiltro.class);
        verify(repository).listar(filtro.capture(), org.mockito.ArgumentMatchers.eq(1), org.mockito.ArgumentMatchers.eq(5));
        assertThat(filtro.getValue().pacienteId()).isEqualTo(paciente);
        assertThat(filtro.getValue().medicoId()).isEqualTo(medico);
        assertThat(filtro.getValue().tipo()).isEqualTo(TipoDocumento.ATESTADO);
    }

    @Test
    void medicoListaDocumentosEmitidosPorEleFiltrandoPorTipo() throws Exception {
        configurarMedico();
        when(repository.listar(any(), org.mockito.ArgumentMatchers.eq(0), org.mockito.ArgumentMatchers.eq(20)))
                .thenReturn(PaginaResultado.de(List.of(documento), 0, 20, 1));

        mvc.perform(get(ROTA)
                        .param("tipo", "ATESTADO")
                        .param("pagina", "0")
                        .param("tamanho", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo[0].tipoDocumento").value("ATESTADO"))
                .andExpect(jsonPath("$.conteudo[0].conteudo").doesNotExist());

        var filtro = ArgumentCaptor.forClass(DocumentoMedicoFiltro.class);
        verify(repository).listar(filtro.capture(), org.mockito.ArgumentMatchers.eq(0), org.mockito.ArgumentMatchers.eq(20));
        assertThat(filtro.getValue().medicoId()).isEqualTo(medico);
        assertThat(filtro.getValue().tipo()).isEqualTo(TipoDocumento.ATESTADO);
        assertThat(filtro.getValue().pacientesPermitidos()).isNull();
    }

    @Test
    void medicoAutorCancelaDocumento() throws Exception {
        configurarMedico();
        when(repository.buscarPorId(documento.getId())).thenReturn(Optional.of(documento));
        when(repository.salvar(any(DocumentoMedico.class))).thenAnswer(invocation -> invocation.getArgument(0));
        String body = json.writeValueAsString(Map.of("motivo", "emitido em duplicidade"));

        mvc.perform(put(ROTA + "/" + documento.getId() + "/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CANCELADO"))
                .andExpect(jsonPath("$.motivoCancelamento").value("emitido em duplicidade"));
    }

    @Test
    void medicoNaoAutorNaoCancelaDocumento() throws Exception {
        configurarMedico();
        when(cadastros.medicoIdDoUsuario(usuario)).thenReturn(Optional.of(UUID.randomUUID()));
        when(repository.buscarPorId(documento.getId())).thenReturn(Optional.of(documento));
        String body = json.writeValueAsString(Map.of("motivo", "emitido em duplicidade"));

        mvc.perform(put(ROTA + "/" + documento.getId() + "/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("médico autor")));
    }

    @Test
    void baixaPdfDoDocumento() throws Exception {
        configurarPaciente();
        byte[] bytes = "%PDF-1.4".getBytes();
        when(repository.buscarPorId(documento.getId())).thenReturn(Optional.of(documento));
        when(pdfs.gerar(any())).thenReturn(bytes);

        mvc.perform(get(ROTA + "/" + documento.getId() + "/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("documento-" + documento.getId() + ".pdf")))
                .andExpect(content().bytes(bytes));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUT", "PATCH", "DELETE"})
    void rejeitaAlteracaoOuExclusaoComExplicacao(String metodo) throws Exception {
        configurarPaciente();

        mvc.perform(request(HttpMethod.valueOf(metodo), ROTA + "/" + documento.getId()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail", containsString("imutavel")));

        verify(repository, never()).salvar(any(DocumentoMedico.class));
    }
}
