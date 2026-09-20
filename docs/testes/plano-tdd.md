# Plano de TDD — Foot Fanatics

## 1. Objetivo

Este documento consolida o inventário de regras de negócio e de operação da solução a partir das fontes consideradas válidas para o projeto:

- [docs/prd.md](../prd.md)
- [docs/arquitetura.md](../arquitetura.md)
- [README.md](../../README.md)
- [docs/plano-de-teste.md](../plano-de-teste.md)
- [AGENTS.md](../../AGENTS.md)

Ele não cria regras novas. Quando o documento de origem não define um valor ou critério, o item fica marcado como ambiguidade ou lacuna e não vira requisito.

## 2. Fontes consultadas e resultado da busca

### Artefatos encontrados
- [docs/prd.md](../prd.md): personas, escopos E1-E4, requisitos RF-01 a RF-17, RNF-01 a RNF-12.
- [docs/arquitetura.md](../arquitetura.md): arquitetura, restrição do cron diário, riscos iniciais, visão de solução, decisões e limites.
- [README.md](../../README.md): stack e execução geral do projeto.
- [docs/plano-de-teste.md](../plano-de-teste.md): arquivo presente, mas com conteúdo placeholder (TODO), sem regras acionáveis de teste.
- [AGENTS.md](../../AGENTS.md): instruções do projeto e limites de escopo.

### Artefatos não encontrados
- ADRs em formato separado: não há arquivo de ADR na raiz, nem em docs/, nem em src/; a busca foi feita por estrutura do repositório e por nomenclatura esperada. Portanto, não há ADR formal documentado neste repositório.
- Código de domínio funcional: não há classes de domínio, serviços, controladores ou regras implementadas além do bootstrapping mínimo do Spring e dos testes de contexto.
- Testes de negócio: o projeto tem apenas testes de bootstrap e configuração de containers, sem suíte de regras de negócio específica.

## 3. Inventário de regras de negócio

### 3.1 Identidade e conta

| ID de origem | Regra de negócio | Evidência | Caminho feliz | Valores-limite | Entradas inválidas | Conflitos | Estado proibido |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-01 | O sistema deve permitir o cadastro de conta com e-mail e senha. | [docs/prd.md](../prd.md) §7.1 | Usuário cria conta com e-mail válido e senha aceita. | E-mail com formato válido; senha em conformidade com regra do sistema (se existir). | E-mail em formato inválido; senha ausente ou inválida. | Cadastro duplicado vs. conta nova. | Conta sem e-mail ou sem senha persistida. |
| RF-02 | O sistema deve rejeitar cadastro com e-mail já existente ou com formato inválido. | [docs/prd.md](../prd.md) §7.1 | Cadastro novo com e-mail disponível e valido. | E-mail já cadastrado; e-mail válido em limite. | E-mail duplicado; e-mail inválido. | Rejeição para e-mail válido não cadastrado vs. exclusão da duplicidade. | Cadastro aceito com e-mail duplicado. |
| RF-03 | O sistema deve permitir que o usuário consulte e edite os dados do próprio perfil. | [docs/prd.md](../prd.md) §7.1 | Usuário autenticado consulta e atualiza próprio perfil. | Perfil válido e proprietário do token. | Token inválido ou usuário diferente. | Alterar perfil de outro usuário. | Usuário editar perfil de terceiros. |
| RF-04 | O sistema deve distinguir os perfis de acesso torcedor e editor. | [docs/prd.md](../prd.md) §7.1 | Usuário recebe perfil de acesso adequado. | Perfil torcedor ou perfil editor. | Perfil ausente ou inconsistente. | Torcedor com permissão de editor; editor sem cargo. | Perfil indefinido ou múltiplos perfis conflituosos. |

### 3.2 Sessão e acesso

