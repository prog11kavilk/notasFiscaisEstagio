# Notas Fiscais Estágio

API REST com Java, Spring Boot, Spring Data JPA e PostgreSQL.

## Modelo

- **Cliente** (`clientes`): nome e CPF (único).
- **Produto** (`produtos`): nome e valor.
- **NotaFiscal** (`nota_fiscal`): colunas `numero_nota`, `data`, `cliente_codigo`, `cliente_nome` e `valor_total` (as mesmas do GET). `cliente_nome` e `valor_total` ficam gravados na nota e são recalculados ao criar/atualizar a nota, ao adicionar/alterar/remover itens e ao renomear o cliente. Uma nota tem vários itens.
- **Item** (`itens`): pertence a uma nota e referencia um produto, com `quantidade`, `valor_unitario` (copiado do produto no momento da venda, não muda se o produto mudar depois) e `valor_total` (`valor_unitario x quantidade`).

Relacionamentos: Cliente 1—N NotaFiscal, NotaFiscal 1—N Item, Produto 1—N Item. Excluir uma nota apaga seus itens; não é possível excluir cliente ou produto que já estejam em notas (409).

## Configuração do PostgreSQL

Crie o banco `notaFiscal_db` e ajuste usuário/senha em `src/main/resources/application.properties`. O Hibernate usa `ddl-auto=update`; o SQL equivalente está em `src/main/resources/queries.sql`.

```bash
./mvnw spring-boot:run
```

> Se o banco foi criado pela versão antiga do mapeamento (tabelas `itens_nota` / `itens` antigas e coluna `nota_fiscal.cliente_id`), rode `src/main/resources/migracao_itens.sql` uma vez antes de subir. Para as colunas `data`, `cliente_nome` e `valor_total` da nota, rode também `src/main/resources/migracao_nota_colunas.sql`. O `update` do Hibernate não corrige estruturas antigas.

## Endpoints

Base: `http://localhost:8080`. Body -> raw -> JSON no Postman. Erros voltam com o status HTTP e a mensagem no corpo.

### Clientes — `/nf/clientes`

| Método | Endpoint | Ação |
|---|---|---|
| GET | `/nf/clientes` | Listar |
| GET | `/nf/clientes/{codigo}` | Buscar |
| POST | `/nf/clientes/create` | Criar `{"nome":"Ana Souza","cpf":"12345678900"}` |
| PUT | `/nf/clientes/update/{codigo}` | Atualizar (nome e CPF obrigatórios; CPF não pode repetir) |
| DELETE | `/nf/clientes/delete/{codigo}` | Excluir (409 se tiver notas) |

### Produtos — `/nf/produtos`

| Método | Endpoint | Ação |
|---|---|---|
| GET | `/nf/produtos` | Listar |
| GET | `/nf/produtos/{codigo}` | Buscar |
| POST | `/nf/produtos/create` | Criar `{"nome":"Teclado","valor":120.50}` |
| PUT | `/nf/produtos/update/{codigo}` | Atualizar (nome obrigatório, valor >= 0) |
| DELETE | `/nf/produtos/delete/{codigo}` | Excluir (409 se estiver em notas) |

### Notas fiscais — `/nf/nota_fiscal`

| Método | Endpoint | Ação |
|---|---|---|
| GET | `/nf/nota_fiscal` | Listar |
| GET | `/nf/nota_fiscal/{numeroNota}` | Buscar |
| POST | `/nf/nota_fiscal/create` | Criar |
| PUT | `/nf/nota_fiscal/update/{numeroNota}` | Trocar cliente e substituir todos os itens |
| DELETE | `/nf/nota_fiscal/delete/{numeroNota}` | Excluir nota e itens |

Criar/atualizar (a data é gerada pelo servidor; o `valorTotal` é a soma dos itens):

```json
{
  "clienteCodigo": 1,
  "item": [
    { "produtoCodigo": 1, "quantidade": 2 },
    { "produtoCodigo": 2, "quantidade": 3 }
  ]
}
```

Resposta:

```json
{
  "numeroNota": 1,
  "data": "2026-10-15T15:30:00",
  "clienteCodigo": 1,
  "clienteNome": "Ana Souza",
  "item": [
    { "produtoCodigo": 1, "produtoNome": "Teclado", "valor": 120.50, "quantidade": 2, "valorTotal": 241.00 }
  ],
  "valorTotal": 391.00
}
```

### Itens — `/nf/itens`

| Método | Endpoint | Ação |
|---|---|---|
| GET | `/nf/itens/nota/{numeroNota}` | Itens de uma nota (404 se a nota não existe) |
| GET | `/nf/itens/item/{codigo}` | Buscar um item |
| POST | `/nf/itens/create/{numeroNota}` | Adicionar item `{"produto":{"codigo":1},"quantidade":2}` |
| PUT | `/nf/itens/update/{codigo}` | Alterar só a quantidade `{"quantidade":5}` |
| DELETE | `/nf/itens/delete/{codigo}` | Remover (a nota precisa manter pelo menos 1 item) |

## Testes

```bash
./mvnw test
```

Os testes usam H2 em memória e verificam os relacionamentos nota ↔ cliente ↔ itens ↔ produto.
