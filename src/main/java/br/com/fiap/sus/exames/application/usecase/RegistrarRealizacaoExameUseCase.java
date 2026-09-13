package br.com.fiap.sus.exames.application.usecase;

import br.com.fiap.sus.exames.application.dto.ExameOutput;
import br.com.fiap.sus.exames.application.dto.RegistrarRealizacaoExameDTO;
import br.com.fiap.sus.exames.domain.model.Exame;
import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.exames.domain.repository.ExameRepository;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/** HU-03/RF-03/RN-05/EX-03: atendente/admin registram realizacao. */
@Component
public class RegistrarRealizacaoExameUseCase {

    private final ExameRepository exameRepository;
    private final SolicitacaoExameRepository solicitacaoExameRepository;

    public RegistrarRealizacaoExameUseCase(ExameRepository exameRepository,
                                           SolicitacaoExameRepository solicitacaoExameRepository) {
        this.exameRepository = exameRepository;
        this.solicitacaoExameRepository = solicitacaoExameRepository;
    }

    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMINISTRADOR')")
    public ExameOutput executar(RegistrarRealizacaoExameDTO dto) {
        Exame exame = exameRepository.buscarPorId(dto.exameId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Exame nao encontrado."));

        SolicitacaoExame solicitacao = solicitacaoExameRepository.buscarPorId(exame.getSolicitacaoExameId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitacao de exame nao encontrada."));

        exame.registrarRealizacao(dto.dataRealizacao());
        solicitacao.marcarComoRealizada();

        exameRepository.salvar(exame);
        solicitacaoExameRepository.salvar(solicitacao);

        return ExameOutput.de(exame);
    }
}