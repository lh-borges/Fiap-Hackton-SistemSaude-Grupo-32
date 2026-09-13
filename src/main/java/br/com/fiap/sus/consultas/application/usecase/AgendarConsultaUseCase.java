package br.com.fiap.sus.consultas.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.application.dto.AgendarConsultaDTO;
import br.com.fiap.sus.consultas.application.dto.ConsultaOutput;
import br.com.fiap.sus.consultas.domain.model.Consulta;
import br.com.fiap.sus.consultas.domain.repository.ConsultaRepository;
import br.com.fiap.sus.shared.domain.exception.NaoAutorizadoException;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** HU-01/RF-01/RN-01/RN-02: agenda consulta. PACIENTE so agenda para si mesmo. */
@Component
public class AgendarConsultaUseCase {

    private final ConsultaRepository consultaRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public AgendarConsultaUseCase(ConsultaRepository consultaRepository, CadastroQuery cadastroQuery,
                                  UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.consultaRepository = consultaRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("hasAnyRole('ATENDENTE', 'PACIENTE', 'ADMINISTRADOR')")
    public ConsultaOutput executar(AgendarConsultaDTO dto) {
        UsuarioAutenticado usuario = usuarioAutenticadoProvider.obrigatorio();

        // RN: PACIENTE so agenda para si mesmo (nao pode agendar consulta de outro paciente).
        if (usuario.ehPaciente() && !usuario.ehAdministrador()) {
            UUID pacienteIdDoUsuario = cadastroQuery.pacienteIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente nao encontrado para este usuario."));
            if (!pacienteIdDoUsuario.equals(dto.pacienteId())) {
                throw new NaoAutorizadoException("Paciente so pode agendar consulta para si mesmo.");
            }
        }

        if (!cadastroQuery.pacienteAtivoExiste(dto.pacienteId())) {
            throw new RecursoNaoEncontradoException("Paciente nao encontrado ou inativo.");
        }
        if (!cadastroQuery.medicoAtivoExiste(dto.medicoId())) {
            throw new RecursoNaoEncontradoException("Medico nao encontrado ou inativo.");
        }
        if (!cadastroQuery.unidadeAtivaExiste(dto.unidadeSaudeId())) {
            throw new RecursoNaoEncontradoException("Unidade de saude nao encontrada ou inativa.");
        }

        Consulta consulta = Consulta.agendar(dto.pacienteId(), dto.medicoId(), dto.unidadeSaudeId(),
                dto.dataHora(), dto.motivo());
        return ConsultaOutput.de(consultaRepository.salvar(consulta));
    }
}