package br.com.fiap.sus.consultas.domain.repository;

import br.com.fiap.sus.consultas.domain.model.Consulta;
import br.com.fiap.sus.shared.domain.PaginaResultado;
import java.util.Optional;
import java.util.UUID;

public interface ConsultaRepository {

    Consulta salvar(Consulta consulta);

    Optional<Consulta> buscarPorId(UUID id);

    /** RF-05: filtro por paciente, medico, unidade, situacao e periodo. */
    PaginaResultado<Consulta> listar(ConsultaFiltro filtro, int pagina, int tamanho);
}