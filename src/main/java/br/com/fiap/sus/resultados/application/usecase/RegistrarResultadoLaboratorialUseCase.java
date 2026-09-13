package br.com.fiap.sus.resultados.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.exames.api.ExameQuery;
import br.com.fiap.sus.resultados.application.dto.ItemResultadoLaboratorialDTO;
import br.com.fiap.sus.resultados.application.dto.RegistrarResultadoLaboratorialDTO;
import br.com.fiap.sus.resultados.application.dto.ResultadoExameOutput;
import br.com.fiap.sus.resultados.domain.enums.SituacaoParametro;
import br.com.fiap.sus.resultados.domain.model.ItemResultadoLaboratorial;
import br.com.fiap.sus.resultados.domain.model.ResultadoExame;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameRepository;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/** HU-02/RF-02/RF-03/RN-01/RN-02/RN-03/RN-05/RN-06/RN-07: ATENDENTE/ADMINISTRADOR registram. */
@Component
public class RegistrarResultadoLaboratorialUseCase {

    private final ResultadoExameRepository resultadoExameRepository;
    private final ExameQuery exameQuery;
    private final CadastroQuery cadastroQuery;

    public RegistrarResultadoLaboratorialUseCase(ResultadoExameRepository resultadoExameRepository,
                                                 ExameQuery exameQuery, CadastroQuery cadastroQuery) {
        this.resultadoExameRepository = resultadoExameRepository;
        this.exameQuery = exameQuery;
        this.cadastroQuery = cadastroQuery;
    }

    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMINISTRADOR')")
    public ResultadoExameOutput executar(RegistrarResultadoLaboratorialDTO dto) {
        if (!exameQuery.exameRealizadoExiste(dto.exameId())) {
            throw new RegraDeNegocioException("Resultado so pode ser registrado para exame realizado.");
        }
        if (resultadoExameRepository.existePorExameId(dto.exameId())) {
            throw new ConflitoException("Este exame ja possui um resultado registrado.");
        }

        UUID tipoExameId = exameQuery.tipoExameIdDoExame(dto.exameId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Exame nao encontrado."));
        String categoria = cadastroQuery.categoriaDoTipoExame(tipoExameId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de exame nao encontrado."));
        if (!"LABORATORIAL".equalsIgnoreCase(categoria)) {
            throw new RegraDeNegocioException("O tipo de exame solicitado nao e da categoria LABORATORIAL.");
        }

        List<ItemResultadoLaboratorial> itens = dto.itens().stream().map(this::converter).toList();
        ResultadoExame resultado = ResultadoExame.criarLaboratorial(dto.exameId(), dto.pacienteId(), itens,
                dto.observacao());

        return ResultadoExameOutput.de(resultadoExameRepository.salvar(resultado));
    }

    private ItemResultadoLaboratorial converter(ItemResultadoLaboratorialDTO item) {
        boolean quantitativo = item.valor() != null && item.unidade() != null && !item.unidade().isBlank();
        if (quantitativo) {
            return ItemResultadoLaboratorial.quantitativo(item.nomeParametro(), item.valor(), item.unidade(),
                    item.valorMinimoReferencia(), item.valorMaximoReferencia());
        }
        SituacaoParametro situacao = item.situacao() == null ? null : SituacaoParametro.valueOf(item.situacao());
        return ItemResultadoLaboratorial.qualitativo(item.nomeParametro(), item.resultadoTexto(), situacao);
    }
}