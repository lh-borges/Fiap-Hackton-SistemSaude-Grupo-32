package br.com.fiap.sus.resultados.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.resultados.application.dto.ResultadoExameOutput;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameFiltro;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class ListarResultadosUseCase {

    private final ResultadoExameRepository resultadoExameRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public ListarResultadosUseCase(ResultadoExameRepository resultadoExameRepository,
                                   CadastroQuery cadastroQuery,
                                   UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.resultadoExameRepository = resultadoExameRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE', 'ADMINISTRADOR')")
    public PaginaResultado<ResultadoExameOutput> executar(ResultadoExameFiltro filtro, int pagina, int tamanho) {
        UsuarioAutenticado usuario = usuarioAutenticadoProvider.obrigatorio();
        ResultadoExameFiltro filtroEfetivo = filtro;

        if (usuario.ehPaciente() && !usuario.ehAdministrador()) {
            var pacienteId = cadastroQuery.pacienteIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente nao encontrado para este usuario."));
            filtroEfetivo = new ResultadoExameFiltro(pacienteId, filtro.periodoInicio(), filtro.periodoFim());
        }

        return resultadoExameRepository.listar(filtroEfetivo, pagina, tamanho).mapear(ResultadoExameOutput::de);
    }
}