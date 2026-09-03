package br.com.fiap.sus.cadastros.application.usecase;

import br.com.fiap.sus.cadastros.application.dto.MedicoOutput;
import br.com.fiap.sus.cadastros.domain.model.Medico;
import br.com.fiap.sus.cadastros.domain.repository.MedicoRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Vinculo usuario -> medico da sessao. E como o cliente descobre o proprio medicoId. */
@Service
public class BuscarMeuCadastroDeMedicoUseCase {

    private final MedicoRepository medicos;
    private final BuscarMedicoUseCase buscarMedico;
    private final UsuarioAutenticadoProvider autenticado;

    public BuscarMeuCadastroDeMedicoUseCase(MedicoRepository medicos, BuscarMedicoUseCase buscarMedico,
                                            UsuarioAutenticadoProvider autenticado) {
        this.medicos = medicos;
        this.buscarMedico = buscarMedico;
        this.autenticado = autenticado;
    }

    @Transactional(readOnly = true)
    public MedicoOutput executar() {
        Medico medico = medicos.porUsuarioId(autenticado.obrigatorio().id())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nao ha cadastro de medico vinculado a este usuario."));
        return buscarMedico.montar(medico);
    }
}