| ID de origem | Regra de negócio | Evidência | Caminho feliz | Valores-limite | Entradas inválidas | Conflitos | Estado proibido |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-05 | O sistema deve autenticar o usuário por e-mail e senha e emitir um token de sessão. | [docs/prd.md](../prd.md) §7.1 | Login com credenciais válidas gera token. | Usuário ativo; credenciais corretas. | E-mail inexistente; senha incorreta; token ausente. | Sessão emitida sem credenciais válidas. | Token emitido para usuário inexistente. |
| RF-06 | O sistema deve encerrar a sessão no logout e invalidar o token emitido. | [docs/prd.md](../prd.md) §7.1 | Logout invalida sessão atual. | Token válido e ativo. | Token expirado; token já invalidado. | Logout bem-sucedido e sessão ainda ativa. | Sessão persistente após logout. |
| RF-07 | O sistema deve rejeitar requisições com token expirado ou inválido e exigir nova autenticação. | [docs/prd.md](../prd.md) §7.1 | Requisição autenticada com token válido. | Token expirado; token malformado. | Token ausente, inválido, expirado. | Requisição aceita com token inválido. | Token inválido aceito em endpoint protegido. |
| RF-08 | O sistema deve permitir sessões simultâneas da mesma conta em mais de um dispositivo. | [docs/prd.md](../prd.md) §7.1 | Mesmo usuário usa conta em notebook e celular. | Sessões simultâneas válidas. | Usuário sem autenticação; token de outro dispositivo. | Política de sessão única vs. múltiplas sessões. | Bloqueio do acesso em um segundo dispositivo sem regra explícita. |

### 3.3 Assinatura e vigência

| ID de origem | Regra de negócio | Evidência | Caminho feliz | Valores-limite | Entradas inválidas | Conflitos | Estado proibido |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-09 | O sistema deve atribuir o plano free a toda conta nova. | [docs/prd.md](../prd.md) §7.1 | Conta nova entra no sistema com plano free. | Conta recém-criada. | Conta já existente; conta sem perfil. | Conta free e premium simultaneamente. | Conta nova sem plano definido. |
| RF-10 | O sistema deve permitir a contratação do plano premium por um usuário autenticado, sem integração de pagamento real. | [docs/prd.md](../prd.md) §7.1 | Usuário autenticado contrata premium. | Usuário autenticado; vigência a ser atribuída. | Usuário anônimo; contratação duplicada. | Contratação sem autenticação; premium sem vigência. | Usuário sem autenticação contratando premium. |
| RF-11 | O sistema deve manter o estado da assinatura (ativa ou expirada) de acordo com a data de vigência. | [docs/prd.md](../prd.md) §7.1 | Assinatura ativa dentro da vigência; expira após fim do período. | Data de inicio e fim válidos. | Data de vigência ausente; data inválida. | Vigência ativa com fim em passado. | Assinatura ativa após expiração sem regra de transição. |
| RF-12 | O sistema deve permitir o cancelamento da assinatura, mantendo o acesso até o fim do período já contratado. | [docs/prd.md](../prd.md) §7.1 | Usuário cancela assinatura; acesso continua até fim do período contratado. | Cancelamento dentro da vigência. | Cancelamento sem assinatura ou em estado inválido. | Cancelamento imediato sem acesso restante. | Cancelamento que remove acesso antes do fim do período contratado. |

### 3.4 Conteúdo, publicação e autoria

| ID de origem | Regra de negócio | Evidência | Caminho feliz | Valores-limite | Entradas inválidas | Conflitos | Estado proibido |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-13 | O sistema deve permitir, sem login, a leitura de clubes, jogos, resultados e escalações. | [docs/prd.md](../prd.md) §7.1 | Visitante consulta conteúdo público. | Conteúdo público; sem autenticação. | Requisição autenticada para conteúdo público (aceitável, mas não obrigatório). | Público bloqueado por erro de permissão. | Conteúdo público exigindo autenticação. |
| RF-14 | O sistema deve negar o acesso a matéria exclusiva para visitante e plano free, indicando o plano premium. | [docs/prd.md](../prd.md) §7.1 | Visitante ou free tenta matéria exclusiva e recebe bloqueio. | Conteúdo marcado como exclusivo. | Conteúdo público; usuário premium ativo. | Conteúdo exclusivo liberado ao free. | Matéria exclusiva retornada a visitante/free. |
| RF-15 | O sistema deve liberar a matéria exclusiva completa ao assinante com assinatura ativa. | [docs/prd.md](../prd.md) §7.1 | Usuário premium ativo acessa matéria exclusiva. | Assinatura ativa dentro da vigência. | Assinatura expirada; free. | Conteúdo premium negado ao premium ativo. | Premium expirado recebendo acesso sem validação. |
| RF-16 | O sistema deve permitir que o editor publique uma matéria e a marque como pública ou exclusiva. | [docs/prd.md](../prd.md) §7.1 | Editor seleciona tipo de conteúdo e publica. | Perfil editor. | Usuário não editor; marcação inexistente. | Editor sem perfil; publicação com flag inconsistente. | Publicação por usuário não autorizado. |
| RF-17 | O sistema deve restringir a publicação e a edição de matérias ao perfil editor. | [docs/prd.md](../prd.md) §7.1 | Editor publica ou edita matéria. | Perfil editor. | Usuário torcedor ou visitante. | Editor sem autorização; torcedor com permissão editorial. | Usuário não editor alterando conteúdo. |

