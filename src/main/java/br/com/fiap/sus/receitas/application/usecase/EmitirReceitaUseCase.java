package br.com.fiap.sus.receitas.application.usecase;

import br.com.fiap.sus.cadastros.api.CadastroQuery;
import br.com.fiap.sus.receitas.application.dto.EmitirReceitaDTO;
import br.com.fiap.sus.receitas.application.dto.ReceitaOutput;
import br.com.fiap.sus.receitas.domain.model.ItemReceita;
import br.com.fiap.sus.receitas.domain.model.Receita;
import br.com.fiap.sus.receitas.domain.repository.ReceitaRepository;
import br.com.fiap.sus.shared.domain.exception.RecursoNaoEncontradoException;
import br.com.fiap.sus.shared.infrastructure.security.UsuarioAutenticadoProvider;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/** HU-01/RF-01/RF-02/RN-01/RN-02: so medico emite receita. */
@Component
public class EmitirReceitaUseCase {

    private final ReceitaRepository receitaRepository;
    private final CadastroQuery cadastroQuery;
    private final UsuarioAutenticadoProvider usuarioAutenticadoProvider;

    public EmitirReceitaUseCase(ReceitaRepository receitaRepository, CadastroQuery cadastroQuery,
                                UsuarioAutenticadoProvider usuarioAutenticadoProvider) {
        this.receitaRepository = receitaRepository;
        this.cadastroQuery = cadastroQuery;
        this.usuarioAutenticadoProvider = usuarioAutenticadoProvider;
    }

    @PreAuthorize("hasRole('MEDICO')")
    public ReceitaOutput executar(EmitirReceitaDTO dto) {
        var usuario = usuarioAutenticadoProvider.obrigatorio();
        var medicoId = cadastroQuery.medicoIdDoUsuario(usuario.id())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Medico nao encontrado para este usuario."));

        if (!cadastroQuery.pacienteAtivoExiste(dto.pacienteId())) {
            throw new RecursoNaoEncontradoException("Paciente nao encontrado ou inativo.");
        }

        List<ItemReceita> itens = dto.itens().stream()
                .map(i -> ItemReceita.criar(i.medicamento(), i.dosagem(), i.frequencia(), i.duracao(),
                        i.orientacao()))
                .toList();

        Receita receita = Receita.emitir(dto.pacienteId(), medicoId, dto.consultaId(), itens,
                dto.validade(), dto.observacao());

        return ReceitaOutput.de(receitaRepository.salvar(receita));
    }
}