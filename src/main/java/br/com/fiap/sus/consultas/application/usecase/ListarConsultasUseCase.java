package br.com.fiap.sus.consultas.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.application.dto.ConsultaOutput;
import br.com.fiap.sus.consultas.domain.repository.ConsultaFiltro;
import br.com.fiap.sus.consultas.domain.repository.ConsultaRepository;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/** RF-05/RF-06: lista consultas com filtros; PACIENTE so ve as proprias, independente
 *  do pacienteId informado no filtro (sobrescrito aqui). */
@Component
public class ListarConsultasUseCase {

    private final ConsultaRepository consultaRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public ListarConsultasUseCase(ConsultaRepository consultaRepository, CadastroQuery cadastroQuery,
                                  UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.consultaRepository = consultaRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("isAuthenticated()")
    public PaginaResultado<ConsultaOutput> executar(ConsultaFiltro filtro, int pagina, int tamanho) {
        UsuarioAutenticado usuario = usuarioAutenticadoProvider.obrigatorio();

        ConsultaFiltro filtroEfetivo = filtro;
        if (usuario.ehPaciente() && !usuario.ehAdministrador()) {
            var pacienteId = cadastroQuery.pacienteIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente nao encontrado para este usuario."));
            filtroEfetivo = new ConsultaFiltro(pacienteId, filtro.medicoId(), filtro.unidadeSaudeId(),
                    filtro.situacao(), filtro.periodoInicio(), filtro.periodoFim());
        }

        return consultaRepository.listar(filtroEfetivo, pagina, tamanho).mapear(ConsultaOutput::de);
    }
}