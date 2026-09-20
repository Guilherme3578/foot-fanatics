# Foot Fanatics

API REST de conteúdo esportivo (clubes, jogos, resultados e matérias) com assinatura free e premium. Projeto-caso da disciplina de Qualidade de Software (TADS, Senac).

Autor: Guilherme Israel dos Santos

## Stack

Java 21 · Spring Boot 3.x · Maven · PostgreSQL

## Pré-requisitos

- Java 21 (`java -version`)
- Node.js (`node --version`), usado pelo AIOX
- Git

## Como subir o projeto

```bash
./mvnw spring-boot:run
```

## Como rodar os testes

```bash
./mvnw test
```

## Documentação

- [docs/prd.md](docs/prd.md): visão, personas, escopos e requisitos
- [docs/arquitetura.md](docs/arquitetura.md): stack, camadas, modelo de dados e decisões
- [docs/plano-de-teste.md](docs/plano-de-teste.md): estratégia, níveis, critérios e metas de cobertura
