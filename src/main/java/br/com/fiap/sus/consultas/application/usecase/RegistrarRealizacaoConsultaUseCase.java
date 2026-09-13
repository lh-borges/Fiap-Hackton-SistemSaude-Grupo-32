package br.com.fiap.sus.consultas.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.application.dto.ConsultaOutput;
import br.com.fiap.sus.consultas.application.dto.RegistrarRealizacaoConsultaDTO;
import br.com.fiap.sus.consultas.domain.model.Consulta;
import br.com.fiap.sus.consultas.domain.repository.ConsultaRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/** HU-04/RF-04/RN-05: so o medico da consulta registra a realizacao. */
@Component
public class RegistrarRealizacaoConsultaUseCase {

    private final ConsultaRepository consultaRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public RegistrarRealizacaoConsultaUseCase(ConsultaRepository consultaRepository,
                                              CadastroQuery cadastroQuery,
                                              UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.consultaRepository = consultaRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("hasRole('MEDICO')")
    public ConsultaOutput executar(RegistrarRealizacaoConsultaDTO dto) {
        Consulta consulta = consultaRepository.buscarPorId(dto.consultaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta nao encontrada."));

        var usuario = usuarioAutenticadoProvider.obrigatorio();
        var medicoId = cadastroQuery.medicoIdDoUsuario(usuario.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medico nao encontrado para este usuario."));

        consulta.registrarRealizacao(medicoId, dto.observacoes());
        return ConsultaOutput.de(consultaRepository.salvar(consulta));
    }
}