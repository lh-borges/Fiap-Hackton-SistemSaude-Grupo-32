package br.com.fiap.sus.pareceres.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.iam.api.UsuarioResumo;
import br.com.fiap.sus.pareceres.application.service.ParecerLeituraService;
import br.com.fiap.sus.pareceres.domain.model.ParecerMedico;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoFiltro;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeituraParecerUseCaseTest {
    @Mock ParecerMedicoRepository repository;
    @Mock CadastroQuery cadastros;
    @Mock ConsultaQuery consultas;
    @Mock IamQuery iam;
    @Mock UsuarioAutenticadoProvider usuarios;
    ConsultarParecerUseCase consultar;
    ListarPareceresUseCase listar;
    final UUID usuario = UUID.randomUUID();
    final UUID paciente = UUID.randomUUID();
    final UUID medico = UUID.randomUUID();
    final UUID autorUsuario = UUID.randomUUID();
    final ParecerMedico parecer = ParecerMedico.emitir(UUID.randomUUID(), paciente, medico, "Interpretacao clinica.");

    @BeforeEach
    void configurar() {
        var leitura = new ParecerLeituraService(cadastros, consultas, iam);
        consultar = new ConsultarParecerUseCase(repository, leitura, usuarios);
        listar = new ListarPareceresUseCase(repository, leitura, usuarios);
    }

    void papel(String role) {
        when(usuarios.obrigatorio()).thenReturn(new UsuarioAutenticado(usuario, "Usuario", Set.of(role)));
    }

    void autor() {
        when(cadastros.resumoDoMedico(medico)).thenReturn(Optional.of(
                new MedicoResumo(medico, autorUsuario, "Dra. Maria", "teste@example.org",
                        "1234", "SP", UUID.randomUUID(), "Clinica Geral", false)));
        when(iam.resumoDoUsuario(autorUsuario)).thenReturn(Optional.of(
                new UsuarioResumo(autorUsuario, "Dra. Maria", "111.444.777-35",
                        "teste@example.org", false, Set.of("MEDICO"))));
    }

    @Test
    void pacienteLeProprioParecerComNomeECrmMesmoComAutorInativo() {
        papel("PACIENTE");
        when(repository.buscarPorId(parecer.getId())).thenReturn(Optional.of(parecer));
        when(cadastros.pacienteIdDoUsuario(usuario)).thenReturn(Optional.of(paciente));
        autor();
        var output = consultar.executar(parecer.getId());
        assertThat(output.medicoNome()).isEqualTo("Dra. Maria");
        assertThat(output.medicoCrm()).isEqualTo("1234");
        assertThat(output.descricao()).isEqualTo(parecer.getDescricao());
    }

    @Test
    void pacienteNaoLeTerceiro() {
        papel("PACIENTE");
        when(repository.buscarPorId(parecer.getId())).thenReturn(Optional.of(parecer));
        when(cadastros.pacienteIdDoUsuario(usuario)).thenReturn(Optional.of(UUID.randomUUID()));
        assertThatThrownBy(() -> consultar.executar(parecer.getId())).isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(iam);
        verify(cadastros, never()).resumoDoMedico(any());
    }

    @Test
    void medicoNaoLePacienteSemVinculo() {
        papel("MEDICO");
        when(repository.buscarPorId(parecer.getId())).thenReturn(Optional.of(parecer));
        when(cadastros.medicoIdDoUsuario(usuario)).thenReturn(Optional.of(medico));
        when(consultas.pacientesDoMedico(medico)).thenReturn(Set.of());
        assertThatThrownBy(() -> consultar.executar(parecer.getId())).isInstanceOf(RecursoNaoEncontradoException.class);
        verifyNoInteractions(iam);
    }

    @Test
    void medicoLeParecerDeOutroAutorQuandoAtendePaciente() {
        papel("MEDICO");
        UUID leitor = UUID.randomUUID();
        when(repository.buscarPorId(parecer.getId())).thenReturn(Optional.of(parecer));
        when(cadastros.medicoIdDoUsuario(usuario)).thenReturn(Optional.of(leitor));
        when(consultas.pacientesDoMedico(leitor)).thenReturn(Set.of(paciente));
        autor();
        assertThat(consultar.executar(parecer.getId()).descricao()).isEqualTo(parecer.getDescricao());
    }

    @Test
    void administradorRecebeSomenteMetadados() {
        papel("ADMINISTRADOR");
        when(repository.buscarPorId(parecer.getId())).thenReturn(Optional.of(parecer));
        autor();
        var output = consultar.executar(parecer.getId());
        assertThat(output.descricao()).isNull();
        assertThat(output.id()).isEqualTo(parecer.getId());
        assertThat(output.medicoNome()).isEqualTo("Dra. Maria");
        verifyNoInteractions(consultas);
    }

    @Test
    void listagemDoPacienteIgnoraTitularForjado() {
        papel("PACIENTE");
        when(cadastros.pacienteIdDoUsuario(usuario)).thenReturn(Optional.of(paciente));
        when(repository.listar(any(), eq(0), eq(20))).thenReturn(PaginaResultado.de(List.of(parecer), 0, 20, 1));
        autor();
        var output = listar.executar(new ParecerMedicoFiltro(UUID.randomUUID(), medico, parecer.getResultadoExameId(), null, null), 0, 20);
        var filtro = ArgumentCaptor.forClass(ParecerMedicoFiltro.class);
        verify(repository).listar(filtro.capture(), eq(0), eq(20));
        assertThat(filtro.getValue().pacienteId()).isEqualTo(paciente);
        assertThat(filtro.getValue().pacientesPermitidos()).containsExactly(paciente);
        assertThat(filtro.getValue().medicoId()).isEqualTo(medico);
        assertThat(output.conteudo()).hasSize(1);
    }

    @Test
    void listagemMedicaAplicaVinculoAntesDaPaginacao() {
        papel("MEDICO");
        when(cadastros.medicoIdDoUsuario(usuario)).thenReturn(Optional.of(medico));
        when(consultas.pacientesDoMedico(medico)).thenReturn(Set.of(paciente));
        when(repository.listar(any(), eq(0), eq(20))).thenReturn(PaginaResultado.de(List.of(), 0, 20, 0));
        UUID terceiro = UUID.randomUUID();
        listar.executar(new ParecerMedicoFiltro(terceiro, null, null, null, null), 0, 20);
        var filtro = ArgumentCaptor.forClass(ParecerMedicoFiltro.class);
        verify(repository).listar(filtro.capture(), eq(0), eq(20));
        assertThat(filtro.getValue().pacienteId()).isEqualTo(terceiro);
        assertThat(filtro.getValue().pacientesPermitidos()).containsExactly(paciente);
    }

    @Test
    void medicoAusenteProduzErroDeDominioEmVezDeOptionalGet() {
        papel("ADMINISTRADOR");
        when(repository.buscarPorId(parecer.getId())).thenReturn(Optional.of(parecer));
        when(cadastros.resumoDoMedico(medico)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> consultar.executar(parecer.getId())).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
