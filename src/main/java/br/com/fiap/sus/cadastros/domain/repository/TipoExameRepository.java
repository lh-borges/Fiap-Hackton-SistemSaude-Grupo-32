package br.com.fiap.sus.cadastros.domain.repository;

import br.com.fiap.sus.cadastros.domain.enums.CategoriaExame;
import br.com.fiap.sus.cadastros.domain.model.TipoExame;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.Optional;
import java.util.UUID;

public interface TipoExameRepository {

    TipoExame salvar(TipoExame tipoExame);

    Optional<TipoExame> porId(UUID id);

    boolean existePorNome(String nome);

    PaginaResultado<TipoExame> listar(CategoriaExame categoria, Boolean ativo, int pagina, int tamanho);
}
