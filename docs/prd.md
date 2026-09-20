# Foot Fanatics — PRD

## 1. Visão do produto
TODO

## 2. Problema
TODO

## 3. Personas

### 3.1 Rafael, torcedor casual (plano free)

- **Perfil:** 27 anos, usuário do plano free, acessa pelo celular.
- **Objetivo:** ver resultado e escalação de forma rápida, sem cadastro nem login.
- **Contexto:** acessa no ônibus ou no intervalo do trabalho, com internet às vezes instável. Entra poucas vezes na semana, quase sempre em dia de jogo, e não pretende pagar por conteúdo.
- **Frustração:** clicar numa matéria, esperar carregar e só então descobrir que ela é exclusiva para assinantes. Também se irrita quando exigem login só para ver um placar.
- **Gera cenário de:** fronteira entre conteúdo público e pago.

### 3.2 Marina, torcedora assinante (plano premium)

- **Perfil:** 34 anos, assinante do plano premium, acessa por notebook, celular e tablet.
- **Objetivo:** ler análises táticas e matérias exclusivas com continuidade, sem precisar autenticar de novo a cada acesso.
- **Contexto:** paga a assinatura todo mês e fica logada por longos períodos, às vezes em mais de um dispositivo ao mesmo tempo. Lê matérias longas com calma.
- **Frustração:** ser deslogada no meio da leitura ou ter a assinatura em dia e ainda assim ver o conteúdo bloqueado. Ser cobrada sem conseguir acessar o que pagou é o pior cenário para ela.
- **Gera cenário de:** sessão, expiração e vigência da assinatura.

### 3.3 Carlos, editor de conteúdo (operação)

- **Perfil:** 31 anos, integrante da redação, publica matérias pela API de conteúdo.
- **Objetivo:** publicar matérias rapidamente e marcar corretamente o que é exclusivo para assinantes.
- **Contexto:** trabalha sob pressão em dia de jogo e publica várias matérias por hora. Quase não tem tempo de conferir se cada publicação teve o efeito esperado.
- **Frustração:** marcar uma matéria como exclusiva e ela aparecer para o plano free (conteúdo pago vazando), ou o contrário. Também se preocupa com usuários comuns acessando funções de edição que deveriam ser só da operação.
- **Gera cenário de:** autorização e acesso indevido.

## 4. Escopos
- E1 Identidade & Conta: cadastro, credencial, perfil, recuperação de acesso
- E2 Sessão & Acesso: login, emissão de token, expiração, renovação, logout
- E3 Assinatura: planos free e premium, vigência, upgrade, cancelamento
- E4 Conteúdo: clubes, jogos e matérias; parte pública e parte exclusiva de assinante

## 5. Fora de escopo
- Pagamento real, mobile, push, painel administrativo

## 6. Métricas de sucesso
TODO

## 7. Requisitos

Os requisitos partem das três personas e cada um está ligado ao escopo que o originou (E1 a E4). Os candidatos foram levantados com apoio de IA; a turma decide o que entra. Coluna Origem: R (Rafael), M (Marina), C (Carlos).

### 7.1 Requisitos funcionais (RF)