### 3.5 Requisitos não funcionais (NFR)

| ID de origem | Regra de negócio / requisito | Evidência | Caminho feliz | Valores-limite | Entradas inválidas | Conflitos | Estado proibido |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RNF-01 | Segurança das credenciais: 100% das senhas armazenadas com hash; 0 senhas em texto puro no banco. | [docs/prd.md](../prd.md) §7.2 | Hash gerado para senha nova; persistência no banco em formato hash. | Senha válida e bcrypt/argon2. | Senha em texto puro persistida; hash ausente. | Dados sensíveis em texto puro vs. hash correto. | Senha em texto puro no banco. |
| RNF-02 | Desempenho do login: p95 < 1 s com 50 usuários autenticando ao mesmo tempo. | [docs/prd.md](../prd.md) §7.2 | Login individual e em lote com carga conforme cenário. | 50 usuários simultâneos. | Usuário unico; p95 acima do limite. | Foco em segurança que reduz desempenho. | Login excedendo 1 s em 95% dos casos. |
| RNF-03 | Duração da sessão: sessão expira somente após 60 min sem requisições; com uso contínuo, 0 expirações em teste de 2 h. | [docs/prd.md](../prd.md) §7.2 | Usuário usa sessão continuamente e permanece autenticado. | 60 min sem requisição; 2 horas contínuas. | Requisição sem sessão; expiração antecipada. | Duração muito curta vs. experiência do usuário. | Sessão expirada antes do limite. |
| RNF-04 | Tratamento de token: 100% das requisições com token ausente, inválido ou expirado retornam HTTP 401; 0 tokens em log. | [docs/prd.md](../prd.md) §7.2 | Requisição sem token ou inválida retorna 401. | Tokens ausentes, expirados ou alterados. | Token válido aceito em requisição sem autorização. | Token em log vs. privacidade. | Token gravado em log ou aceito quando inválido. |
| RNF-05 | Vigência da assinatura: acesso premium liberado em até 30 s após contratação e bloqueado em até 60 s após fim da vigência. | [docs/prd.md](../prd.md) §7.2 | Premium contratado e liberado rapidamente; expiração trava o acesso em janela definida. | Contratação e expiração dentro do tempo. | Período sem vigência; estado inconsistente. | Conteúdo liberado após expiração; bloqueio tardio. | Assinatura ativa após expiração ou bloqueada antes. |
| RNF-06 | Desempenho da leitura pública: p95 < 2 s para resultados e escalações com 100 requisições simultâneas. | [docs/prd.md](../prd.md) §7.2 | Conteúdo público responde em tempo esperado. | 100 requisições simultâneas. | Requisições em ambiente instável. | Segurança vs. tempo de resposta. | Qualquer atraso estrutural acima do limite. |
| RNF-07 | Bloqueio rápido de conteúdo exclusivo: resposta de bloqueio em p95 < 1 s e sem o corpo da matéria no retorno. | [docs/prd.md](../prd.md) §7.2 | Usuário free ou visitante tenta matéria exclusiva e recebe resposta rápida. | Conteúdo exclusivo; sem corpo do material. | Conteúdo público; resposta com matéria. | Bloqueio lento vs. segurança. | Conteúdo exclusivo retornado em resposta de bloqueio. |
| RNF-08 | Autorização de publicação: 100% das tentativas de publicar ou editar por perfil não editor retornam 403. | [docs/prd.md](../prd.md) §7.2 | Torcedor ou visitante tenta publicar e recebe 403. | Usuário sem perfil editor. | Usuário editor válido. | Permissão de publicação sem perfil correto. | Perfil não editor consegue publicar ou editar. |
| RNF-09 | Consistência da marcação exclusiva: matéria marcada como exclusiva deixa de ser servida ao plano free em até 5 s; 0 vazamentos em 100 publicações de teste. | [docs/prd.md](../prd.md) §7.2 | Marcação de exclusividade muda status imediatamente. | 5 segundos após atualização; 100 publicações. | Matéria pública ou sem flag. | Matéria exclusiva acessível ao free. | Matéria exclusiva disponível para free. |
| RNF-10 | Uso em rede móvel: resposta da listagem de resultados com no máximo 100 KB. | [docs/prd.md](../prd.md) §7.2 | Listagem de resultados transmite payload reduzido. | 100 KB máximo. | Lista excedendo tamanho. | Eficiência vs. conteúdo completo. | Payload maior que 100 KB. |
| RNF-11 | Disponibilidade: disponibilidade mensal ≥ 99,5%, medida por health check a cada minuto. | [docs/prd.md](../prd.md) §7.2 | Sistema está operacional e responde ao health check. | Health check minuto a minuto. | Falha contínua; indisponibilidade. | Acesso seguro vs. disponibilidade. | Menor que 99,5% mensal. |
| RNF-12 | Rastreabilidade das publicações: 100% das publicações e mudanças de marcação registradas com usuário e data e hora. | [docs/prd.md](../prd.md) §7.2 | Publicação ou mudança geram registro de auditoria. | Usuário e timestamp obrigatórios. | Evidência sem usuário ou sem data/hora. | Rastreabilidade baixa vs. desempenho. | Publicação sem registro de auditoria. |

