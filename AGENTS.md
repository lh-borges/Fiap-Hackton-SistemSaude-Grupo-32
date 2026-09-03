# Plataforma Digital de Atendimento SUS — Guia do repositório

Hackathon FIAP · Grupo 32 · Equipe: Luis, Thiago, Juliana, Gilmar, Danilo.

Este projeto é desenvolvido por **Spec-Driven Development**. A especificação é a fonte de
verdade; o código é consequência dela.

## Leia nesta ordem antes de qualquer tarefa

1. `.specify/memory/constitution.md` — regras não negociáveis do projeto.
2. `specs/000-plataforma-sus/spec.md` — o que o produto faz e para quem.
3. `specs/000-plataforma-sus/plan.md` — arquitetura, stack e convenções globais.
4. A `spec.md` e o `plan.md` da feature em que você vai mexer.

## Fluxo de trabalho

```
/specify <feature>   → specs/NNN-slug/spec.md     (o QUE e o PORQUÊ, sem tecnologia)
/plan NNN-slug       → specs/NNN-slug/plan.md     (o COMO: camadas, contratos, dados)
/tasks NNN-slug      → specs/NNN-slug/tasks.md    (tarefas ordenadas e paralelizáveis)
/implement NNN-slug  → código + testes
/analyze NNN-slug    → auditoria de consistência
```

Regra dura: **nenhum código de produção antes da spec aprovada**. Se a implementação
revelar que a spec está errada, corrija a spec primeiro.

## Arquitetura em uma frase

Monólito modular com Spring Modulith, onde cada módulo de negócio é uma microaplicação com
Clean Architecture própria, fronteira verificada em build e comunicação por porta publicada
(leitura) ou evento de domínio (reação).

### Regra da dependência

```
presentation → application → domain ← infrastructure
```

`domain` é Java puro: sem Spring, sem JPA, sem Jackson. Entidade de domínio não é entidade
JPA. Repositório é interface no `domain`, implementada em `infrastructure`.

### Regra de módulo

- Um módulo só expõe o pacote `api`. O resto é interno.
- Nada de FK entre tabelas de módulos diferentes — referência é `UUID` validado por porta.
- `notificacoes` não é importado por ninguém; ele só escuta eventos.

## Convenções rápidas

| Assunto | Convenção |
|---|---|
| Pacote raiz | `br.com.fiap.sus.<modulo>` |
| Rota | `/api/v1/<recurso-plural-em-portugues>` |
| Erro | RFC 7807 (`application/problem+json`) |
| PK | `UUID` gerada na aplicação |
| Tabela | `snake_case`, prefixo do módulo (`con_`, `exa_`, `res_`…) |
| Migration | `V<seq4>__<modulo>_<descricao>.sql`, faixa reservada por módulo |
| Enum no banco | `VARCHAR` com `CHECK` |
| Evento | `<Agregado><FatoNoPassado>Event`, tópico `sus.<modulo>.<evento>.v1` |
| Idioma | Domínio, API e documentação em português; termos técnicos em inglês |

## Segurança — inegociável

- JWT stateless; autorização com `@PreAuthorize` **no caso de uso**, não só no controller.
- Paciente só acessa o que é dele. O `pacienteId` vem do token, nunca da requisição.
- Recurso de terceiro responde `404`, não `403`.
- **Nunca logar** CPF, cartão SUS, resultado, laudo, diagnóstico ou medicamento.
- Segredo só por variável de ambiente.

## Build e ambiente

```bash
mvn verify          # compila, testa, verifica cobertura e fronteiras de módulo
docker compose up      # sobe Postgres, Kafka e a aplicação
```

Swagger em `/swagger-ui.html`. Perfis: `local` e `docker`. Não há Maven wrapper: use um `mvn` instalado ou o build via Docker.

## Divisão da equipe

| Pessoa | Frente | Features |
|---|---|---|
| Luis | Arquitetura e segurança | `001-iam-seguranca`, `002-cadastros` |
| Thiago | Consultas, exames, resultados, receitas | `003`, `004`, `005`, `007` |
| Juliana | Clínico e qualidade | `006-pareceres`, `008-documentos`, testes |
| Gilmar | Infraestrutura | Docker, Compose, Postgres, Flyway |
| Danilo | Mensageria e entrega | `009-notificacoes`, Kafka, relatório e vídeo |

## O que NÃO fazer

- Criar tabela `HISTORICO` — o histórico é derivado dos registros existentes.
- Usar entidade JPA como entidade de domínio.
- Chamar repositório direto do controller.
- Importar pacote interno de outro módulo.
- Implementar item de "evoluções futuras" sem repriorizar.
- Editar migration já aplicada.
