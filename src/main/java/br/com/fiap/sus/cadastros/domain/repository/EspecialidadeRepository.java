package br.com.fiap.sus.cadastros.domain.repository;

import br.com.fiap.sus.cadastros.domain.model.Especialidade;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.Optional;
import java.util.UUID;

public interface EspecialidadeRepository {

    Especialidade salvar(Especialidade especialidade);

    Optional<Especialidade> porId(UUID id);

    boolean existePorNome(String nome);

    PaginaResultado<Especialidade> listar(Boolean ativo, int pagina, int tamanho);
}
