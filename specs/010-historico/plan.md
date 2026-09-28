# Plano Técnico — Histórico do paciente

- **Spec:** `./spec.md` · **Responsável:** Danilo · **Status:** Aprovado
- **Módulo:** `br.com.fiap.sus.historico` — somente leitura, sem tabela, sem migration, sem evento.
- **Plano-mãe:** `specs/000-plataforma-sus/plan.md`, seção 3 já prevê
  `historico → shared + portas de leitura dos módulos clínicos`.

---

## 1. Portão constitucional

| Artigo | Verificação | OK? |
|---|---|---|
| I — SDD | Spec aprovada sem pendência; decisões registradas na seção 12 da spec | [x] |
| II — Clean Arch | `domain` é Java puro; `FonteHistorico` é porta de saída no domínio, implementada em `infrastructure` | [x] |
| III — Modulith | Depende só de `shared` e do `api` de `cadastros`, `consultas`, `exames`, `resultados`, `pareceres`, `receitas` e `documentos`. Sem FK, sem tabela | [x] |
| IV — Segurança | `@PreAuthorize` no caso de uso; `pacienteId` do PACIENTE vem do token; paciente não atendido responde 404; item sem conteúdo clínico | [x] |
| V — Dados | Nenhuma migration; nenhuma entidade JPA | [x] |
| VI — Eventos | Não publica nem consome | [x] |
| VII — Testes | Domínio, caso de uso e serviço de acesso testados sem Spring; controller com MockMvc e método de segurança | [x] |
| VIII — API | `GET /api/v1/historico`; RFC 7807 pelo handler de `shared`; página com `conteudo`, `pagina`, `tamanho`, `totalElementos`, `totalPaginas` | [x] |
| X — Simplicidade | Ordenação e paginação em memória; sem cache; sem DTO intermediário entre domínio e apresentação | [x] |

## 2. Decisão: onde montar a linha do tempo

| Opção | Prós | Contras | Decisão |
|---|---|---|---|
| **A. Módulo `historico` lendo as portas `api` dos módulos clínicos** | Segue o plano-mãe; cada módulo continua dono da sua tabela; fronteira verificada em build; sem migration | Exige uma porta nova por módulo (7 leituras) | **Escolhida** |
| B. Rota em `cadastros` (`/pacientes/{id}/historico`) | Rota REST "natural" | `cadastros` passaria a depender dos módulos clínicos, que já dependem dele: ciclo | Rejeitada |
| C. Consulta SQL única (view ou `UNION`) sobre as sete tabelas | Menos código; ordenação no banco | Viola a fronteira de módulo (lê tabela alheia); acopla o histórico ao schema dos outros seis módulos | Rejeitada |
| D. Projeção alimentada pelos eventos de `009` | Escala; leitura O(1) | É uma tabela de histórico, proibida pela spec-mãe; só cobre fatos que geram evento (cancelamento de exame e de receita não geram) | Rejeitada |

## 3. Estrutura de pacotes

```
br.com.fiap.sus.historico
├── package-info.java            @ApplicationModule(allowedDependencies = {"shared", "cadastros::api",
│                                  "consultas::api", "exames::api", "resultados::api",
│                                  "pareceres::api", "receitas::api", "documentos::api"})
├── domain
│   ├── enums/TipoRegistro       CONSULTA, SOLICITACAO_EXAME, EXAME, RESULTADO_EXAME, PARECER, RECEITA, DOCUMENTO
│   │                            cada um com recurso() → nome da rota do módulo dono
│   ├── model/RegistroHistorico  record imutável; valida obrigatórios
│   ├── model/Referencia         (tipo, id) do registro relacionado
│   ├── model/HistoricoFiltro    tipos + período; aceita(registro); valida início ≤ fim
│   ├── model/LinhaDoTempo       ordena (RN-02, RN-07), filtra, pagina → PaginaResultado
│   └── port/FonteHistorico      List<RegistroHistorico> registrosDoPaciente(UUID)
├── application
│   ├── service/AcessoHistoricoService   resolve o paciente autorizado por perfil (seção 5)
│   └── usecase/ConsultarHistoricoUseCase  @PreAuthorize; junta as fontes; devolve PaginaResultado
├── infrastructure
│   └── fonte/                   uma classe por módulo de origem, cada uma implements FonteHistorico:
│       ConsultasFonte, ExamesFonte (solicitações + exames), ResultadosFonte,
│       PareceresFonte, ReceitasFonte, DocumentosFonte
└── presentation
    ├── controller/HistoricoController
    └── response/RegistroHistoricoResponse, ReferenciaResponse
```

Sem `dto` na aplicação: o caso de uso devolve `PaginaResultado<RegistroHistorico>` e a
apresentação converte. Sem `repository` no domínio: não há persistência.

## 4. Modelo de domínio

```java
record RegistroHistorico(TipoRegistro tipo, UUID id, Instant data, String titulo,
                         String situacao, UUID medicoId, Referencia origem)
record Referencia(TipoRegistro tipo, UUID id)
record HistoricoFiltro(Set<TipoRegistro> tipos, Instant inicio, Instant fim)
final class LinhaDoTempo { static de(List<RegistroHistorico>); filtrar(HistoricoFiltro); pagina(int, int) }
```

