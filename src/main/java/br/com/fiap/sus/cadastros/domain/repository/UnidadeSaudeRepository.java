package br.com.fiap.sus.cadastros.domain.repository;

import br.com.fiap.sus.cadastros.domain.model.UnidadeSaude;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.Optional;
import java.util.UUID;

public interface UnidadeSaudeRepository {

    UnidadeSaude salvar(UnidadeSaude unidade);

    Optional<UnidadeSaude> porId(UUID id);

    boolean existePorCnes(String cnes);

    PaginaResultado<UnidadeSaude> listar(Boolean ativo, int pagina, int tamanho);
}
