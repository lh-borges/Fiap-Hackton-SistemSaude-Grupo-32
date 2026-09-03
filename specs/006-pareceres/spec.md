# Especificação — Parecer médico

- **ID:** `006-pareceres` · **Módulo:** `pareceres` · **Responsável:** Juliana
- **Status:** Em revisão · **Criada em:** 2026-09-03
- **Herda de:** `specs/000-plataforma-sus/spec.md`

## 1. Contexto e problema

O resultado diz o que foi medido; ele não diz o que aquilo significa para o paciente. A
interpretação é um ato médico com autoria e responsabilidade, e precisa ser registrada
separadamente do dado técnico — inclusive porque dois médicos podem interpretar o mesmo
resultado. Essa separação é o princípio central da plataforma.

## 2. Objetivo

Permitir que um médico registre e disponibilize a interpretação clínica de um resultado de
exame, sem alterar o resultado, com autoria e data rastreáveis.

## 3. Fora de escopo

- Assinatura digital do parecer.
- Modelos e textos pré-configurados de laudo.
- Conclusão automática ou sugestão por IA.
- Fluxo de revisão/aprovação por um segundo médico.

## 4. Atores e permissões

| Ator | O que pode fazer |
|---|---|
| ADMINISTRADOR | Consultar pareceres para fins administrativos |
| ATENDENTE | Nenhum acesso ao conteúdo do parecer |
| MEDICO | Criar parecer sobre resultado; consultar pareceres dos pacientes que atende |
| PACIENTE | Consultar os pareceres emitidos sobre os próprios resultados |

## 5. Histórias de usuário

### HU-01 — Emitir parecer
```gherkin
Dado que sou médico autenticado
E existe um resultado de exame registrado
Quando registro um parecer com a descrição da minha interpretação
Então o parecer é criado com a minha autoria e a data
E o paciente é notificado
E o resultado do exame permanece inalterado
```

### HU-02 — Só médico interpreta
```gherkin
Dado que sou atendente ou paciente
Quando tento registrar um parecer
Então a operação é negada
```

### HU-03 — Segunda opinião
```gherkin
Dado que um resultado já possui parecer de um médico
Quando outro médico registra o seu parecer sobre o mesmo resultado
Então ambos os pareceres coexistem, cada um com seu autor e sua data
```

### HU-04 — Imutabilidade
```gherkin
Dado que um parecer foi registrado
Quando se tenta alterá-lo ou excluí-lo
Então a operação é negada
E, se houver erro, um novo parecer retificador pode ser registrado
```

### HU-05 — Leitura pelo paciente
```gherkin
Dado que sou paciente autenticado
Quando consulto os pareceres sobre meus resultados
Então vejo a descrição, o médico responsável e a data
Quando tento consultar parecer de outro paciente
Então o registro é tratado como inexistente
```

## 6. Requisitos funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | O sistema DEVE permitir ao médico registrar parecer vinculado a um resultado de exame | Obrigatório |
| RF-02 | O sistema DEVE registrar autor, paciente, resultado analisado, descrição e data | Obrigatório |
| RF-03 | O sistema DEVE impedir alteração e exclusão de parecer | Obrigatório |
| RF-04 | O sistema DEVE permitir mais de um parecer por resultado | Obrigatório |
| RF-05 | O sistema DEVE listar pareceres por paciente, por médico e por resultado | Obrigatório |
| RF-06 | O sistema DEVE restringir a leitura do paciente aos pareceres sobre seus resultados | Obrigatório |
| RF-07 | O sistema DEVE notificar o paciente quando um parecer for emitido | Obrigatório |
| RF-08 | O sistema DEVE indicar, na consulta de um resultado, se já existe parecer | Obrigatório |

## 7. Regras de negócio

| ID | Regra |
|---|---|
| RN-01 | Somente usuário com perfil MEDICO registra parecer |
| RN-02 | O parecer só é registrado sobre resultado existente |
| RN-03 | O paciente do parecer é sempre o titular do resultado; não é informado pelo requisitante |
| RN-04 | Descrição obrigatória, mínimo de 10 e máximo de 5000 caracteres |
| RN-05 | Parecer é imutável após criado |
| RN-06 | O parecer nunca altera o resultado do exame |
| RN-07 | Médico inativo não emite parecer |

## 8. Fluxos de exceção

| ID | Situação | Comportamento |
|---|---|---|
| EX-01 | Não-médico tenta emitir | Negar |
| EX-02 | Resultado inexistente | Tratar como inexistente |
| EX-03 | Tentativa de edição ou exclusão | Negar com explicação da imutabilidade |
| EX-04 | Descrição vazia ou curta demais | Rejeitar por validação |
| EX-05 | Paciente acessa parecer de terceiro | Tratar como inexistente |

## 9. Conceitos de domínio

- **Parecer médico:** interpretação clínica de um resultado. Resultado analisado, médico
  responsável, paciente, descrição, data do parecer.

## 10. Eventos produzidos

| Evento | Quando ocorre | Quem se interessa |
|---|---|---|
| Parecer criado | Médico conclui a análise | Paciente |

## 11. Requisitos não funcionais

- A descrição do parecer é dado clínico: **nunca** em log nem em payload de evento.
- A resposta de listagem para o paciente traz o nome e o CRM do médico responsável.
- Data de criação imutável, gerada pelo sistema (não aceita do cliente).

## 12. Definition of Done

- [ ] RF-01 a RF-08 implementados e testados
- [ ] Teste: atendente e paciente não conseguem emitir parecer
- [ ] Teste: tentativa de edição/exclusão é negada
- [ ] Teste: o resultado permanece byte a byte igual após a criação do parecer
- [ ] Teste: paciente não lê parecer de terceiro
- [ ] Evento publicado sem conteúdo clínico
- [ ] Contrato OpenAPI publicado

## 13. Pendências

- [NEEDS CLARIFICATION: apenas o médico que solicitou o exame pode emitir parecer, ou qualquer médico ativo? (Assumido: qualquer médico ativo, para permitir análise remota.)]
- [NEEDS CLARIFICATION: o administrador vê o conteúdo do parecer ou apenas a existência e os metadados?]

## 14. Dependências

- Depende de: `001`, `002`, `005`
