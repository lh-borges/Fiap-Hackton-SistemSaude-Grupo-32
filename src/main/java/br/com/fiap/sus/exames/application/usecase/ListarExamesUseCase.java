package br.com.fiap.sus.exames.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.exames.application.dto.ExameOutput;
import br.com.fiap.sus.exames.domain.repository.ExameFiltro;
import br.com.fiap.sus.exames.domain.repository.ExameRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class ListarExamesUseCase {

    private final ExameRepository exameRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public ListarExamesUseCase(ExameRepository exameRepository, CadastroQuery cadastroQuery,
                               UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.exameRepository = exameRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("isAuthenticated()")
    public PaginaResultado<ExameOutput> executar(ExameFiltro filtro, int pagina, int tamanho) {
        UsuarioAutenticado usuario = usuarioAutenticadoProvider.obrigatorio();
        ExameFiltro filtroEfetivo = filtro;

        if (usuario.ehPaciente() && !usuario.ehAdministrador()) {
            var pacienteId = cadastroQuery.pacienteIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente nao encontrado para este usuario."));
            filtroEfetivo = new ExameFiltro(pacienteId, filtro.unidadeSaudeId(), filtro.situacao(),
                    filtro.periodoInicio(), filtro.periodoFim());
        }

        return exameRepository.listar(filtroEfetivo, pagina, tamanho).mapear(ExameOutput::de);
    }
}