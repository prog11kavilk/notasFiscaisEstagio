# Notas Fiscais Estágio

API REST para o cadastro de clientes, produtos e notas fiscais, desenvolvida em Java 17 com Spring Boot, Spring Data JPA e PostgreSQL. O banco de dados é executado em um contêiner Docker, o que permite reproduzir o ambiente em qualquer máquina.

## Modelo de dados

O modelo é composto por quatro entidades. Cada uma possui um identificador interno numérico (`id`) e, quando aplicável, um código de negócio informado pelo usuário, que é único.

| Entidade | Tabela | Atributos principais |
|---|---|---|
| Cliente | `clientes` | `codigo` (texto, único), `nome`, `cpf` (único) |
| Produto | `produtos` | `codigo` (texto, único), `nome`, `valor` |
| NotaFiscal | `nota_fiscal` | `codigo_nota` (inteiro, único), `data`, `valor_total`, cliente |
| Item | `itens` | `item` (inteiro, único), `quantidade`, `valor_total`, nota, produto |

**Relacionamentos:** um cliente possui várias notas; uma nota possui vários itens; um produto pode aparecer em vários itens.

**Regras de negócio:**

- A data da nota é gerada pelo servidor no momento da criação.
- O `valor_total` do item é o valor do produto multiplicado pela quantidade. O `valor_total` da nota é a soma dos itens e é recalculado sempre que um item é criado, alterado ou removido.
- Uma nota deve conter pelo menos um item; por isso, o último item de uma nota não pode ser removido.
- Ao excluir uma nota, seus itens também são excluídos.
- Um cliente com notas, ou um produto presente em itens, não pode ser excluído (erro 409).
- Os códigos de nota e de item são únicos em todo o banco, e não apenas dentro de uma nota.

## Execução do projeto

### Pré-requisitos

- Java 17 e Maven (o projeto inclui o wrapper `./mvnw`)
- Docker e Docker Compose

### 1. Banco de dados no Docker

Copie o arquivo de exemplo de variáveis de ambiente e preencha a senha:

```bash
cp .env.example .env
```

| Variável | Finalidade | Valor padrão |
|---|---|---|
| `DB_USERNAME` | usuário do PostgreSQL | `postgres` |
| `DB_PASSWORD` | senha do PostgreSQL | (obrigatória) |
| `DB_PORT` | porta do banco na máquina local | `5434` |

Em seguida, inicie o contêiner:

```bash
docker compose up -d
```

Em instalações que possuem apenas a versão antiga do Compose, use `docker-compose up -d`. O banco `notaFiscal_db` é criado automaticamente na primeira execução, e os dados são mantidos no volume `notafiscal_data`, de modo que persistem quando o contêiner é reiniciado ou recriado.

Para verificar o estado do contêiner: `docker ps`.

### 2. Aplicação

A aplicação lê as credenciais das variáveis de ambiente `DB_USERNAME` e `DB_PASSWORD`, que devem ter os mesmos valores usados no passo anterior:

```bash
export DB_USERNAME=postgres
export DB_PASSWORD=sua_senha
./mvnw spring-boot:run
```

No IntelliJ IDEA, defina as duas variáveis em *Run > Edit Configurations > Environment variables*.

A URL de conexão está em `src/main/resources/application.properties` e aponta para a porta `5434`. Caso `DB_PORT` seja alterada, a porta da URL deve ser ajustada para o mesmo valor. A aplicação fica disponível em `http://localhost:8080`.

### Observações

- O Hibernate está configurado com `ddl-auto=update`: as tabelas são criadas na primeira execução e preservadas nas seguintes. Esse modo não corrige estruturas antigas. Se o banco foi criado por uma versão anterior do mapeamento, é necessário recriá-lo, removendo o volume com `docker compose down -v` (o que apaga todos os dados).
- Duas aplicações não podem usar a porta 8080 ao mesmo tempo. Se ocorrer o erro `Endereço já em uso`, encerre a instância anterior.

## Endpoints

Todas as requisições usam JSON (`Content-Type: application/json`). Os erros retornam o código HTTP correspondente e a mensagem no corpo da resposta: `400` para dados inválidos, `404` para registro inexistente e `409` para conflito (código ou CPF duplicado, ou exclusão de registro em uso).

### Clientes — `/nf/clientes`

| Método | Endpoint | Ação |
|---|---|---|
| GET | `/nf/clientes` | Listar clientes |
| GET | `/nf/clientes/{codigo}` | Buscar por código |
| POST | `/nf/clientes/create` | Criar |
| PUT | `/nf/clientes/update/{codigo}` | Atualizar nome e CPF |
| DELETE | `/nf/clientes/delete/{codigo}` | Excluir (409 se possuir notas) |

