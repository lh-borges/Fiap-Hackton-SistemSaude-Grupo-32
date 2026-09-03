# Constituição do Projeto — Plataforma Digital de Atendimento SUS

> Documento normativo. Toda spec, plano, tarefa e código produzidos neste repositório
> devem obedecer aos artigos abaixo. Violação exige registro explícito na seção
> `Complexity Tracking` do `plan.md` da feature, com justificativa e alternativa descartada.

- **Versão:** 1.0.0
- **Ratificada em:** 2026-09-03
- **Equipe:** Luis (arquitetura/segurança) · Thiago (consultas/exames/receitas) · Juliana (clínico/qualidade) · Gilmar (infra) · Danilo (mensageria/entrega)

---

## Artigo I — Spec-Driven Development é obrigatório

1. **Nenhuma linha de código de produção antes da spec.** O fluxo é sempre
   `spec.md` → `plan.md` → `tasks.md` → implementação → verificação.
2. A `spec.md` descreve **O QUE e POR QUÊ** em linguagem de negócio. É proibido citar
   classe, framework, nome de tabela ou endpoint dentro dela.
3. O `plan.md` descreve **COMO**: stack, camadas, contratos, modelo de dados, eventos.
4. O `tasks.md` quebra o plano em tarefas executáveis, ordenadas por dependência,
   com marcação `[P]` para tarefas paralelizáveis (arquivos distintos).
5. Toda ambiguidade vira um marcador explícito `[NEEDS CLARIFICATION: pergunta]`.
   Uma spec com marcadores pendentes **não pode** avançar para o plano.
6. Mudança de escopo ocorre **na spec primeiro**; código que diverge da spec é bug.

## Artigo II — Clean Architecture (regra da dependência)

1. A dependência aponta **sempre para dentro**: `presentation → application → domain`,
   `infrastructure → application/domain`. O `domain` não depende de ninguém.
2. `domain` é **livre de framework**: proibidos `org.springframework.*`,
   `jakarta.persistence.*` e qualquer anotação de I/O. Permitido: Java puro.
3. Entidade de domínio **não é** entidade JPA. Persistência usa entidades próprias em
   `infrastructure/persistence/entity` mais mappers explícitos.
4. Repositórios são **portas** (interfaces) declaradas no `domain`, implementadas por
   adapters em `infrastructure`.
5. Casos de uso em `application` são unidades de intenção única, com input/output DTO
   próprios. Controller nunca fala com repositório.
6. DTO de API não atravessa a fronteira do `application` para o `domain`, e vice-versa.

## Artigo III — Modularização (Spring Modulith / "microaplicações")

1. O sistema é um **monólito modular** com Spring Modulith. Cada módulo de negócio é um
   pacote de primeiro nível sob `br.com.fiap.sus.<modulo>` e é candidato a extração
   futura para microserviço sem reescrita de domínio.
2. Cada módulo declara sua API pública em `package-info.java` com `@ApplicationModule`.
   Tudo fora dos pacotes exportados é **interno** e inacessível a outros módulos.
3. **Proibida chamada direta entre internals de módulos** e **proibida FK entre tabelas
   de módulos diferentes**. O acoplamento se dá por:
   - porta/interface publicada pelo módulo dono (síncrono, leitura), ou
   - **evento de domínio** (assíncrono, reação/notificação).
4. Referência entre módulos usa **identificador** (`UUID`), não objeto agregado.
5. `ApplicationModules.verify()` e ArchUnit rodam no CI. Build quebra em violação.
6. Módulos previstos: `iam`, `cadastros`, `consultas`, `exames`, `resultados`,
   `pareceres`, `receitas`, `documentos`, `notificacoes`, `shared`.

## Artigo IV — Segurança e privacidade de dado clínico

1. Autenticação stateless via **JWT**; autorização por role com Spring Security.
2. Roles: `ADMINISTRADOR`, `ATENDENTE`, `MEDICO`, `PACIENTE`. Autoridades no formato
   `ROLE_<NOME>`.
3. **Negar por padrão**: todo endpoint novo é negado até ter regra explícita.
   Autorização declarada no caso de uso (`@PreAuthorize`) e não apenas no controller.
4. **Ownership obrigatório**: `PACIENTE` só acessa dados cujo `pacienteId` seja o seu;
   `MEDICO` só edita o que ele mesmo produziu. Posse é regra de negócio testada, não
   filtro de query acidental.
