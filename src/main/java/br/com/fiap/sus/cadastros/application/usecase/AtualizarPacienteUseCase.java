package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.AtualizarPacienteInput;
import br.com.fiap.sus.cadastros.application.dto.PacienteOutput;
import br.com.fiap.sus.cadastros.domain.enums.Sexo;
import br.com.fiap.sus.cadastros.domain.model.Paciente;
import br.com.fiap.sus.cadastros.domain.repository.PacienteRepository;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.iam.api.UsuarioResumo;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Correcao cadastral. O paciente nao altera os proprios dados no MVP
 * (decisao registrada em specs/002-cadastros/plan.md, secao 10).
 */
@Service
public class AtualizarPacienteUseCase {

    private final PacienteRepository pacientes;
    private final IamQuery iam;

    public AtualizarPacienteUseCase(PacienteRepository pacientes, IamQuery iam) {
        this.pacientes = pacientes;
        this.iam = iam;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE')")
    public PacienteOutput executar(UUID id, AtualizarPacienteInput input) {
        Paciente paciente = pacientes.porId(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Paciente"));
        paciente.alterarDados(input.dataNascimento(), Sexo.de(input.sexo()), input.tipoSanguineo());

        Paciente salvo = pacientes.salvar(paciente);
        UsuarioResumo usuario = iam.resumoDoUsuario(salvo.getUsuarioId()).orElse(null);
        return PacienteOutput.de(salvo,
                usuario == null ? null : usuario.nome(),
                usuario == null ? null : usuario.email());
    }
}
