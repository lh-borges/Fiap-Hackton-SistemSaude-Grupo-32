package br.com.fiap.sus.documentos.application.service;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.documentos.domain.model.DocumentoMedico;
import br.com.fiap.sus.documentos.domain.repository.DocumentoMedicoFiltro;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DocumentoLeituraService {
    private final CadastroQuery cadastros;
    private final ConsultaQuery consultas;

    public DocumentoLeituraService(CadastroQuery cadastros, ConsultaQuery consultas) {
        this.cadastros = cadastros;
        this.consultas = consultas;
    }

    public Set<UUID> pacientesPermitidos(UsuarioAutenticado usuario) {
        if (usuario.ehAdministrador()) {
            return null;
        }
        if (usuario.ehPaciente()) {
            return Set.of(cadastros.pacienteIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado para este usuario.")));
        }
        UUID medicoId = medicoIdDoUsuario(usuario);
        return Set.copyOf(consultas.pacientesDoMedico(medicoId));
    }

    public UUID medicoIdDoUsuario(UsuarioAutenticado usuario) {
        return cadastros.medicoIdDoUsuario(usuario.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Médico não encontrado para este usuario."));
    }

    public void verificarAcesso(DocumentoMedico documento, UsuarioAutenticado usuario) {
        if (usuario.ehAdministrador()) {
            return;
        }
        if (usuario.ehMedico() && documento.getMedicoId().equals(medicoIdDoUsuario(usuario))) {
            return;
        }
        var permitidos = pacientesPermitidos(usuario);
        if (permitidos != null && !permitidos.contains(documento.getPacienteId())) {
            throw new RecursoNaoEncontradoException("Documento não encontrado.");
        }
    }

    public DocumentoMedicoFiltro restringir(DocumentoMedicoFiltro filtro, UsuarioAutenticado usuario) {
        if (usuario.ehAdministrador()) {
            return new DocumentoMedicoFiltro(filtro.pacienteId(), filtro.medicoId(), filtro.tipo(),
                    filtro.periodoInicio(), filtro.periodoFim());
        }
        if (usuario.ehPaciente()) {
            UUID pacienteId = cadastros.pacienteIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado para este usuario."));
            return new DocumentoMedicoFiltro(pacienteId, filtro.medicoId(), filtro.tipo(),
                    filtro.periodoInicio(), filtro.periodoFim(), Set.of(pacienteId));
        }
        UUID medicoId = medicoIdDoUsuario(usuario);
        return new DocumentoMedicoFiltro(filtro.pacienteId(), medicoId, filtro.tipo(),
                filtro.periodoInicio(), filtro.periodoFim());
    }
}
