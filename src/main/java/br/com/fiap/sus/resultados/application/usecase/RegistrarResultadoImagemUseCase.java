package br.com.fiap.sus.resultados.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.exames.api.ExameQuery;
import br.com.fiap.sus.resultados.application.dto.RegistrarResultadoImagemDTO;
import br.com.fiap.sus.resultados.application.dto.ResultadoExameOutput;
import br.com.fiap.sus.resultados.domain.model.ResultadoExame;
import br.com.fiap.sus.resultados.domain.repository.ResultadoExameRepository;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/** HU-01/RF-01/RN-01/RN-02/RN-03/RN-04: ATENDENTE/ADMINISTRADOR registram resultado de imagem. */
@Component
public class RegistrarResultadoImagemUseCase {

    private final ResultadoExameRepository resultadoExameRepository;
    private final ExameQuery exameQuery;
    private final CadastroQuery cadastroQuery;

    public RegistrarResultadoImagemUseCase(ResultadoExameRepository resultadoExameRepository,
                                           ExameQuery exameQuery, CadastroQuery cadastroQuery) {
        this.resultadoExameRepository = resultadoExameRepository;
        this.exameQuery = exameQuery;
        this.cadastroQuery = cadastroQuery;
    }

    @PreAuthorize("hasAnyRole('ATENDENTE', 'ADMINISTRADOR')")
    public ResultadoExameOutput executar(RegistrarResultadoImagemDTO dto) {
        if (!exameQuery.exameRealizadoExiste(dto.exameId())) {
            throw new RegraDeNegocioException("Resultado so pode ser registrado para exame realizado.");
        }
        if (resultadoExameRepository.existePorExameId(dto.exameId())) {
            throw new ConflitoException("Este exame ja possui um resultado registrado.");
        }

        var tipoExameId = exameQuery.tipoExameIdDoExame(dto.exameId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Exame nao encontrado."));
        String categoria = cadastroQuery.categoriaDoTipoExame(tipoExameId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de exame nao encontrado."));
        if (!"IMAGEM".equalsIgnoreCase(categoria)) {
            throw new RegraDeNegocioException("O tipo de exame solicitado nao e da categoria IMAGEM.");
        }

        ResultadoExame resultado = ResultadoExame.criarImagem(dto.exameId(), dto.pacienteId(),
                dto.arquivoUrl(), dto.descricao(), dto.laudo(), dto.observacao());

        return ResultadoExameOutput.de(resultadoExameRepository.salvar(resultado));
    }
}