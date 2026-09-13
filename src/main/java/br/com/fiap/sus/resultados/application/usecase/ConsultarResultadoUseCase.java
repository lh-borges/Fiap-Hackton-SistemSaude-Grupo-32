package br.com.fiap.sus.resultados.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.resultados.application.dto.ResultadoExameOutput;
import br.com.fiap.sus.resultados.domain.model.ResultadoExame;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * HU-05/RF-07/EX-06: PACIENTE so consulta os proprios (senao, tratado como inexistente,
 * nao como "nao autorizado" - a spec pede que a existencia do recurso nao seja revelada).
 */
@Component
public class ConsultarResultadoUseCase {

    private final ResultadoExameRepository resultadoExameRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public ConsultarResultadoUseCase(ResultadoExameRepository resultadoExameRepository,
                                     CadastroQuery cadastroQuery,
                                     UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.resultadoExameRepository = resultadoExameRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'PACIENTE', 'ADMINISTRADOR')")
    public ResultadoExameOutput executar(UUID resultadoId) {
        ResultadoExame resultado = resultadoExameRepository.buscarPorId(resultadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Resultado nao encontrado."));

        UsuarioAutenticado usuario = usuarioAutenticadoProvider.obrigatorio();
        if (usuario.ehPaciente() && !usuario.ehAdministrador()) {
            var pacienteId = cadastroQuery.pacienteIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente nao encontrado para este usuario."));
            if (!resultado.getPacienteId().equals(pacienteId)) {
                // EX-06: tratado como inexistente, nao como acesso negado.
                throw new RecursoNaoEncontradoException("Resultado nao encontrado.");
            }
        }

        return ResultadoExameOutput.de(resultado);
    }
}