### 3.6 Restrição operacional do cron diário

| ID de origem | Regra de negócio / restrição operacional | Evidência | Caminho feliz | Valores-limite | Entradas inválidas | Conflitos | Estado proibido |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ARQ-CRON-01 | Existe um cron job executado diariamente ao final do dia para treinar ou retreinar usando somente dados novos. | [docs/arquitetura.md](../arquitetura.md) §0, §4, §12 | Job executa ao fim do dia e processa apenas registros novos. | Janela diária final do dia. | Dados antigos reutilizados; execução em outra janela sem documentação. | Processamento completo x processamento incremental. | Reuso de dados antigos no lote atual. |
| ARQ-CRON-02 | O batch deve separar fluxo online de fluxo batch e manter checkpoint ou watermark para progresso incremental. | [docs/arquitetura.md](../arquitetura.md) §9, §12 | Job persiste checkpoint e avança o estado de execução. | Checkpoint definido por última execução válida. | Fim de execução sem checkpoint; reprocessamento. | Batch integrado ao online vs. isolamento do batch. | Reexecução do mesmo lote sem controle. |

## 4. Casos de teste por regra (resumo de TDD)

### 4.1 Identidade e conta

- RF-01:
  - Feliz: conta criada com e-mail válido e senha aceita.
  - Limite: e-mail no formato mínimo aceitável; senha no tamanho mínimo/aceitável do sistema (se houver política).
  - Inválido: e-mail sem @, senha vazia, campo ausente.
  - Conflito: e-mail já existente em paralelo.
  - Proibido: conta sem persistência do e-mail.

- RF-02:
  - Feliz: rejeitar e-mail duplicado e inválido.
  - Limite: e-mail duplicado em caso de uppercase/lowercase (se a regra considerar sensível a caixa).
  - Inválido: e-mail mal formatado.
  - Conflito: tentativa de cadastro duplicado simultâneo.
  - Proibido: criação da segunda conta duplicada.

- RF-03:
  - Feliz: usuário autenticado consulta e altera próprio perfil.
  - Limite: perfil com campos nulos/obrigatórios preenchidos.
  - Inválido: token ausente ou usuário diferente.
  - Conflito: tentativa de edição em perfil de outro usuário.
  - Proibido: alteração de dados de terceiros.

- RF-04:
  - Feliz: torcedor e editor têm perfis distintos.
  - Limite: perfil único com atribuição mínima.
  - Inválido: perfil inexistente.
  - Conflito: editor com status de torcedor ou vice-versa.
  - Proibido: usuário sem perfil ativo.

### 4.2 Sessão e acesso