```json
{ "codigo": "C1", "nome": "Ana Souza", "cpf": "11122233344" }
```

### Produtos — `/nf/produtos`

| Método | Endpoint | Ação |
|---|---|---|
| GET | `/nf/produtos` | Listar produtos |
| GET | `/nf/produtos/{codigo}` | Buscar por código |
| POST | `/nf/produtos/create` | Criar |
| PUT | `/nf/produtos/update/{codigo}` | Atualizar nome e valor (valor maior ou igual a zero) |
| DELETE | `/nf/produtos/delete/{codigo}` | Excluir (409 se estiver em itens) |

```json
{ "codigo": "P1", "nome": "Teclado", "valor": 120.50 }
```

### Notas fiscais — `/nf/nota_fiscal`

| Método | Endpoint | Ação |
|---|---|---|
| GET | `/nf/nota_fiscal` | Listar todas as notas, ordenadas por código |
| GET | `/nf/nota_fiscal/{codigoNota}` | Buscar por código da nota |
| GET | `/nf/nota_fiscal/cliente/codigo/{codigoCliente}` | Notas de um cliente, pelo código (404 se o cliente não existir) |
| GET | `/nf/nota_fiscal/cliente/nome/{nomeCliente}` | Notas de clientes cujo nome contém o texto informado, sem distinção entre maiúsculas e minúsculas |
| POST | `/nf/nota_fiscal/create` | Criar nota com seus itens |
| PUT | `/nf/nota_fiscal/update/{codigoNota}` | Trocar o cliente e substituir todos os itens |
| DELETE | `/nf/nota_fiscal/delete/{codigoNota}` | Excluir a nota e seus itens |

Os campos `data`, `clienteNome` e `valorTotal` são calculados pelo servidor e só aparecem na resposta. O mesmo formato (`NotaFiscalDTO`) é usado em todos os métodos.

Requisição (POST e PUT; no PUT, o código da nota vem na URL):

```json
{
  "codigoNota": 1001,
  "clienteCodigo": "C1",
  "itens": [
    { "item": 1, "produtoCodigo": "P1", "quantidade": 2 },
    { "item": 2, "produtoCodigo": "P2", "quantidade": 1 }
  ]
}
```

Resposta:

```json
{
  "codigoNota": 1001,
  "data": "2026-10-27T15:27:29.172567",
  "clienteCodigo": "C1",
  "clienteNome": "Ana Souza",
  "itens": [
    { "item": 1, "produtoCodigo": "P1", "produtoNome": "Teclado", "valorUnitario": 120.50, "quantidade": 2, "valorTotal": 241.00 },
    { "item": 2, "produtoCodigo": "P2", "produtoNome": "Mouse", "valorUnitario": 50.00, "quantidade": 1, "valorTotal": 50.00 }
  ],
  "valorTotal": 291.00
}
```

As notas são listadas em ordem crescente de `codigoNota`, e os itens de cada nota em ordem crescente de `item`.

### Itens — `/nf/itens`

| Método | Endpoint | Ação |
|---|---|---|
| GET | `/nf/itens/nota/{codigoNota}` | Itens de uma nota (404 se a nota não existir) |
| GET | `/nf/itens/item/{codigo}` | Buscar um item pelo código |
| POST | `/nf/itens/create/{codigoNota}` | Adicionar item a uma nota existente |
| PUT | `/nf/itens/update/{codigo}` | Alterar apenas a quantidade |
| DELETE | `/nf/itens/delete/{codigo}` | Remover item (a nota deve manter ao menos um) |

Criação: `{ "item": 3, "produtoCodigo": "P1", "quantidade": 2 }`. Atualização: `{ "quantidade": 5 }`. A quantidade deve ser maior que zero.

## Consultas SQL

O arquivo `src/main/resources/queries.sql` contém duas consultas que reproduzem diretamente no banco o conteúdo devolvido pela API:

1. a nota em formato tabular, com uma linha por item;
2. a nota no formato do `NotaFiscalDTO`, com uma linha por nota e os itens agrupados em JSON (`json_agg`).

Como `nota_fiscal.cliente_codigo` e `itens.codigo_produto` armazenam o `id` numérico das chaves estrangeiras, e não o código de texto, as junções utilizam `clientes.id` e `produtos.id`.

Para executá-las no contêiner:

```bash
docker exec -i notafiscal-db psql -U postgres -d notaFiscal_db < src/main/resources/queries.sql
```

## Validação

O projeto não possui testes automatizados. A verificação do comportamento é feita com a coleção do Postman na pasta `postman/`, organizada por entidade e com casos de erro (códigos duplicados, registros inexistentes e exclusões em uso). Ela deve ser executada com a aplicação em funcionamento e conectada ao banco do Docker.
