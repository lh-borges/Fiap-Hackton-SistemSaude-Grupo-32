package br.com.fiap.sus.receitas.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.receitas.application.dto.CancelarReceitaDTO;
import br.com.fiap.sus.receitas.application.dto.ReceitaOutput;
import br.com.fiap.sus.receitas.domain.model.Receita;
import br.com.fiap.sus.receitas.domain.repository.ReceitaRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/** HU-04/RF-05/RN-06/EX-05: so o medico autor cancela (verificado no dominio). */
@Component
public class CancelarReceitaUseCase {

    private final ReceitaRepository receitaRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public CancelarReceitaUseCase(ReceitaRepository receitaRepository, CadastroQuery cadastroQuery,
                                  UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.receitaRepository = receitaRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("hasRole('MEDICO')")
    public ReceitaOutput executar(CancelarReceitaDTO dto) {
        Receita receita = receitaRepository.buscarPorId(dto.receitaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Receita nao encontrada."));

        var usuario = usuarioAutenticadoProvider.obrigatorio();
        var medicoId = cadastroQuery.medicoIdDoUsuario(usuario.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medico nao encontrado para este usuario."));

        // Receita.cancelar ja lanca RegraDeNegocioException se medicoId nao for o autor (RN-06).
        receita.cancelar(medicoId, dto.motivo());
        return ReceitaOutput.de(receitaRepository.salvar(receita));
    }
}