package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.CadastrarPacienteInput;
import br.com.fiap.sus.cadastros.application.dto.PacienteOutput;
import br.com.fiap.sus.cadastros.domain.enums.Sexo;
import br.com.fiap.sus.cadastros.domain.model.Paciente;
import br.com.fiap.sus.cadastros.domain.repository.PacienteRepository;
import br.com.fiap.sus.iam.api.IamQuery;
import br.com.fiap.sus.iam.api.UsuarioResumo;
import br.com.fiap.sus.shared.domain.exception.ConflitoException;
import br.com.fiap.sus.shared.domain.exception.RegraDeNegocioException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-01: cadastro de paciente por atendente ou administrador. */
@Service
public class CadastrarPacienteUseCase {

    private final PacienteRepository pacientes;
    private final IamQuery iam;

    public CadastrarPacienteUseCase(PacienteRepository pacientes, IamQuery iam) {
        this.pacientes = pacientes;
        this.iam = iam;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ATENDENTE')")
    public PacienteOutput executar(CadastrarPacienteInput input) {
        // RN-04: o usuario precisa existir, estar ativo e possuir o perfil PACIENTE.
        if (!iam.usuarioAtivoPossuiRole(input.usuarioId(), "PACIENTE")) {
            throw new RegraDeNegocioException(
                    "O usuario informado nao existe, esta inativo ou nao possui o perfil PACIENTE.");
        }
        // RN-01: no maximo um cadastro de paciente por usuario.
        if (pacientes.existePorUsuarioId(input.usuarioId())) {
            throw new ConflitoException("Este usuario ja possui cadastro de paciente.");
        }

        Paciente paciente = Paciente.criar(input.usuarioId(), input.cartaoSus(), input.dataNascimento(),
                Sexo.de(input.sexo()), input.tipoSanguineo());

        // RN-02: cartao SUS unico. Validado apos a construcao para que o formato seja checado antes.
        if (pacientes.existePorCartaoSus(paciente.getCartaoSus())) {
            throw new ConflitoException("Ja existe um paciente com este cartao SUS.");
        }

        Paciente salvo = pacientes.salvar(paciente);
        UsuarioResumo usuario = iam.resumoDoUsuario(salvo.getUsuarioId()).orElse(null);
        return PacienteOutput.de(salvo,
                usuario == null ? null : usuario.nome(),
                usuario == null ? null : usuario.email());
    }
}
