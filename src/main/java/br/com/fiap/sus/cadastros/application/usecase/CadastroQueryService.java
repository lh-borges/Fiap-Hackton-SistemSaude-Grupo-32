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
import br.com.fiap.sus.cadastros.domain.repository.TipoExameRepository;
import br.com.fiap.sus.cadastros.domain.repository.UnidadeSaudeRepository;
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
    private final UnidadeSaudeRepository unidades;
    private final TipoExameRepository tiposExame;

    public CadastroQueryService(PacienteRepository pacientes, MedicoRepository medicos,
                                UnidadeSaudeRepository unidades, TipoExameRepository tiposExame) {
        this.pacientes = pacientes;
        this.medicos = medicos;
        this.unidades = unidades;
        this.tiposExame = tiposExame;
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
                .map(paciente -> new PacienteResumo(paciente.getId(), paciente.getUsuarioId(),
                        paciente.isAtivo()));
    }

    @Override
    public Optional<MedicoResumo> resumoDoMedico(UUID medicoId) {
        return medicos.porId(medicoId)
                .map(medico -> new MedicoResumo(medico.getId(), medico.getUsuarioId(), medico.getCrm(),
                        medico.getUfCrm(), medico.getEspecialidadeId(), medico.isAtivo()));
    }
}