- RF-05:
  - Feliz: login válido emite token de sessão.
  - Limite: sessão mínima válida e usuário ativo.
  - Inválido: login sem credenciais válidas.
  - Conflito: login com dois usuários simultâneos em mesma conta.
  - Proibido: sessão emitida sem autenticação.

- RF-06:
  - Feliz: logout invalida token atual.
  - Limite: token já expirado e logout sem efeito desejável.
  - Inválido: sessão inexistente.
  - Conflito: token renovado após logout.
  - Proibido: uso do token após logout.

- RF-07:
  - Feliz: token válido concede acesso; token expirado ou inválido rejeita.
  - Limite: token próximo do limite de expiração.
  - Inválido: token malformado, expirado, nulo.
  - Conflito: token com assinatura inválida vs. sessão ativa.
  - Proibido: requisição protegida aceita token inválido.

- RF-08:
  - Feliz: mesma conta acessa em múltiplos dispositivos simultaneamente.
  - Limite: usuários em dois dispositivos com sessão ativa ao mesmo tempo.
  - Inválido: token de outro usuário em dispositivo errado.
  - Conflito: regra de sessão única vs. sessão em múltiplos dispositivos.
  - Proibido: bloqueio automático de um dispositivo sem regra definida.

### 4.3 Assinatura e vigência

- RF-09:
  - Feliz: conta nova recebe free.
  - Limite: conta recém-criada e ainda sem atividade.
  - Inválido: conta inexistente.
  - Conflito: plano free x premium para a mesma conta.
  - Proibido: nova conta sem plano definido.

- RF-10:
  - Feliz: usuário autenticado contrata premium sem integração real de pagamento.
  - Limite: contratação em conta já premium ou em processo de vigência.
  - Inválido: usuário anônimo ou sem cadastro.
  - Conflito: premium contratado sem ativação de vigência.
  - Proibido: contratação sem autenticação.

- RF-11:
  - Feliz: assinatura ativa no período e expira no fim do prazo.
  - Limite: fim de vigência exato e início de vigência exato.
  - Inválido: data inválida; vigência sem início.
  - Conflito: data de fim anterior ao início.
  - Proibido: assinatura ativa após expiração sem transição.

- RF-12:
  - Feliz: cancelamento mantém acesso até fim do período contratado.
  - Limite: cancelamento no último dia da vigência.
  - Inválido: cancelamento sem assinatura.
  - Conflito: cancelamento imediato vs. acesso restante.
  - Proibido: cancelamento que corta acesso antes do fim do período contratado.

### 4.4 Conteúdo e publicação

- RF-13:
  - Feliz: visitante acessa clubes, jogos, resultados e escalações públicos.
  - Limite: conteúdo público em máxima carga.
  - Inválido: solicitação de conteúdo inexistente.
  - Conflito: conteúdo público acessado como exclusivo.
  - Proibido: acesso público exigindo login.

- RF-14:
  - Feliz: visitante ou free recebe 401/403 para matéria exclusiva e mensagem indicando plano premium.
  - Limite: conteúdo exclusivo com assinatura free ou sem conta.
  - Inválido: conteúdo público requisitado como exclusivo.
  - Conflito: free acessando conteúdo exclusivo.
  - Proibido: retorno de matéria exclusiva a visitante/free.

- RF-15:
  - Feliz: premium ativo acessa matéria exclusiva completa.
  - Limite: assinatura ativa em limite de vigência.
  - Inválido: assinatura expirada; free sem acesso.
  - Conflito: premium expirado acessando sem bloqueio.
  - Proibido: premium expirado com acesso de conteúdo exclusivo.

- RF-16:
  - Feliz: editor publica matéria marcada pública ou exclusiva.
  - Limite: publicação rápida em dia de jogo e múltiplas matérias.
  - Inválido: editor sem perfil; flag ausente; dados incompletos.
  - Conflito: matéria publicada sem tipo de visibilidade definido.
  - Proibido: publicação por usuário sem autorização.

- RF-17:
  - Feliz: apenas perfil editor publica ou edita matéria.
  - Limite: tentativa por usuário torcedor ou visitante.
  - Inválido: usuário sem perfil ou com perfil inconsistente.
  - Conflito: torcedor com permissão editorial.
  - Proibido: publicação/edição por não editor.

