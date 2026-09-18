package br.com.fiap.sus.pareceres.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.pareceres.application.event.ParecerCriadoEvent;
import br.com.fiap.sus.pareceres.application.dto.RegistrarParecerDTO;
import br.com.fiap.sus.pareceres.domain.model.ParecerMedico;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoRepository;
import br.com.fiap.sus.resultados.api.ResultadoQuery;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarParecerUseCaseTest {
    @Mock ParecerMedicoRepository repository;
    @Mock ResultadoQuery resultados;
    @Mock CadastroQuery cadastros;
    @Mock UsuarioAutenticadoProvider usuarios;
    @Mock ApplicationEventPublisher events;
    RegistrarParecerUseCase useCase;
    final UUID usuario = UUID.randomUUID();
    final UUID medico = UUID.randomUUID();
    final UUID paciente = UUID.randomUUID();
    final UUID resultado = UUID.randomUUID();
    final UUID especialidade = UUID.randomUUID();

    @BeforeEach
    void configurar() {
        useCase = new RegistrarParecerUseCase(repository, resultados, cadastros, usuarios, events);
        when(usuarios.obrigatorio()).thenReturn(new UsuarioAutenticado(usuario, "Medico", Set.of("MEDICO")));
        when(cadastros.medicoIdDoUsuario(usuario)).thenReturn(Optional.of(medico));
    }

    void medicoAtivo(boolean ativo) {
        when(cadastros.resumoDoMedico(medico))
                .thenReturn(Optional.of(new MedicoResumo(medico, usuario, "12345", "SP", especialidade, ativo)));
    }

    @Test
    void derivaPacienteDoResultadoEAutorDoUsuario() {
        medicoAtivo(true);
        when(resultados.pacienteIdDoResultado(resultado)).thenReturn(Optional.of(paciente));
        when(repository.salvar(any(ParecerMedico.class), eq(usuario))).thenAnswer(invocation -> invocation.getArgument(0));
        var output = useCase.executar(new RegistrarParecerDTO(resultado, "Interpretacao do resultado."));
        assertThat(output.pacienteId()).isEqualTo(paciente);
        assertThat(output.medicoId()).isEqualTo(medico);
        assertThat(output.resultadoExameId()).isEqualTo(resultado);
        assertThat(output.idEspecialidade()).isEqualTo(especialidade);
        assertThat(output.medicoCrm()).isEqualTo("12345");
        var event = ArgumentCaptor.forClass(ParecerCriadoEvent.class);
        var ordem = inOrder(repository, events);
        ordem.verify(repository).salvar(any(ParecerMedico.class), eq(usuario));
        ordem.verify(events).publishEvent(event.capture());
        assertThat(event.getValue()).isEqualTo(new ParecerCriadoEvent(output.id(), resultado, paciente, medico, output.dataParecer()));
        verify(cadastros, times(1)).resumoDoMedico(medico);
    }

    @Test
    void rejeitaMedicoInativo() {
        medicoAtivo(false);
        assertThatThrownBy(() -> useCase.executar(new RegistrarParecerDTO(resultado, "Descricao valida")))
                .isInstanceOf(RegraDeNegocioException.class);
        verifyNoInteractions(resultados, repository, events);
    }

    @Test
    void rejeitaUsuarioSemCadastroMedico() {
        when(cadastros.medicoIdDoUsuario(usuario)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.executar(new RegistrarParecerDTO(resultado, "Descricao valida")))
                .isInstanceOf(RegraDeNegocioException.class);
        verifyNoInteractions(resultados, repository, events);
    }

    @Test
    void rejeitaMedicoAusente() {
        when(cadastros.resumoDoMedico(medico)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.executar(new RegistrarParecerDTO(resultado, "Descricao valida")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(resultados, repository, events);
    }

    @Test
    void rejeitaResultadoInexistente() {
        medicoAtivo(true);
        when(resultados.pacienteIdDoResultado(resultado)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.executar(new RegistrarParecerDTO(resultado, "Descricao valida")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(repository, events);
    }

    @Test
    void rejeitaResultadoNuloAntesDaConsulta() {
        medicoAtivo(true);
        assertThatThrownBy(() -> useCase.executar(new RegistrarParecerDTO(null, "Descricao valida")))
                .isInstanceOf(RegraDeNegocioException.class);
        verifyNoInteractions(resultados, repository, events);
    }

    @Test
    void naoPublicaEventoQuandoPersistenciaFalha() {
        medicoAtivo(true);
        when(resultados.pacienteIdDoResultado(resultado)).thenReturn(Optional.of(paciente));
        when(repository.salvar(any(ParecerMedico.class), eq(usuario))).thenThrow(new IllegalStateException("Falha de persistencia"));
        assertThatThrownBy(() -> useCase.executar(new RegistrarParecerDTO(resultado, "Descricao valida")))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(events);
    }
}
