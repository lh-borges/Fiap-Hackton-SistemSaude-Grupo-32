package br.com.fiap.sus.exames.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.exames.application.dto.SolicitacaoExameOutput;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameFiltro;
import br.com.fiap.sus.exames.domain.repository.SolicitacaoExameRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class ListarSolicitacoesExameUseCase {

    private final SolicitacaoExameRepository solicitacaoExameRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public ListarSolicitacoesExameUseCase(SolicitacaoExameRepository solicitacaoExameRepository,
                                          CadastroQuery cadastroQuery,
                                          UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.solicitacaoExameRepository = solicitacaoExameRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("isAuthenticated()")
    public PaginaResultado<SolicitacaoExameOutput> executar(SolicitacaoExameFiltro filtro, int pagina, int tamanho) {
        UsuarioAutenticado usuario = usuarioAutenticadoProvider.obrigatorio();
        SolicitacaoExameFiltro filtroEfetivo = filtro;

        if (usuario.ehPaciente() && !usuario.ehAdministrador()) {
            var pacienteId = cadastroQuery.pacienteIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente nao encontrado para este usuario."));
            filtroEfetivo = new SolicitacaoExameFiltro(pacienteId, filtro.medicoId(), filtro.tipoExameId(),
                    filtro.situacao(), filtro.periodoInicio(), filtro.periodoFim());
        } else if (usuario.ehMedico() && !usuario.ehAdministrador()) {
            var medicoId = cadastroQuery.medicoIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Medico nao encontrado para este usuario."));
            filtroEfetivo = new SolicitacaoExameFiltro(filtro.pacienteId(), medicoId, filtro.tipoExameId(),
                    filtro.situacao(), filtro.periodoInicio(), filtro.periodoFim());
        }

        return solicitacaoExameRepository.listar(filtroEfetivo, pagina, tamanho).mapear(SolicitacaoExameOutput::de);
    }
}