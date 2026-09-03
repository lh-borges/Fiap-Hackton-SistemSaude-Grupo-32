# Especificação — Usuários, autenticação e perfis

- **ID:** `001-iam-seguranca` · **Módulo:** `iam` · **Responsável:** Luis
- **Status:** Em revisão · **Criada em:** 2026-09-03
- **Herda de:** `specs/000-plataforma-sus/spec.md`

## 1. Contexto e problema

Todo dado da plataforma é sensível e pertence a alguém. Sem identidade e perfil não existe
nem sigilo do dado do paciente nem separação entre quem agenda, quem atende e quem
prescreve. Esta é a fundação: nenhuma outra feature funciona sem ela.

## 2. Objetivo

Permitir que uma pessoa se identifique na plataforma e que cada operação seja autorizada
conforme os perfis atribuídos a ela.

## 3. Fora de escopo

- Recuperação de senha, verificação de e-mail e autenticação de dois fatores.
- Login social ou integração com gov.br.
- Expiração e histórico de senha.
- Bloqueio por tentativas (será tratado como evolução).

## 4. Atores e permissões

| Ator | O que pode fazer |
|---|---|
| ADMINISTRADOR | Criar, listar, editar, inativar usuários; atribuir e remover perfis |
| ATENDENTE | Autenticar-se; ver e alterar os próprios dados |
| MEDICO | Autenticar-se; ver e alterar os próprios dados |
| PACIENTE | Autenticar-se; ver e alterar os próprios dados |

## 5. Histórias de usuário

### HU-01 — Autenticação
**Como** usuário cadastrado **quero** me identificar **para que** eu acesse o que é meu.
```gherkin
Dado que existe um usuário ativo com e-mail e senha válidos
Quando ele se autentica com as credenciais corretas
Então recebe uma credencial de acesso com validade determinada
E essa credencial informa quais perfis ele possui

Dado que existe um usuário
Quando ele se autentica com senha incorreta
Então o acesso é negado sem revelar se o e-mail existe
```

### HU-02 — Cadastro de usuário pelo administrador
```gherkin
Dado que sou administrador autenticado
Quando cadastro um usuário com nome, CPF, e-mail, senha e ao menos um perfil
Então o usuário é criado ativo e passa a poder se autenticar

Quando informo um CPF ou e-mail já existente
Então o cadastro é rejeitado indicando conflito
```

### HU-03 — Atribuição de perfis
```gherkin
Dado que sou administrador
Quando atribuo o perfil MEDICO a um usuário
Então ele passa a poder executar operações clínicas
E a alteração vale para as próximas autenticações
```

### HU-04 — Inativação
```gherkin
Dado que um usuário está inativo
Quando tenta se autenticar
Então o acesso é negado
E seus registros históricos continuam preservados
```

### HU-05 — Dados próprios
```gherkin
Dado que sou um usuário autenticado de qualquer perfil
Quando consulto meus dados
Então vejo nome, CPF, e-mail e perfis, sem a senha
Quando tento consultar os dados de outro usuário sem ser administrador
Então o acesso é negado
```

## 6. Requisitos funcionais

| ID | Requisito | Prioridade |
|---|---|---|
| RF-01 | O sistema DEVE autenticar por e-mail e senha e devolver credencial com prazo de validade | Obrigatório |
| RF-02 | O sistema DEVE permitir ao administrador cadastrar, listar, editar e inativar usuários | Obrigatório |
| RF-03 | O sistema DEVE permitir associar um ou mais perfis a um usuário | Obrigatório |
| RF-04 | O sistema DEVE armazenar a senha de forma irreversível | Obrigatório |
| RF-05 | O sistema DEVE negar acesso a usuário inativo | Obrigatório |
| RF-06 | O sistema DEVE expor os dados do próprio usuário autenticado | Obrigatório |
| RF-07 | O sistema DEVE ter os quatro perfis pré-carregados na inicialização | Obrigatório |

## 7. Regras de negócio

| ID | Regra |
|---|---|
| RN-01 | CPF e e-mail são únicos entre usuários ativos e inativos |
| RN-02 | CPF deve ser válido (11 dígitos e dígitos verificadores corretos) |
| RN-03 | Senha tem no mínimo 8 caracteres com letra e número |
| RN-04 | Todo usuário tem pelo menos um perfil |
| RN-05 | Usuário não é excluído fisicamente; é inativado |
| RN-06 | A credencial de acesso expira; após expirar exige nova autenticação |
| RN-07 | Um administrador não pode remover o próprio perfil ADMINISTRADOR |

## 8. Fluxos de exceção

| ID | Situação | Comportamento |
|---|---|---|
| EX-01 | Credencial inválida | Negar sem distinguir e-mail inexistente de senha errada |
| EX-02 | Credencial expirada | Negar e sinalizar necessidade de nova autenticação |
| EX-03 | CPF/e-mail duplicado | Rejeitar com conflito, apontando o campo |
| EX-04 | Perfil inexistente na atribuição | Rejeitar indicando os perfis válidos |
| EX-05 | Usuário inativo autenticando | Negar acesso |

## 9. Conceitos de domínio

- **Usuário:** identidade de acesso. Nome, CPF, e-mail, senha, situação (ativo/inativo),
  data de criação, perfis.
- **Perfil (role):** rótulo de autorização. Valores fixos: ADMINISTRADOR, ATENDENTE,
  MEDICO, PACIENTE.
- **Credencial de acesso:** prova temporária de identidade, portadora dos perfis e do
  vínculo com paciente ou médico quando existir.

## 10. Eventos produzidos

Nenhum no MVP. (Auditoria de acesso é evolução futura.)

## 11. Requisitos não funcionais

- Senha nunca retorna em resposta nem aparece em log.
- CPF não aparece em log.
- Tempo de resposta da autenticação abaixo de 1 s.
- A credencial deve permitir ao sistema saber, sem consultar o banco, quais perfis o
  usuário tem.

## 12. Definition of Done

- [ ] RF-01 a RF-07 implementados e testados
- [ ] Teste: usuário inativo não autentica
- [ ] Teste: não-administrador não acessa dados de terceiro
- [ ] Teste: senha não aparece em nenhuma resposta
- [ ] Perfis pré-carregados por migration
- [ ] Contrato OpenAPI publicado

## 13. Pendências

- [NEEDS CLARIFICATION: qual o prazo de validade da credencial de acesso? Sugestão: 8 horas para a demo.]
- [NEEDS CLARIFICATION: haverá auto-cadastro de paciente (usuário criado pelo próprio cidadão), ou todo usuário nasce pelo administrador/atendente?]

## 14. Dependências

- Pré-requisito de: todas as demais features.