Ordem: `data` desc, depois `tipo` (ordinal asc), depois `id`. Título por tipo e situação,
sem dado clínico (ex.: "Consulta cancelada", "Exame realizado", "Resultado de exame
(imagem)", "Receita renovada", "Documento: atestado").

## 5. Casos de uso e acesso

`ConsultarHistoricoUseCase.executar(UUID pacienteIdSolicitado, HistoricoFiltro filtro, int pagina, int tamanho)`
com `@PreAuthorize("hasAnyRole('PACIENTE', 'MEDICO', 'ADMINISTRADOR')")`.

| Perfil | Paciente efetivo | Falha |
|---|---|---|
| PACIENTE | `CadastroQuery.pacienteIdDoUsuario(token.id)`; parâmetro ignorado | sem paciente vinculado → 404 |
| MEDICO | parâmetro obrigatório; `CadastroQuery.medicoIdDoUsuario` e `ConsultaQuery.pacientesDoMedico` deve conter o paciente | parâmetro ausente → 400; não atende → 404 |
| ADMINISTRADOR | parâmetro obrigatório; `CadastroQuery.resumoDoPaciente` presente | ausente → 400; inexistente → 404 |

Depois de resolver o paciente, o caso de uso chama todas as `FonteHistorico` injetadas
(`List<FonteHistorico>`), monta a `LinhaDoTempo`, filtra e pagina.

## 6. Contrato REST

`GET /api/v1/historico` — contrato completo em `contracts/openapi.yaml`.

| Parâmetro | Tipo | Regra |
|---|---|---|
| `pacienteId` | UUID | Obrigatório para MEDICO e ADMINISTRADOR; ignorado para PACIENTE |
| `tipo` | lista de `TipoRegistro` | Opcional, repetível |
| `inicio`, `fim` | date-time | Opcionais; `inicio` > `fim` → 400 |
| `pagina`, `tamanho` | int | `0..`, `1..100` (padrão 20) |

Item da resposta: `tipo`, `id`, `recurso`, `data`, `titulo`, `situacao`, `medicoId`,
`origem {tipo, recurso, id}` (nulo quando não há relacionado).

## 7. Portas novas nos módulos clínicos

Todas somente leitura, no pacote `api` do módulo dono, devolvendo `record`s sem conteúdo
clínico. Implementação segue o padrão que cada módulo já usa.

| Módulo | Porta | Implementação |
|---|---|---|
| `consultas` | `ConsultaQuery.consultasDoPaciente(UUID) → List<ConsultaResumo>` | `ConsultaQueryAdapter` + `ConsultaJpaRepository.findByPacienteId` |
| `exames` | `ExameQuery.solicitacoesDoPaciente(UUID) → List<SolicitacaoExameResumo>`; `examesDoPaciente(UUID) → List<ExameResumo>` | `ExameQueryAdapter` + queries JPA (exame não tem `pacienteId`; junta pela solicitação) |
| `resultados` | `ResultadoQuery.resultadosDoPaciente(UUID) → List<ResultadoResumo>` | `ResultadoQueryService` + `ResultadoExameRepository.listarPorPaciente` |
| `pareceres` | `ParecerQuery.pareceresDoPaciente(UUID) → List<ParecerResumo>` | `ParecerQueryService` + `ParecerMedicoRepository.listarPorPaciente` |
| `receitas` | **novo pacote `api`**: `ReceitaQuery.receitasDoPaciente(UUID) → List<ReceitaResumo>` | `ReceitaQueryService` (novo) + `ReceitaRepository.listarPorPaciente` |
| `documentos` | `DocumentoQuery.documentosDoPaciente(UUID) → List<DocumentoResumo>` | `DocumentoQueryService` + `DocumentoMedicoRepository.listarPorPaciente` |

`ResultadoResumo` já existe e carrega `laudo`/`descricao` para uso de `pareceres`; a fonte
do histórico descarta esses campos ao montar o item (RN-06).

## 8. Estratégia de testes

| Nível | O quê | Onde |
|---|---|---|
| Unitário (domínio) | ordem, desempate, filtro por tipo e período, paginação, validação de filtro e de registro | `historico/domain` |
| Unitário (aplicação) | acesso por perfil (HU-02, HU-03, EX-01, EX-02, EX-04); caso de uso junta as fontes e aplica filtro | `historico/application` |
| Unitário (infra) | cada fonte converte o resumo do módulo em registro com título, data (RN-05) e origem certos, sem conteúdo clínico | `historico/infrastructure/fonte` |
| Endpoint | MockMvc com `@EnableMethodSecurity`: PACIENTE ignora `pacienteId`; MEDICO sem parâmetro → 400; ATENDENTE → 403; item serializado com `recurso` e `origem` | `historico/presentation` |
| Portas novas | serviço de leitura de cada módulo com repositório mockado; queries JPA de `consultas` e `exames` com `@DataJpaTest` + H2 | módulo dono |
| Módulo/arquitetura | `ModularidadeTest` e `ArquiteturaTest` seguem verdes | existentes |

## 9. Riscos e decisões

| Risco | Mitigação |
|---|---|
| Paginação em memória com paciente de histórico longo | Volume do MVP é pequeno; cada fonte devolve só campos leves. Se crescer, o passo seguinte é limitar por período na fonte, não criar tabela |
| Regra "meu paciente" do médico depende de consulta | É a regra já vigente em `documentos`; centralizar em uma porta única fica como evolução |
| `ResultadoResumo` expõe laudo para quem chama a porta | O contrato interno já era assim; a fonte descarta. Evolução: resumo leve separado |

## 10. Complexity Tracking (exceções à constituição)

| Artigo | Exceção | Por quê | Alternativa rejeitada |
|---|---|---|---|
| III (plano-mãe: dono `Thiago`) | Implementado por Danilo | Entrega final; RF-11 estava sem dono efetivo | Deixar RF-11 fora do MVP |
| VII.1 (Testcontainers) | H2 em `@DataJpaTest` para as queries novas | Prática vigente nos nove módulos | Testcontainers só aqui |