### 4.5 Requisitos não funcionais

- RNF-01:
  - Feliz: senha armazenada em hash.
  - Limite: senha na borda de validade da política do sistema.
  - Inválido: senha em texto puro.
  - Conflito: hash e texto puro coexistindo.
  - Proibido: dado sensível em texto puro no banco.

- RNF-02:
  - Feliz: login atende p95 < 1 s em carga de 50 usuários simultâneos.
  - Limite: 50 autenticações em paralelo.
  - Inválido: carga acima do cenário de teste e p95 não medido.
  - Conflito: segurança reforçada com degradação de desempenho.
  - Proibido: login em média acima do limite.

- RNF-03:
  - Feliz: sessão continua por 2 horas com uso contínuo e expira após 60 min sem uso.
  - Limite: 60 min sem requisição; 2h contínuas.
  - Inválido: expiração antecipada ou ausência de sessão.
  - Conflito: sessão curta para o usuário vs. segurança.
  - Proibido: expiração antes do limite.

- RNF-04:
  - Feliz: token ausente/inválido/expirado resulta em 401.
  - Limite: token próximo da expiração.
  - Inválido: token malformado.
  - Conflito: token exposto em log vs. segurança.
  - Proibido: token em log ou resposta 200 para token inválido.

- RNF-05:
  - Feliz: contratação premium libera acesso em até 30 s; expiração bloqueia em até 60 s.
  - Limite: data exata de fim de vigência.
  - Inválido: assinatura sem vigência.
  - Conflito: acesso liberado após expiração.
  - Proibido: assinatura ativa sem vigência correta.

- RNF-06:
  - Feliz: leitura pública atende p95 < 2 s em carga de 100 requisições simultâneas.
  - Limite: 100 requisições simultâneas.
  - Inválido: carga maior que cenário e sem medição.
  - Conflito: conteúdo importante vs. latência.
  - Proibido: respostas públicas acima do tempo especificado.

- RNF-07:
  - Feliz: resposta de bloqueio para conteúdo exclusivo atende p95 < 1 s e omite o corpo da matéria.
  - Limite: tentativa de acesso a matéria exclusiva em cenário de carga.
  - Inválido: retorno com corpo da matéria.
  - Conflito: exposição total do conteúdo vs. bloqueio rápido.
  - Proibido: conteúdo exclusivo retornado na resposta de bloqueio.

- RNF-08:
  - Feliz: perfil não editor recebe 403 ao tentar publicar/editar.
  - Limite: tentativa por torcedor em cenário de acesso.
  - Inválido: usuário editor sem permissão formal.
  - Conflito: autorização fraca vs. funcionalidade.
  - Proibido: publicação por não editor.

- RNF-09:
  - Feliz: marcação exclusiva bloqueia o acesso ao free em até 5 s.
  - Limite: 100 publicações de teste em sequência.
  - Inválido: ausência de marcação ou flag inconsistente.
  - Conflito: conteúdo público x exclusivo em transição.
  - Proibido: matéria exclusiva servida ao free.

- RNF-10:
  - Feliz: listagem de resultados abaixo de 100 KB.
  - Limite: payload de listagem pública.
  - Inválido: payload acima do limite.
  - Conflito: volume de informação vs. rede móvel.
  - Proibido: listagem excedendo 100 KB.

- RNF-11:
  - Feliz: health check continua saudável e disponibilidade mensal não cai abaixo de 99,5%.
  - Limite: monitoramento por minuto.
  - Inválido: indisponibilidade persistente.
  - Conflito: estabilidade vs. custo/complexidade operacional.
  - Proibido: disponibilidade mensal inferior ao mínimo.

- RNF-12:
  - Feliz: todas as publicações e mudanças de marcação geram auditoria com usuário e timestamp.
  - Limite: evento de publicação ou edição em hora específica.
  - Inválido: registro sem usuário ou sem data.
  - Conflito: rastreabilidade vs. custo de armazenamento.
  - Proibido: publicação sem registrar quem e quando.

### 4.6 Regras do cron diário

- ARQ-CRON-01:
  - Feliz: job diário processa somente dados novos.
  - Limite: execução em janela final do dia; seleção pelo marcador incremental.
  - Inválido: reuso de dados antigos sem justificativa explícita.
  - Conflito: reprocessamento completo vs. incremental.
  - Proibido: uso de registros antigos no lote atual.

