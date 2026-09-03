package br.com.fiap.sus.cadastros.domain.repository;

import br.com.fiap.sus.cadastros.domain.model.Paciente;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PacienteRepository {

    Paciente salvar(Paciente paciente);

    Optional<Paciente> porId(UUID id);

    Optional<Paciente> porUsuarioId(UUID usuarioId);

    boolean existePorCartaoSus(String cartaoSus);

    boolean existePorUsuarioId(UUID usuarioId);

    /**
     * Busca paginada. Como nome e CPF vivem no modulo iam e nao ha join entre modulos
     * (Artigo III.3), o caso de uso resolve os ids de usuario correspondentes ao termo
     * e os informa em {@code usuarioIds}; aqui a comparacao local e pelo cartao SUS.
     */
    PaginaResultado<Paciente> buscar(String cartaoSus, Collection<UUID> usuarioIds, Boolean ativo,
                                     int pagina, int tamanho);
}
