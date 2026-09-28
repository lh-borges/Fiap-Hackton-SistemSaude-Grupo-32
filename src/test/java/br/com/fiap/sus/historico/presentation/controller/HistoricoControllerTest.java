package br.com.fiap.sus.historico.presentation.controller;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.cadastros.api.PacienteResumo;
import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.historico.application.service.AcessoHistoricoService;
import br.com.fiap.sus.historico.application.usecase.ConsultarHistoricoUseCase;
import br.com.fiap.sus.historico.domain.enums.TipoRegistro;
import br.com.fiap.sus.historico.domain.model.Referencia;
import br.com.fiap.sus.historico.domain.model.RegistroHistorico;
import br.com.fiap.sus.historico.domain.port.FonteHistorico;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import br.com.fiap.sus.shared.presentation.ApiExceptionHandler;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HistoricoControllerTest.Config.class)
@DisplayName("GET /api/v1/historico")
class HistoricoControllerTest {
    static final String ROTA = "/api/v1/historico";
    @Autowired HistoricoController controller;
    @Autowired CadastroQuery cadastros;
    @Autowired ConsultaQuery consultas;
    @Autowired FonteHistorico fonte;
    MockMvc mvc;
    final UUID usuario = UUID.randomUUID();
    final UUID pacienteId = UUID.randomUUID();
    final UUID outroPaciente = UUID.randomUUID();
    final UUID medicoId = UUID.randomUUID();
    final UUID exameId = UUID.randomUUID();
    final RegistroHistorico resultado = new RegistroHistorico(TipoRegistro.RESULTADO_EXAME, UUID.randomUUID(),
            Instant.parse("2026-09-05T10:00:00Z"), "Resultado de exame: imagem", "DISPONIVEL", null,
            new Referencia(TipoRegistro.EXAME, exameId));
    final RegistroHistorico consulta = new RegistroHistorico(TipoRegistro.CONSULTA, UUID.randomUUID(),
            Instant.parse("2026-09-01T10:00:00Z"), "Consulta realizada", "REALIZADA", medicoId, null);

    @Configuration
    @EnableMethodSecurity
    @Import({HistoricoController.class, ConsultarHistoricoUseCase.class, AcessoHistoricoService.class,
            UsuarioAutenticadoProvider.class})
    static class Config {
        @Bean CadastroQuery cadastros() { return mock(CadastroQuery.class); }
        @Bean ConsultaQuery consultas() { return mock(ConsultaQuery.class); }
        @Bean FonteHistorico fonte() { return mock(FonteHistorico.class); }
    }

    @BeforeEach
    void configurar() {
        reset(cadastros, consultas, fonte);
        mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiExceptionHandler()).build();
        when(fonte.registrosDoPaciente(pacienteId)).thenReturn(List.of(consulta, resultado));
    }

    @AfterEach
    void limparContexto() { SecurityContextHolder.clearContext(); }

    void autenticar(String role) {
        var principal = new UsuarioAutenticado(usuario, "Usuario", Set.of(role));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, AuthorityUtils.createAuthorityList("ROLE_" + role)));
    }

    @Test
    @DisplayName("HU-01/RN-02: paciente ve a propria linha do tempo mesmo passando outro pacienteId")
    void pacienteVeOProprio() throws Exception {
        autenticar("PACIENTE");
        when(cadastros.pacienteIdDoUsuario(usuario)).thenReturn(Optional.of(pacienteId));

        mvc.perform(get(ROTA).param("pacienteId", outroPaciente.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.conteudo[0].tipo").value("RESULTADO_EXAME"))
                .andExpect(jsonPath("$.conteudo[0].recurso").value("resultados-exame"))
                .andExpect(jsonPath("$.conteudo[0].titulo").value("Resultado de exame: imagem"))
                .andExpect(jsonPath("$.conteudo[0].situacao").value("DISPONIVEL"))
                .andExpect(jsonPath("$.conteudo[0].origem.tipo").value("EXAME"))
                .andExpect(jsonPath("$.conteudo[0].origem.recurso").value("exames"))
                .andExpect(jsonPath("$.conteudo[0].origem.id").value(exameId.toString()))
                .andExpect(jsonPath("$.conteudo[1].tipo").value("CONSULTA"))
                .andExpect(jsonPath("$.conteudo[1].medicoId").value(medicoId.toString()))
                .andExpect(jsonPath("$.conteudo[1].origem").doesNotExist());

        verify(fonte).registrosDoPaciente(pacienteId);
        verify(fonte, never()).registrosDoPaciente(outroPaciente);
    }

    @Test
    @DisplayName("HU-04: filtro por tipo repetido e periodo")
    void filtros() throws Exception {
        autenticar("PACIENTE");
        when(cadastros.pacienteIdDoUsuario(usuario)).thenReturn(Optional.of(pacienteId));

        mvc.perform(get(ROTA).param("tipo", "CONSULTA", "RECEITA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].tipo").value("CONSULTA"));

        mvc.perform(get(ROTA).param("inicio", "2026-09-02T00:00:00Z").param("fim", "2026-09-30T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].tipo").value("RESULTADO_EXAME"));

        mvc.perform(get(ROTA).param("inicio", "2026-09-30T00:00:00Z").param("fim", "2026-09-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(ROTA).param("tamanho", "101")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("HU-02/HU-03: medico ve quem atende; paciente nao atendido responde 404; sem pacienteId responde 400")
    void medico() throws Exception {
        autenticar("MEDICO");
        when(cadastros.medicoIdDoUsuario(usuario)).thenReturn(Optional.of(medicoId));
        when(consultas.pacientesDoMedico(medicoId)).thenReturn(Set.of(pacienteId));

        mvc.perform(get(ROTA).param("pacienteId", pacienteId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2));
        mvc.perform(get(ROTA).param("pacienteId", outroPaciente.toString()))
                .andExpect(status().isNotFound());
        mvc.perform(get(ROTA)).andExpect(status().isBadRequest());
        verify(fonte, never()).registrosDoPaciente(outroPaciente);
    }

    @Test
    @DisplayName("administrador ve qualquer paciente existente")
    void administrador() throws Exception {
        autenticar("ADMINISTRADOR");
        when(cadastros.resumoDoPaciente(pacienteId))
                .thenReturn(Optional.of(new PacienteResumo(pacienteId, UUID.randomUUID(), "Ana", "1", "a@x", true)));
        when(cadastros.resumoDoPaciente(outroPaciente)).thenReturn(Optional.empty());

        mvc.perform(get(ROTA).param("pacienteId", pacienteId.toString())).andExpect(status().isOk());
        mvc.perform(get(ROTA).param("pacienteId", outroPaciente.toString())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("EX-03: atendente nao acessa historico")
    void atendente() throws Exception {
        autenticar("ATENDENTE");

        mvc.perform(get(ROTA).param("pacienteId", pacienteId.toString())).andExpect(status().isForbidden());
        verify(fonte, never()).registrosDoPaciente(any());
    }

    @Test
    @DisplayName("Artigo IV.3: sem autenticacao responde 401")
    void semAutenticacao() throws Exception {
        mvc.perform(get(ROTA)).andExpect(status().isUnauthorized());
        verifyNoInteractions(fonte);
    }
}
