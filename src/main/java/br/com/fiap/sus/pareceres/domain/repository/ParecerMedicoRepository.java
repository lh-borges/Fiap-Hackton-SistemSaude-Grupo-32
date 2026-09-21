package br.com.fiap.sus.pareceres.domain.repository;

import br.com.fiap.sus.pareceres.domain.model.ParecerMedico;
import br.com.fiap.sus.shared.domain.PaginaResultado;

import java.util.Optional;
import java.util.UUID;

public interface ParecerMedicoRepository {

    ParecerMedico salvar(ParecerMedico parecer, UUID criadoPorUsuarioId);

    Optional<ParecerMedico> buscarPorId(UUID id);

    java.util.Set<UUID> resultadosComParecer(java.util.Set<UUID> resultadoExameIds);

    /** RF-08 (consumido pelo modulo resultados): indica se ja existe parecer para o resultado. */
    boolean existePorResultadoExameId(UUID resultadoExameId);

    /** RF-05: listagem por paciente, medico, resultado e periodo. */
    PaginaResultado<ParecerMedico> listar(ParecerMedicoFiltro filtro, int pagina, int tamanho);
}
