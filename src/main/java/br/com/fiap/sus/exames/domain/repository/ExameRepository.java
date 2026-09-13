package br.com.fiap.sus.exames.domain.repository;

import br.com.fiap.sus.exames.domain.model.Exame;
import br.com.fiap.sus.shared.domain.PaginaResultado;

import java.util.Optional;
import java.util.UUID;

public interface ExameRepository {

    Exame salvar(Exame exame);

    Optional<Exame> buscarPorId(UUID id);

    /** RN-02: usado antes de agendar, para garantir no maximo um exame ativo por solicitacao. */
    boolean existeExameAtivoParaSolicitacao(UUID solicitacaoExameId);

    Optional<Exame> buscarPorSolicitacaoId(UUID solicitacaoExameId);

    PaginaResultado<Exame> listar(ExameFiltro filtro, int pagina, int tamanho);
}