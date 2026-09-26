package br.com.fiap.sus.consultas.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.application.dto.CancelarConsultaDTO;
import br.com.fiap.sus.consultas.application.dto.ConsultaOutput;
import br.com.fiap.sus.consultas.application.event.ConsultaCanceladaEvent;
import br.com.fiap.sus.consultas.domain.model.Consulta;
import br.com.fiap.sus.consultas.domain.repository.ConsultaRepository;
import br.com.fiap.sus.shared.domain.exception.NaoAutorizadoException;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** HU-03/RF-03/RN-06: cancela consulta com motivo. MEDICO/PACIENTE so cancelam as proprias. Publica ConsultaCanceladaEvent. */
@Component
public class CancelarConsultaUseCase {

    private final ConsultaRepository consultaRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;
    private final ApplicationEventPublisher eventPublisher;

    public CancelarConsultaUseCase(ConsultaRepository consultaRepository, CadastroQuery cadastroQuery,
                                   UsuarioAutenticadoProvider usuarioAutenticadoProvider,
                                   ApplicationEventPublisher eventPublisher) {
        this.consultaRepository = consultaRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
        this.eventPublisher = eventPublisher;
    }

    @PreAuthorize("hasAnyRole('ATENDENTE', 'MEDICO', 'PACIENTE', 'ADMINISTRADOR')")
    @Transactional
    public ConsultaOutput executar(CancelarConsultaDTO dto) {
        Consulta consulta = consultaRepository.buscarPorId(dto.consultaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta nao encontrada."));

        UsuarioAutenticado usuario = usuarioAutenticadoProvider.obrigatorio();
        verificarPosse(usuario, consulta);

        consulta.cancelar(dto.motivoCancelamento());
        Consulta salva = consultaRepository.salvar(consulta);
        eventPublisher.publishEvent(ConsultaCanceladaEvent.de(salva, usuario.id()));
        return ConsultaOutput.de(salva);
    }

    private void verificarPosse(UsuarioAutenticado usuario, Consulta consulta) {
        if (usuario.ehAdministrador()) {
            return;
        }
        if (usuario.ehMedico()) {
            var medicoId = cadastroQuery.medicoIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Medico nao encontrado para este usuario."));
            if (!consulta.getMedicoId().equals(medicoId)) {
                throw new NaoAutorizadoException("Medico so pode cancelar a propria consulta.");
            }
            return;
        }
        if (usuario.ehPaciente()) {
            var pacienteId = cadastroQuery.pacienteIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente nao encontrado para este usuario."));
            if (!consulta.getPacienteId().equals(pacienteId)) {
                throw new NaoAutorizadoException("Paciente so pode cancelar a propria consulta.");
            }
        }
        // ATENDENTE cancela qualquer uma, sem verificacao adicional.
    }
}
