# Coupon API

API REST para cadastro, consulta e exclusao logica de cupons, desenvolvida como desafio tecnico Java/Spring.

## Tecnologias

- Java 25
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Flyway
- MapStruct
- Lombok
- Bean Validation
- Springdoc OpenAPI/Swagger
- JUnit 5, Mockito
- JaCoCo
- Docker e Docker Compose

## Decisoes Tecnicas

### Java 25

O projeto usa Java 25 por ser uma versao LTS recente da plataforma Java. A escolha busca manter a aplicacao alinhada com uma versao atual, com suporte de longo prazo, sem abrir mao de estabilidade para um backend Spring.

### PostgreSQL em vez de H2

Embora o desafio cite H2 como uma alternativa para ambiente em memoria, este projeto usa PostgreSQL para aproximar o ambiente local do comportamento esperado em producao. Essa escolha permite validar melhor migracoes Flyway, constraints, indices, tipos como `UUID` e `TIMESTAMP WITH TIME ZONE`, alem do indice unico parcial usado para permitir reutilizacao de codigo apos soft delete.

### Regras no dominio

As regras de negocio principais ficam encapsuladas em `Coupon`, evitando concentrar comportamento no controller ou no service. O dominio e responsavel por:

- sanitizar o codigo do cupom;
- validar tamanho do codigo;
- validar descricao;
- validar valor de desconto;
- impedir data de expiracao no passado;
- definir status inicial;
- executar exclusao logica;
- impedir exclusao de cupom ja deletado.

### Soft Delete

A exclusao nao remove registros fisicamente do banco. O cupom e marcado com `deleted = true`, `status = DELETED`, `published = false` e `deleted_at` preenchido. A entidade JPA usa uma restricao Hibernate para filtrar automaticamente registros deletados nas consultas, e os fluxos da aplicacao tambem usam metodos explicitos do repository, como `findByIdAndDeletedFalse`.

## Como Executar com Docker

Suba a aplicacao e o banco PostgreSQL:

```bash
docker compose up --build
```

A API ficara disponivel em:

```text
http://localhost:8085
```

O PostgreSQL ficara exposto localmente em:

```text
localhost:5434
```

Credenciais usadas pelo `docker-compose.yml`:

```text
database: coupon_db
username: coupon_user
password: coupon_password
```

## Como Executar Localmente

Com um PostgreSQL disponivel em `localhost:5434`, execute:

```bash
mvn spring-boot:run
```

Tambem e possivel sobrescrever as configuracoes por variaveis de ambiente:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/coupon_db
SPRING_DATASOURCE_USERNAME=coupon_user
SPRING_DATASOURCE_PASSWORD=coupon_password
SERVER_PORT=8080
```

## Swagger

A documentacao interativa da API esta disponivel em:

```text
http://localhost:8085/swagger-ui.html
```

Se executar fora do Docker usando a porta padrao da aplicacao:

```text
http://localhost:8080/swagger-ui.html
```

## Endpoints

### Criar cupom

```http
POST /coupon
Content-Type: application/json
```

Exemplo de request:

```json
{
  "code": "ABC-123",
  "description": "Black Friday coupon",
  "discountValue": 10.50,
  "expirationDate": "2030-11-04T17:14:45.180Z",
  "published": true
}
```

Resposta esperada:

```http
201 Created
Location: /coupon/{id}
```

### Buscar cupom por id

```http
GET /coupon/{id}
```

Resposta esperada:

```http
200 OK
```

### Deletar cupom

```http
DELETE /coupon/{id}
```

Resposta esperada:

```http
204 No Content
```

## Regras de Negocio

- O codigo e obrigatorio.
- O codigo deve possuir exatamente 6 caracteres alfanumericos apos sanitizacao.
- Caracteres especiais sao aceitos na entrada, removidos antes da persistencia e do retorno.
- A descricao e obrigatoria e deve possuir no maximo 255 caracteres.
- O valor de desconto e obrigatorio.
- O valor de desconto deve ser maior ou igual a `0.50`.
- O valor de desconto deve possuir no maximo 2 casas decimais.
- A data de expiracao e obrigatoria.
- A data de expiracao nao pode estar no passado.
- Um cupom pode nascer publicado ou nao.
- Cupom publicado nasce com status `ACTIVE`.
- Cupom nao publicado nasce com status `INACTIVE`.
- A exclusao e logica.
- Nao e permitido deletar um cupom ja deletado.

## Testes e Cobertura

Execute a suite de testes com verificacao de cobertura:

```bash
mvn verify
```

O JaCoCo esta configurado para exigir cobertura minima de 80% nas camadas com regra de negocio e superficie da API:

- controller;
- service;
- domain/model;
- domain/enums.

As camadas estruturais, como DTOs, exceptions, mapper, persistence e classe principal da aplicacao, ficam fora da regra de cobertura por nao concentrarem regra de negocio.

O relatorio HTML e gerado em:

```text
target/site/jacoco/index.html
```

## Estrutura do Projeto

```text
src/main/java/br/com/wilner/couponapi
|-- controller
|-- domain
|-- dto
|-- exception
|-- mapper
|-- persistence
`-- service
```

## Observacoes

- As migracoes de banco ficam em `src/main/resources/db/migration`.
- O Flyway valida a evolucao do schema ao iniciar a aplicacao.
- O schema possui constraints para reforcar regras tambem no banco, incluindo formato do codigo, faixa de desconto, status validos e ciclo de vida de exclusao logica.
