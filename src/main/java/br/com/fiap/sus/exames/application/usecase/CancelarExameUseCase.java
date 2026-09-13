package br.com.fiap.sus.exames.application.usecase;

import br.com.fiap.sus.exames.application.dto.CancelarExameDTO;
import br.com.fiap.sus.exames.application.dto.ExameOutput;
import br.com.fiap.sus.exames.domain.model.Exame;
import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.exames.domain.repository.ExameRepository;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/** HU-04 (2o cenario)/RN-06/EX-04: atendente/admin cancelam exame agendado; solicitacao volta a pendente. */
@Component
public class CancelarExameUseCase {

    private final ExameRepository exameRepository;
    private final SolicitacaoExameRepository solicitacaoExameRepository;

    public CancelarExameUseCase(ExameRepository exameRepository,
                                SolicitacaoExameRepository solicitacaoExameRepository) {
        this.exameRepository = exameRepository;
        this.solicitacaoExameRepository = solicitacaoExameRepository;
    }

    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMINISTRADOR')")
    public ExameOutput executar(CancelarExameDTO dto) {
        Exame exame = exameRepository.buscarPorId(dto.exameId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Exame nao encontrado."));

        SolicitacaoExame solicitacao = solicitacaoExameRepository.buscarPorId(exame.getSolicitacaoExameId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitacao de exame nao encontrada."));

        exame.cancelar();
        solicitacao.voltarParaPendente();

        exameRepository.salvar(exame);
        solicitacaoExameRepository.salvar(solicitacao);

        return ExameOutput.de(exame);
    }
}