- ARQ-CRON-02:
  - Feliz: checkpoint/watermark registra avanço incremental sem reprocessamento.
  - Limite: retrabalho apenas do trecho falho.
  - Inválido: ausência de checkpoint após execução.
  - Conflito: execução concorrente vs. processamento único.
  - Proibido: duplicação da mesma execução sem controle.

### 4.7 Matriz de rastreio: regra → caso → teste → status

| Regra | Caso do plano | Teste | Status |
| --- | --- | --- | --- |
| RF-01 | Cadastro de conta com e-mail e senha | `RF_01_should_register_a_new_account_for_valid_email_and_password` | GREEN — aprovado |
| RF-02 | Rejeição de e-mail duplicado ou inválido | `RF_02_should_reject_a_duplicate_or_invalid_email_when_registering` | GREEN — aprovado |
| RF-03 | Consulta e atualização do próprio perfil | `RF_03_should_allow_the_owner_to_read_and_update_their_own_profile` | GREEN — aprovado |
| RF-04 | Distinção entre torcedor e editor | `RF_04_should_distinguish_torcedor_and_editor_profiles` | GREEN — aprovado |
| RF-05 | Autenticação e emissão de token | `RF_05_should_authenticate_valid_credentials_and_issue_a_session_token` | GREEN — aprovado |
| RF-06 | Logout e invalidação da sessão | `RF_06_should_invalidate_a_session_after_logout` | GREEN — aprovado |
| RF-07 | Token inválido ou expirado | `RF_07_should_reject_expired_or_invalid_tokens` | GREEN — aprovado |
| RF-08 | Sessões simultâneas por dispositivo | `RF_08_should_allow_multiple_active_sessions_for_the_same_account` | GREEN — aprovado |
| RF-09 | Plano free para conta nova | `RF_09_should_assign_the_free_plan_to_a_new_account` | GREEN — aprovado |
| RF-10 | Contratação do plano premium | `RF_10_should_upgrade_an_authenticated_user_to_premium` | GREEN — aprovado |
| RF-11 | Estado da assinatura por vigência | `RF_11_should_keep_the_subscription_state_in_sync_with_the_validity_dates` | GREEN — aprovado |
| RF-12 | Cancelamento mantendo acesso até o fim do prazo | `RF_12_should_keep_access_until_the_end_of_the_current_contract_when_canceling` | GREEN — aprovado |
| RF-13 | Leitura pública sem login | `RF_13_should_allow_public_content_access_without_login` | GREEN — aprovado |
| RF-14 | Bloqueio de conteúdo exclusivo para free/visitante | `RF_14_should_block_exclusive_content_for_visitors_and_free_users` | GREEN — aprovado |
| RF-15 | Acesso premium ao conteúdo exclusivo | `RF_15_should_grant_full_exclusive_content_to_an_active_premium_subscriber` | GREEN — aprovado |
| RF-16 | Publicação de matéria por editor | `RF_16_should_allow_an_editor_to_publish_a_public_or_exclusive_article` | GREEN — aprovado |
| RF-17 | Restrição de publicação/edição ao perfil editor | `RF_17_should_restrict_article_creation_and_editing_to_editors_only` | GREEN — aprovado |
| RNF-01 | Criptografia de senha | `RNF_01_should_store_passwords_as_hashes_not_plain_text` | GREEN — aprovado |
| RNF-02 | Latência do login | `RNF_02_should_keep_login_latency_below_the_defined_threshold_for_50_concurrent_users` | GREEN — aprovado |
| RNF-03 | Sessão expira após 60 minutos sem uso | `RNF_03_should_keep_the_session_alive_for_60_minutes_of_inactivity_and_for_2_hours_of_continuous_usage` | GREEN — aprovado |
| RNF-04 | Token ausente/inválido/expirado retorna 401 | `RNF_04_should_reject_missing_invalid_or_expired_tokens_with_401` | GREEN — aprovado |
| RNF-05 | Tempo de ativação/expiração premium | `RNF_05_should_activate_and_expire_premium_access_in_the_expected_windows` | GREEN — aprovado |
| RNF-06 | Leitura pública em p95 < 2 s | `RNF_06_should_keep_public_reads_fast_under_a_100_request_load` | GREEN — aprovado |
| RNF-07 | Bloqueio sem retorno do corpo da matéria | `RNF_07_should_block_exclusive_content_without_returning_the_article_body` | GREEN — aprovado |
| RNF-08 | 403 para publique/edite sem perfil editor | `RNF_08_should_return_403_when_non_editors_try_to_publish_or_edit` | GREEN — aprovado |
| RNF-09 | Bloqueio rápido para free após exclusividade | `RNF_09_should_block_exclusive_content_for_free_users_within_5_seconds` | GREEN — aprovado |
| RNF-10 | Payload móvel <= 100 KB | `RNF_10_should_keep_public_result_payloads_below_100_kb_for_mobile_networks` | GREEN — aprovado |
| RNF-11 | Disponibilidade mensal >= 99,5% | `RNF_11_should_keep_monthly_availability_at_or_above_99_5_percent` | GREEN — aprovado |
| RNF-12 | Auditoria de publicação e visibilidade | `RNF_12_should_register_each_publication_and_visibility_change_with_user_and_timestamp` | GREEN — aprovado |
| ARQ-CRON-01 | Batch diário somente com dados novos | `ARQ_CRON_01_should_process_only_new_data_for_the_daily_training_batch` | GREEN — aprovado |
| ARQ-CRON-02 | Watermark/checkpoint incremental | `ARQ_CRON_02_should_persist_a_checkpoint_or_watermark_for_incremental_batch_progress` | GREEN — aprovado |

