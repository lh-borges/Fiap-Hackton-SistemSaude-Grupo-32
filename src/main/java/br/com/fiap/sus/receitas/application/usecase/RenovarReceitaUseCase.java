package br.com.fiap.sus.receitas.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.receitas.application.dto.ReceitaOutput;
import br.com.fiap.sus.receitas.application.dto.RenovarReceitaDTO;
import br.com.fiap.sus.receitas.domain.model.Receita;
import br.com.fiap.sus.receitas.domain.repository.ReceitaRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * HU-02/RF-03/RN-05: renova receita ativa e nao vencida.
 * Decisao (pendencia da spec, secao 13): qualquer medico ATIVO pode renovar, nao so o
 * autor original (continuidade do atendimento caso o medico autor nao esteja mais
 * disponivel). Diferente do cancelamento (RN-06), que continua restrito ao autor.
 */
@Component
public class RenovarReceitaUseCase {

    private final ReceitaRepository receitaRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public RenovarReceitaUseCase(ReceitaRepository receitaRepository, CadastroQuery cadastroQuery,
                                 UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.receitaRepository = receitaRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("hasRole('MEDICO')")
    public ReceitaOutput executar(RenovarReceitaDTO dto) {
        var usuario = usuarioAutenticadoProvider.obrigatorio();
        var medicoId = cadastroQuery.medicoIdDoUsuario(usuario.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medico nao encontrado para este usuario."));
        if (!cadastroQuery.medicoAtivoExiste(medicoId)) {
            throw new RegraDeNegocioException("Medico inativo nao pode renovar receitas.");
        }

        Receita original = receitaRepository.buscarPorId(dto.receitaOrigemId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Receita nao encontrada."));

        Receita renovada = original.renovar(dto.novaConsultaId(), dto.novaValidade(), dto.novaObservacao());

        receitaRepository.salvar(original);
        return ReceitaOutput.de(receitaRepository.salvar(renovada));
    }
}