package br.com.fiap.sus.resultados.domain.repository;

import br.com.fiap.sus.resultados.domain.model.ResultadoExame;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.Optional;
import java.util.UUID;

public interface ResultadoExameRepository {

    ResultadoExame salvar(ResultadoExame resultadoExame);

    Optional<ResultadoExame> buscarPorId(UUID id);

    /** RN-01/EX-02: usado pelo usecase de registro para checar EX-02 (segundo resultado). */
    boolean existePorExameId(UUID exameId);

    Optional<ResultadoExame> buscarPorExameId(UUID exameId);

    /** RF-07: listagem por paciente e periodo, restrita ao titular no controller. */
    PaginaResultado<ResultadoExame> listar(ResultadoExameFiltro filtro, int pagina, int tamanho);
}