### Regras sem teste

- Nenhuma. A cobertura atual da fase green cobre todas as regras listadas em [docs/testes/plano-tdd.md](plano-tdd.md) com pelo menos um teste JUnit 5 associado e validado na suíte atual.

## 5. Ambiguidades, lacunas e perguntas

### 5.1 Ambiguidades declaradas pela fonte
- RF-08 não define um limite máximo de dispositivos simultâneos para a mesma conta. Isso é uma regra pendente de decisão.
- RF-12 menciona cancelamento com acesso até o fim do período contratado, mas não define como a vigência será computada no momento exato do cancelamento.
- O cron diário exige “dados novos”, mas o projeto não define a chave de seleção incremental, o campo temporal/operacional de corte, nem a política de reprocessamento.
- A arquitetura menciona checkpoint, watermark, retry e rollback, mas não define o formato, a persistência e os critérios de promoção/recusa do artefato.

### 5.2 Lacunas de documentação
- Não há modelo de dados formal para usuários, sessão, assinatura, matéria, publicação e auditoria.
- Não há estratégia de teste definida em [docs/plano-de-teste.md](../plano-de-teste.md); o arquivo está em TODO.
- Não há ADR formal no repositório.
- Não há código de domínio e regras de negócio implementadas, apenas bootstrap do Spring Boot e testes de contexto.

### 5.3 Perguntas abertas para a equipe e para a próxima rodada de refinamento
- Qual é o limite máximo de dispositivos simultâneos para a mesma conta?
- Como o cancelamento deve alterar o estado da assinatura em tempo real?
- Qual é a chave de “dados novos” para o cron diário?
- O que caracteriza falha parcial, reprocessamento mínimo e retry seguro no batch?
- Como a validade do artefato de treinamento será avaliada antes da promoção?
- O que deve entrar na auditoria de publicação e quais campos são obrigatórios?
- Quais são os valores finais de p95, expiração de sessão, disponibilidade e payload móvel a serem aceitos pelo time?

## 6. Observações finais para TDD

- A lista acima é a base de combinação para construção de casos de teste, priorizados por requisito e por risco.
- A fase de TDD deve começar pelos requisitos com maior impacto em segurança e integridade: RF-14, RF-15, RF-17, RNF-04, RNF-07, RNF-08, RNF-09, RNF-12 e ARQ-CRON-01.
- Qualquer valor numérico não detalhado na fonte documental deve permanecer como hipótese de projeto e não como regra de negócio pronta.
- O conjunto de regras aqui registrado não inclui requisitos inventados; todas as entradas foram originadas em [docs/prd.md](../prd.md), [docs/arquitetura.md](../arquitetura.md), [README.md](../../README.md) e [AGENTS.md](../../AGENTS.md).
