package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.cadastros.api.MedicoResumo;
import br.com.fiap.sus.cadastros.api.PacienteResumo;
import br.com.fiap.sus.cadastros.domain.model.Medico;
import br.com.fiap.sus.cadastros.domain.model.Paciente;
import br.com.fiap.sus.cadastros.domain.model.TipoExame;
import br.com.fiap.sus.cadastros.domain.model.UnidadeSaude;
import br.com.fiap.sus.cadastros.domain.repository.MedicoRepository;
import br.com.fiap.sus.cadastros.domain.repository.PacienteRepository;
import br.com.fiap.sus.cadastros.domain.repository.EspecialidadeRepository;
import br.com.fiap.sus.cadastros.domain.repository.TipoExameRepository;
import br.com.fiap.sus.cadastros.domain.repository.UnidadeSaudeRepository;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.iam.api.UsuarioResumo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementacao da porta publicada em cadastros.api. */
@Service
@Transactional(readOnly = true)
public class CadastroQueryService implements CadastroQuery {

    private final PacienteRepository pacientes;
    private final MedicoRepository medicos;
    private final EspecialidadeRepository especialidades;
    private final UnidadeSaudeRepository unidades;
    private final TipoExameRepository tiposExame;
    private final IamQuery iam;

    public CadastroQueryService(PacienteRepository pacientes, MedicoRepository medicos,
                                EspecialidadeRepository especialidades, UnidadeSaudeRepository unidades,
                                TipoExameRepository tiposExame,
                                IamQuery iam) {
        this.pacientes = pacientes;
        this.medicos = medicos;
        this.especialidades = especialidades;
        this.unidades = unidades;
        this.tiposExame = tiposExame;
        this.iam = iam;
    }

    @Override
    public boolean pacienteAtivoExiste(UUID pacienteId) {
        return pacientes.porId(pacienteId).map(Paciente::isAtivo).orElse(false);
    }

    @Override
    public boolean medicoAtivoExiste(UUID medicoId) {
        return medicos.porId(medicoId).map(Medico::isAtivo).orElse(false);
    }

    @Override
    public boolean unidadeAtivaExiste(UUID unidadeSaudeId) {
        return unidades.porId(unidadeSaudeId).map(UnidadeSaude::isAtivo).orElse(false);
    }

    @Override
    public boolean tipoExameAtivoExiste(UUID tipoExameId) {
        return tiposExame.porId(tipoExameId).map(TipoExame::isAtivo).orElse(false);
    }

    @Override
    public Optional<String> categoriaDoTipoExame(UUID tipoExameId) {
        return tiposExame.porId(tipoExameId).map(tipo -> tipo.getCategoria().name());
    }

    @Override
    public Optional<String> nomeDoTipoExame(UUID tipoExameId) {
        return tiposExame.porId(tipoExameId)
                .map(TipoExame::getNome)
                .filter(nome -> !nome.isBlank());
    }

    @Override
    public Optional<UUID> pacienteIdDoUsuario(UUID usuarioId) {
        return pacientes.porUsuarioId(usuarioId).map(Paciente::getId);
    }

    @Override
    public Optional<UUID> medicoIdDoUsuario(UUID usuarioId) {
        return medicos.porUsuarioId(usuarioId).map(Medico::getId);
    }

    @Override
    public Optional<PacienteResumo> resumoDoPaciente(UUID pacienteId) {
        return pacientes.porId(pacienteId)
                .map(paciente -> {
                    UsuarioResumo usuario = iam.resumoDoUsuario(paciente.getUsuarioId()).orElse(null);
                    return new PacienteResumo(paciente.getId(), paciente.getUsuarioId(),
                            usuario == null ? null : usuario.nome(),
                            usuario == null ? null : usuario.cpf(),
                            usuario == null ? null : usuario.email(),
                            paciente.isAtivo());
                });
    }

    @Override
    public Optional<MedicoResumo> resumoDoMedico(UUID medicoId) {
        return medicos.porId(medicoId)
                .map(medico -> {
                    UsuarioResumo usuario = iam.resumoDoUsuario(medico.getUsuarioId()).orElse(null);
                    return new MedicoResumo(medico.getId(), medico.getUsuarioId(),
                            usuario == null ? null : usuario.nome(),
                            usuario == null ? null : usuario.email(),
                            medico.getCrm(), medico.getUfCrm(), medico.getEspecialidadeId(),
                            especialidades.porId(medico.getEspecialidadeId())
                                    .map(br.com.fiap.sus.cadastros.domain.model.Especialidade::getNome)
                                    .orElse(null),
                            medico.isAtivo());
                });
    }
}
