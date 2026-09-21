package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.domain.enums.CategoriaExame;
import br.com.fiap.sus.cadastros.domain.enums.Sexo;
import br.com.fiap.sus.cadastros.domain.model.Especialidade;
import br.com.fiap.sus.cadastros.domain.model.Medico;
import br.com.fiap.sus.cadastros.domain.model.Paciente;
import br.com.fiap.sus.cadastros.domain.model.TipoExame;
import br.com.fiap.sus.cadastros.domain.model.UnidadeSaude;
import br.com.fiap.sus.cadastros.domain.repository.EspecialidadeRepository;
import br.com.fiap.sus.cadastros.domain.repository.MedicoRepository;
import br.com.fiap.sus.cadastros.domain.repository.PacienteRepository;
import br.com.fiap.sus.cadastros.domain.repository.TipoExameRepository;
import br.com.fiap.sus.cadastros.domain.repository.UnidadeSaudeRepository;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.iam.api.UsuarioResumo;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CadastroQueryServiceTest {

    private final PacienteRepository pacientes = mock(PacienteRepository.class);
    private final MedicoRepository medicos = mock(MedicoRepository.class);
    private final EspecialidadeRepository especialidades = mock(EspecialidadeRepository.class);
    private final UnidadeSaudeRepository unidades = mock(UnidadeSaudeRepository.class);
    private final TipoExameRepository tiposExame = mock(TipoExameRepository.class);
    private final IamQuery iam = mock(IamQuery.class);
    private final CadastroQueryService service = new CadastroQueryService(pacientes, medicos, especialidades,
            unidades, tiposExame, iam);

    @Test
    void resolveExistenciaResumoEVinculos() {
        UUID usuarioPaciente = UUID.randomUUID();
        UUID usuarioMedico = UUID.randomUUID();
        UUID especialidadeId = UUID.randomUUID();
        var paciente = Paciente.criar(usuarioPaciente, "123456789012345",
                LocalDate.of(1985, 4, 12), Sexo.FEMININO, "O+");
        var medico = Medico.criar(usuarioMedico, "123456", "SP", especialidadeId);
        var unidade = UnidadeSaude.criar("UBS Central", "1234567", null);
        var tipoExame = TipoExame.criar("Raio-X de torax", CategoriaExame.IMAGEM, "Sem preparo");
        var especialidade = Especialidade.reconstituir(especialidadeId, "Radiologia", "Imagem",
                true, Instant.now(), Instant.now());

        when(pacientes.porId(paciente.getId())).thenReturn(Optional.of(paciente));
        when(pacientes.porUsuarioId(usuarioPaciente)).thenReturn(Optional.of(paciente));
        when(medicos.porId(medico.getId())).thenReturn(Optional.of(medico));
        when(medicos.porUsuarioId(usuarioMedico)).thenReturn(Optional.of(medico));
        when(unidades.porId(unidade.getId())).thenReturn(Optional.of(unidade));
        when(tiposExame.porId(tipoExame.getId())).thenReturn(Optional.of(tipoExame));
        when(especialidades.porId(especialidadeId)).thenReturn(Optional.of(especialidade));
        when(iam.resumoDoUsuario(usuarioPaciente)).thenReturn(Optional.of(new UsuarioResumo(usuarioPaciente,
                "Maria Souza", "111.444.777-35", "maria@sus.gov.br", true, Set.of("PACIENTE"))));
        when(iam.resumoDoUsuario(usuarioMedico)).thenReturn(Optional.of(new UsuarioResumo(usuarioMedico,
                "Carlos Lima", "222.333.444-55", "carlos@sus.gov.br", true, Set.of("MEDICO"))));

        assertThat(service.pacienteAtivoExiste(paciente.getId())).isTrue();
        assertThat(service.medicoAtivoExiste(medico.getId())).isTrue();
        assertThat(service.unidadeAtivaExiste(unidade.getId())).isTrue();
        assertThat(service.tipoExameAtivoExiste(tipoExame.getId())).isTrue();
        assertThat(service.categoriaDoTipoExame(tipoExame.getId())).contains("IMAGEM");
        assertThat(service.nomeDoTipoExame(tipoExame.getId())).contains("Raio-X de torax");
        assertThat(service.pacienteIdDoUsuario(usuarioPaciente)).contains(paciente.getId());
        assertThat(service.medicoIdDoUsuario(usuarioMedico)).contains(medico.getId());

        var pacienteResumo = service.resumoDoPaciente(paciente.getId()).orElseThrow();
        assertThat(pacienteResumo.nome()).isEqualTo("Maria Souza");
        assertThat(pacienteResumo.cpf()).isEqualTo("111.444.777-35");

        var medicoResumo = service.resumoDoMedico(medico.getId()).orElseThrow();
        assertThat(medicoResumo.nome()).isEqualTo("Carlos Lima");
        assertThat(medicoResumo.crm()).isEqualTo("123456");
        assertThat(medicoResumo.especialidade()).isEqualTo("Radiologia");
    }

    @Test
    void retornaFalsoOuVazioQuandoNaoEncontra() {
        UUID id = UUID.randomUUID();

        assertThat(service.pacienteAtivoExiste(id)).isFalse();
        assertThat(service.medicoAtivoExiste(id)).isFalse();
        assertThat(service.unidadeAtivaExiste(id)).isFalse();
        assertThat(service.tipoExameAtivoExiste(id)).isFalse();
        assertThat(service.categoriaDoTipoExame(id)).isEmpty();
        assertThat(service.nomeDoTipoExame(id)).isEmpty();
        assertThat(service.pacienteIdDoUsuario(id)).isEmpty();
        assertThat(service.medicoIdDoUsuario(id)).isEmpty();
        assertThat(service.resumoDoPaciente(id)).isEmpty();
        assertThat(service.resumoDoMedico(id)).isEmpty();
    }
}
