package br.com.fiap.sus.receitas.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.receitas.application.dto.ReceitaOutput;
import br.com.fiap.sus.receitas.domain.repository.ReceitaFiltro;
import br.com.fiap.sus.receitas.domain.repository.ReceitaRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@Component
public class ListarReceitasUseCase {

    private final ReceitaRepository receitaRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public ListarReceitasUseCase(ReceitaRepository receitaRepository, CadastroQuery cadastroQuery,
                                 UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.receitaRepository = receitaRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("isAuthenticated()")
    public PaginaResultado<ReceitaOutput> executar(ReceitaFiltro filtro, int pagina, int tamanho) {
        UsuarioAutenticado usuario = usuarioAutenticadoProvider.obrigatorio();
        ReceitaFiltro filtroEfetivo = filtro;

        if (usuario.ehPaciente() && !usuario.ehAdministrador()) {
            var pacienteId = cadastroQuery.pacienteIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente nao encontrado para este usuario."));
            filtroEfetivo = new ReceitaFiltro(pacienteId, filtro.medicoId(), filtro.situacao());
        } else if (usuario.ehMedico() && !usuario.ehAdministrador()) {
            var medicoId = cadastroQuery.medicoIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Medico nao encontrado para este usuario."));
            filtroEfetivo = new ReceitaFiltro(filtro.pacienteId(), medicoId, filtro.situacao());
        }

        return receitaRepository.listar(filtroEfetivo, pagina, tamanho).mapear(ReceitaOutput::de);
    }
}