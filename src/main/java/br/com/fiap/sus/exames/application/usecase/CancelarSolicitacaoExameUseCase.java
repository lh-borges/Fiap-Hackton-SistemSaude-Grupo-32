package br.com.fiap.sus.exames.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.exames.application.dto.CancelarSolicitacaoExameDTO;
import br.com.fiap.sus.exames.application.dto.SolicitacaoExameOutput;
import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameRepository;
import br.com.fiap.sus.shared.domain.exception.NaoAutorizadoException;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/** HU-04 (1o cenario)/RF-04/RN-06: MEDICO cancela apenas a propria solicitacao pendente. */
@Component
public class CancelarSolicitacaoExameUseCase {

    private final SolicitacaoExameRepository solicitacaoExameRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public CancelarSolicitacaoExameUseCase(SolicitacaoExameRepository solicitacaoExameRepository,
                                           CadastroQuery cadastroQuery,
                                           UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.solicitacaoExameRepository = solicitacaoExameRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'ADMINISTRADOR')")
    public SolicitacaoExameOutput executar(CancelarSolicitacaoExameDTO dto) {
        SolicitacaoExame solicitacao = solicitacaoExameRepository.buscarPorId(dto.solicitacaoExameId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Solicitacao de exame nao encontrada."));

        UsuarioAutenticado usuario = usuarioAutenticadoProvider.obrigatorio();
        if (usuario.ehMedico() && !usuario.ehAdministrador()) {
            var medicoId = cadastroQuery.medicoIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Medico nao encontrado para este usuario."));
            if (!solicitacao.getMedicoId().equals(medicoId)) {
                throw new NaoAutorizadoException("Medico so pode cancelar a propria solicitacao.");
            }
        }

        solicitacao.cancelar(dto.motivo());
        return SolicitacaoExameOutput.de(solicitacaoExameRepository.salvar(solicitacao));
    }
}