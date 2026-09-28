package br.com.fiap.sus.exames.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.exames.application.dto.SolicitacaoExameOutput;
import br.com.fiap.sus.exames.application.dto.SolicitarExameDTO;
import br.com.fiap.sus.exames.application.event.ExameSolicitadoEvent;
import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** HU-01/RF-01/RN-01/RN-07: so medico solicita, para paciente e tipo de exame ativos. Publica ExameSolicitadoEvent. */
@Component
public class SolicitarExameUseCase {

    private final SolicitacaoExameRepository solicitacaoExameRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;
    private final ApplicationEventPublisher eventPublisher;

    public SolicitarExameUseCase(SolicitacaoExameRepository solicitacaoExameRepository,
                                 CadastroQuery cadastroQuery,
                                 UsuarioAutenticadoProvider usuarioAutenticadoProvider,
                                 ApplicationEventPublisher eventPublisher) {
        this.solicitacaoExameRepository = solicitacaoExameRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
        this.eventPublisher = eventPublisher;
    }

    @PreAuthorize("hasRole('MEDICO')")
    @Transactional
    public SolicitacaoExameOutput executar(SolicitarExameDTO dto) {
        var usuario = usuarioAutenticadoProvider.obrigatorio();
        var medicoId = cadastroQuery.medicoIdDoUsuario(usuario.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medico nao encontrado para este usuario."));

        if (!cadastroQuery.pacienteAtivoExiste(dto.pacienteId())) {
            throw new RecursoNaoEncontradoException("Paciente nao encontrado ou inativo.");
        }
        if (!cadastroQuery.tipoExameAtivoExiste(dto.tipoExameId())) {
            throw new RecursoNaoEncontradoException("Tipo de exame nao encontrado ou inativo.");
        }

        SolicitacaoExame solicitacao = SolicitacaoExame.solicitar(dto.pacienteId(), medicoId,
                dto.tipoExameId(), dto.consultaId(), dto.justificativa());

        SolicitacaoExame salva = solicitacaoExameRepository.salvar(solicitacao);
        eventPublisher.publishEvent(ExameSolicitadoEvent.de(salva));
        return SolicitacaoExameOutput.de(salva);
    }
}
