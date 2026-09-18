package br.com.fiap.sus.pareceres.application.service;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.consultas.api.ConsultaQuery;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.pareceres.application.dto.ParecerMedicoOutput;
import br.com.fiap.sus.pareceres.domain.model.ParecerMedico;
import br.com.fiap.sus.pareceres.domain.repository.ParecerMedicoFiltro;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticado;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ParecerLeituraService {
    private final CadastroQuery cadastros;
    private final ConsultaQuery consultas;
    private final IamQuery usuarios;

    public ParecerLeituraService(CadastroQuery cadastros, ConsultaQuery consultas, IamQuery usuarios) {
        this.cadastros = cadastros;
        this.consultas = consultas;
        this.usuarios = usuarios;
    }

    public Set<UUID> pacientesPermitidos(UsuarioAutenticado usuario) {
        if (usuario.ehAdministrador()) {
            return null;
        }
        if (usuario.ehPaciente()) {
            return Set.of(cadastros.pacienteIdDoUsuario(usuario.id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente nao encontrado para este usuario.")));
        }
        UUID medicoId = cadastros.medicoIdDoUsuario(usuario.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medico nao encontrado para este usuario."));
        return Set.copyOf(consultas.pacientesDoMedico(medicoId));
    }

    public void verificarAcesso(ParecerMedico parecer, UsuarioAutenticado usuario) {
        var permitidos = pacientesPermitidos(usuario);
        if (permitidos != null && !permitidos.contains(parecer.getPacienteId())) {
            throw new RecursoNaoEncontradoException("Parecer nao encontrado.");
        }
    }

    public ParecerMedicoFiltro restringir(ParecerMedicoFiltro filtro, UsuarioAutenticado usuario) {
        var permitidos = pacientesPermitidos(usuario);
        UUID pacienteId = usuario.ehPaciente() && !usuario.ehAdministrador()
                ? permitidos.iterator().next() : filtro.pacienteId();
        return new ParecerMedicoFiltro(pacienteId, filtro.medicoId(), filtro.resultadoExameId(),
                filtro.periodoInicio(), filtro.periodoFim(), permitidos);
    }

    public ParecerMedicoOutput output(ParecerMedico parecer, UsuarioAutenticado usuario) {
        var medico = cadastros.resumoDoMedico(parecer.getMedicoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medico nao encontrado."));
        String nome = usuarios.resumoDoUsuario(medico.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario do medico nao encontrado.")).nome();
        return new ParecerMedicoOutput(parecer.getId(), parecer.getResultadoExameId(), parecer.getPacienteId(),
                parecer.getMedicoId(), medico.especialidadeId(), medico.crm(), nome,
                usuario.ehAdministrador() ? null : parecer.getDescricao(), parecer.getDataParecer());
    }
}