5. Senha com BCrypt. Segredo nunca em código ou em `application.yml` versionado —
   apenas variável de ambiente, com default somente no perfil `local`.
6. **Nunca logar** dado pessoal ou clínico: CPF, cartão SUS, resultado, laudo,
   diagnóstico. Logs usam identificadores opacos.
7. Erro de autorização não vaza existência de recurso alheio: responde `404` quando a
   própria existência do recurso for informação sensível.

## Artigo V — Dados e migrations

1. **Flyway é a única fonte de verdade do schema.** `ddl-auto` fica em `validate`.
   Migration aplicada **nunca** é editada — corrige-se com nova migration.
2. Nomenclatura: `V<seq>__<modulo>_<descricao>.sql`, sequência global de 4 dígitos.
3. Tabelas e colunas em `snake_case`, tabelas no singular, conforme o modelo ER.
4. PK `UUID` gerada pela aplicação. Toda tabela tem `criado_em` e `atualizado_em`.
5. Enum persiste como `VARCHAR` (`@Enumerated(EnumType.STRING)`), nunca ordinal.
6. Exclusão de registro clínico é **lógica** (`ativo` / `status`), nunca física.

## Artigo VI — Mensageria e eventos

1. Evento de domínio é publicado pelo caso de uso via `ApplicationEventPublisher` dentro
   da transação; a externalização para Kafka ocorre depois do commit (`@Externalized`).
2. Nome do evento: `<Agregado><FatoNoPassado>Event`. Payload contém **IDs e metadados**,
   nunca conteúdo clínico completo.
3. Consumidor é **idempotente** e tolera reentrega (chave de deduplicação).
4. A aplicação deve subir e operar **sem Kafka disponível**; a notificação degrada, o
   fluxo clínico não.
5. Tópico: `sus.<modulo>.<evento>.v1`.

## Artigo VII — Qualidade e testes

1. Pirâmide: unitário (domínio e casos de uso, sem Spring) → integração
   (`@SpringBootTest` com Testcontainers) → módulo/contrato (Modulith).
2. **Cobertura mínima de 80%** em `domain` e `application` (JaCoCo, quebra o build).
3. Regra de negócio é testada em teste unitário **sem** banco e sem mock de framework.
4. Todo endpoint tem ao menos um teste de autorização negativa (role errada e paciente
   acessando dado de terceiro).
5. Toda tarefa de `tasks.md` marcada como testável declara o teste que a valida.

## Artigo VIII — API e contratos

1. REST sobre `/api/v1`, recursos no plural em português (`/api/v1/consultas`).
2. Erro segue **RFC 7807** (`application/problem+json`) com `type`, `title`, `status`,
   `detail`, `instance` e `errors[]` para validação.
3. Validação de entrada com Jakarta Bean Validation no DTO de request; regra de negócio
   nunca é validada apenas por anotação.
4. Listagem é sempre paginada (`page`, `size`, `sort`), com `size` máximo de 100.
5. OpenAPI/Swagger anotado; o contrato em `specs/*/contracts/` precede o código.
6. Datas em ISO-8601; instantes em UTC (`Instant`), data pura em `LocalDate`.

## Artigo IX — Ambiente reprodutível

1. `docker compose up` sobe Postgres, Kafka e a aplicação. Sem passo manual.
2. Nenhum host hardcoded: configuração por variável de ambiente com default local.
3. Perfis: `local`, `test`, `docker`. O perfil `test` usa Testcontainers.
4. README com o comando único para subir e exemplos de chamada da API.

## Artigo X — Simplicidade (YAGNI no MVP)

1. Não se cria abstração para um único implementador previsto.
2. Não se cria tabela `HISTORICO`: o histórico é derivado dos registros existentes.
3. Não se implementa item de "evoluções futuras" sem repriorização explícita.
4. Entre duas soluções equivalentes, vence a que remove código.

---

## Governança

- Emenda a esta constituição exige PR dedicado alterando este arquivo, com bump de versão
  semântico (MAJOR: remove ou reescreve artigo; MINOR: novo artigo ou regra;
  PATCH: redação).
- A revisão de código verifica conformidade constitucional **antes** de estilo.
- Em conflito entre a agilidade do hackathon e um artigo, registre a exceção em
  `plan.md` → `Complexity Tracking`. Exceção não registrada é bloqueio de merge.