| ID | Requisito | Origem | Escopo |
| --- | --- | --- | --- |
| RF-01 | O sistema deve permitir o cadastro de conta com e-mail e senha. | R, M | E1 |
| RF-02 | O sistema deve rejeitar cadastro com e-mail já existente ou com formato inválido. | R, M | E1 |
| RF-03 | O sistema deve permitir que o usuário consulte e edite os dados do próprio perfil. | M | E1 |
| RF-04 | O sistema deve distinguir os perfis de acesso torcedor e editor. | C | E1 |
| RF-05 | O sistema deve autenticar o usuário por e-mail e senha e emitir um token de sessão. | M | E2 |
| RF-06 | O sistema deve encerrar a sessão no logout e invalidar o token emitido. | M | E2 |
| RF-07 | O sistema deve rejeitar requisições com token expirado ou inválido e exigir nova autenticação. | M | E2 |
| RF-08 | O sistema deve permitir sessões simultâneas da mesma conta em mais de um dispositivo. | M | E2 |
| RF-09 | O sistema deve atribuir o plano free a toda conta nova. | R | E3 |
| RF-10 | O sistema deve permitir a contratação do plano premium por um usuário autenticado, sem integração de pagamento real. | M | E3 |
| RF-11 | O sistema deve manter o estado da assinatura (ativa ou expirada) de acordo com a data de vigência. | M | E3 |
| RF-12 | O sistema deve permitir o cancelamento da assinatura, mantendo o acesso até o fim do período já contratado. | M | E3 |
| RF-13 | O sistema deve permitir, sem login, a leitura de clubes, jogos, resultados e escalações. | R | E4 |
| RF-14 | O sistema deve negar o acesso a matéria exclusiva para visitante e plano free, indicando o plano premium. | R | E4 |
| RF-15 | O sistema deve liberar a matéria exclusiva completa ao assinante com assinatura ativa. | M | E4 |
| RF-16 | O sistema deve permitir que o editor publique uma matéria e a marque como pública ou exclusiva. | C | E4 |
| RF-17 | O sistema deve restringir a publicação e a edição de matérias ao perfil editor. | C | E4 |

### 7.2 Requisitos não funcionais (RNF)

Todos com métrica verificável em teste. p95 significa 95% das requisições abaixo do valor indicado.

| ID | Requisito | Métrica (como medir) | Origem | Escopo |
| --- | --- | --- | --- | --- |
| RNF-01 | Segurança das credenciais | 100% das senhas armazenadas com hash (bcrypt ou argon2); 0 senhas em texto puro no banco. | M | E1 |
| RNF-02 | Desempenho do login | p95 < 1 s com 50 usuários autenticando ao mesmo tempo. | M | E2 |
| RNF-03 | Duração da sessão | Sessão expira somente após 60 min sem requisições; com uso contínuo, 0 expirações em um teste de 2 h. | M | E2 |
| RNF-04 | Tratamento de token | 100% das requisições com token ausente, inválido ou expirado retornam HTTP 401; 0 tokens gravados em log. | M | E2 |
| RNF-05 | Vigência da assinatura | Acesso premium liberado em até 30 s após a contratação e bloqueado em até 60 s após o fim da vigência. | M | E3 |
| RNF-06 | Desempenho da leitura pública | p95 < 2 s para resultados e escalações, com 100 requisições simultâneas. | R | E4 |
| RNF-07 | Bloqueio rápido de conteúdo exclusivo | Resposta de bloqueio em p95 < 1 s e sem o corpo da matéria no retorno. | R | E4 |
| RNF-08 | Autorização de publicação | 100% das tentativas de publicar ou editar por perfil não editor retornam HTTP 403. | C | E4 |
| RNF-09 | Consistência da marcação exclusiva | Matéria marcada como exclusiva deixa de ser servida ao plano free em até 5 s; 0 vazamentos em 100 publicações de teste. | C | E4 |
| RNF-10 | Uso em rede móvel | Resposta da listagem de resultados com no máximo 100 KB. | R | E4 |
| RNF-11 | Disponibilidade | Disponibilidade mensal ≥ 99,5%, medida por health check a cada minuto. | R, M | E4 |
| RNF-12 | Rastreabilidade das publicações | 100% das publicações e mudanças de marcação registradas com usuário e data e hora. | C | E4 |

### 7.3 Pontos a validar com a turma

- **Valores das métricas:** 1 s, 2 s, 60 min, 30 s, 99,5% e 100 KB são propostas; a turma confirma ou ajusta cada um.
- **RF-12:** assume que o cancelamento mantém o acesso até o fim do período contratado; a regra de negócio precisa ser confirmada.
- **RF-08:** não define limite de dispositivos simultâneos; decidir se haverá um.
