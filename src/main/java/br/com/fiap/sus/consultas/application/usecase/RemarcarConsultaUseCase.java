package br.com.fiap.sus.consultas.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.application.dto.ConsultaOutput;
import br.com.fiap.sus.consultas.application.dto.RemarcarConsultaDTO;
import br.com.fiap.sus.consultas.application.event.ConsultaRemarcadaEvent;
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

import java.time.Instant;

/** HU-02/RF-02: remarca consulta ainda nao realizada nem cancelada. PACIENTE so remarca a propria. Publica ConsultaRemarcadaEvent. */
@Component
public class RemarcarConsultaUseCase {

    private final ConsultaRepository consultaRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;
    private final ApplicationEventPublisher eventPublisher;

    public RemarcarConsultaUseCase(ConsultaRepository consultaRepository, CadastroQuery cadastroQuery,
                                   UsuarioAutenticadoProvider usuarioAutenticadoProvider,
                                   ApplicationEventPublisher eventPublisher) {
        this.consultaRepository = consultaRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
        this.eventPublisher = eventPublisher;
    }

    @PreAuthorize("hasAnyRole('ATENDENTE', 'PACIENTE', 'ADMINISTRADOR')")
    @Transactional
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

        Instant dataHoraAnterior = consulta.getDataHora();
        consulta.remarcar(dto.novaDataHora());
        Consulta salva = consultaRepository.salvar(consulta);
        eventPublisher.publishEvent(ConsultaRemarcadaEvent.de(salva, dataHoraAnterior));
        return ConsultaOutput.de(salva);
    }
}
