package br.com.fiap.sus.exames.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.exames.application.dto.AgendarExameDTO;
import br.com.fiap.sus.exames.application.dto.ExameOutput;
import br.com.fiap.sus.exames.application.event.ExameAgendadoEvent;
import br.com.fiap.sus.exames.domain.model.Exame;
import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.exames.domain.repository.ExameRepository;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameRepository;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** HU-02/RF-02/RN-02/EX-02: atendente/admin agendam exame a partir de solicitacao pendente. Publica ExameAgendadoEvent. */
@Component
public class AgendarExameUseCase {

    private final SolicitacaoExameRepository solicitacaoExameRepository;
    private final ExameRepository exameRepository;
    private final CadastroQuery cadastroQuery;
    private final ApplicationEventPublisher eventPublisher;

    public AgendarExameUseCase(SolicitacaoExameRepository solicitacaoExameRepository,
                               ExameRepository exameRepository, CadastroQuery cadastroQuery,
                               ApplicationEventPublisher eventPublisher) {
        this.solicitacaoExameRepository = solicitacaoExameRepository;
        this.exameRepository = exameRepository;
        this.cadastroQuery = cadastroQuery;
        this.eventPublisher = eventPublisher;
    }

    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMINISTRADOR')")
    @Transactional
    public ExameOutput executar(AgendarExameDTO dto) {
        SolicitacaoExame solicitacao = solicitacaoExameRepository.buscarPorId(dto.solicitacaoExameId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitacao de exame nao encontrada."));

        if (!cadastroQuery.unidadeAtivaExiste(dto.unidadeSaudeId())) {
            throw new RecursoNaoEncontradoException("Unidade de saude nao encontrada ou inativa.");
        }
        if (exameRepository.existeExameAtivoParaSolicitacao(solicitacao.getId())) {
            throw new ConflitoException("Ja existe um exame ativo para esta solicitacao.");
        }

        Exame exame = Exame.agendar(solicitacao.getId(), dto.unidadeSaudeId(), dto.dataAgendada());
        solicitacao.marcarComoAgendada();

        exameRepository.salvar(exame);
        solicitacaoExameRepository.salvar(solicitacao);
        eventPublisher.publishEvent(ExameAgendadoEvent.de(exame, solicitacao));

        return ExameOutput.de(exame);
    }
}
