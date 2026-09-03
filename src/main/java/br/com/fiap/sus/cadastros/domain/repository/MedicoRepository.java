package br.com.fiap.sus.cadastros.domain.repository;

import br.com.fiap.sus.cadastros.domain.model.Medico;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.Optional;
import java.util.UUID;

public interface MedicoRepository {

    Medico salvar(Medico medico);

    Optional<Medico> porId(UUID id);

    Optional<Medico> porUsuarioId(UUID usuarioId);

    boolean existePorCrmEUf(String crm, String ufCrm);

    boolean existePorUsuarioId(UUID usuarioId);

    PaginaResultado<Medico> buscar(UUID especialidadeId, Boolean ativo, int pagina, int tamanho);
}
