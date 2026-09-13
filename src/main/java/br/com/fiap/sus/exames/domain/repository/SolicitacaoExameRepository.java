package br.com.fiap.sus.exames.domain.repository;

import br.com.fiap.sus.exames.domain.model.SolicitacaoExame;
import br.com.fiap.sus.shared.domain.PaginaResultado;

import java.util.Optional;
import java.util.UUID;

public interface SolicitacaoExameRepository {

    SolicitacaoExame salvar(SolicitacaoExame solicitacao);

    Optional<SolicitacaoExame> buscarPorId(UUID id);

    PaginaResultado<SolicitacaoExame> listar(SolicitacaoExameFiltro filtro, int pagina, int tamanho);
}