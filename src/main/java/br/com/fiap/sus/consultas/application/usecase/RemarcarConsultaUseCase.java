package br.com.fiap.sus.consultas.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.application.dto.ConsultaOutput;
import br.com.fiap.sus.consultas.application.dto.RemarcarConsultaDTO;
import br.com.fiap.sus.consultas.domain.model.Consulta;
import br.com.fiap.sus.consultas.domain.repository.ConsultaRepository;
import br.com.fiap.sus.shared.domain.exception.NaoAutorizadoException;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/** HU-02/RF-02: remarca consulta ainda nao realizada nem cancelada. PACIENTE so remarca a propria. */
@Component
public class RemarcarConsultaUseCase {

    private final ConsultaRepository consultaRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public RemarcarConsultaUseCase(ConsultaRepository consultaRepository, CadastroQuery cadastroQuery,
                                   UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.consultaRepository = consultaRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("hasAnyRole('ATENDENTE', 'PACIENTE', 'ADMINISTRADOR')")
    public ConsultaOutput executar(RemarcarConsultaDTO dto) {
        Consulta consulta = consultaRepository.buscarPorId(dto.consultaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta nao encontrada."));

        UsuarioAutenticado usuario = usuarioAutenticadoProvider.obrigatorio();
        if (usuario.ehPaciente() && !usuario.ehAdministrador()) {
            var pacienteId = cadastroQuery.pacienteIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente nao encontrado para este usuario."));
            if (!consulta.getPacienteId().equals(pacienteId)) {
                throw new NaoAutorizadoException("Paciente so pode remarcar a propria consulta.");
            }
        }

        consulta.remarcar(dto.novaDataHora());
        return ConsultaOutput.de(consultaRepository.salvar(consulta));
    }
}