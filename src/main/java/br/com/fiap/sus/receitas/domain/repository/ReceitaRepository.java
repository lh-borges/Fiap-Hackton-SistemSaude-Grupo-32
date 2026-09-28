package br.com.fiap.sus.receitas.domain.repository;

import br.com.fiap.sus.receitas.domain.model.Receita;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReceitaRepository {

    Receita salvar(Receita receita);

    Optional<Receita> buscarPorId(UUID id);

    /** RF-06: listagem por paciente, medico e situacao. */
    PaginaResultado<Receita> listar(ReceitaFiltro filtro, int pagina, int tamanho);

    /** Feature 010: todas as receitas do paciente, da mais recente para a mais antiga. */
    List<Receita> listarPorPaciente(UUID pacienteId);
}