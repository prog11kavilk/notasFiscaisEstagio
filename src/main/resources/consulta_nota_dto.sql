-- Consultas que devolvem os mesmos campos do NotaFiscalDTO (o que o GET /nf/nota_fiscal retorna).
-- nota_fiscal.cliente_codigo e itens.codigo_produto guardam o ID numérico (FK), por isso os JOINs usam c.id e p.id.
-- itens.item só é único dentro da nota (nota + item), então sempre junte os itens pela nota.

-- 1) em linhas: uma linha por item, com os dados da nota repetidos
SELECT n.codigo_nota   AS "codigoNota",
       n.data          AS "data",
       c.codigo        AS "clienteCodigo",
       c.nome          AS "clienteNome",
       i.item          AS "item",
       p.codigo        AS "produtoCodigo",
       p.nome          AS "produtoNome",
       p.valor         AS "valorUnitario",
       i.quantidade    AS "quantidade",
       i.valor_total   AS "subtotal",
       n.valor_total   AS "valorTotal"
FROM nota_fiscal n
JOIN clientes c ON c.id = n.cliente_codigo
JOIN itens i    ON i.nota = n.id
JOIN produtos p ON p.id = i.codigo_produto
ORDER BY n.codigo_nota, i.item;

-- 2) uma linha por nota, com os itens agrupados em JSON (mesmo formato do DTO)
SELECT n.codigo_nota   AS "codigoNota",
       n.data          AS "data",
       c.codigo        AS "clienteCodigo",
       c.nome          AS "clienteNome",
       json_agg(json_build_object(
                    'item',          i.item,
                    'produtoCodigo', p.codigo,
                    'produtoNome',   p.nome,
                    'valorUnitario', p.valor,
                    'quantidade',    i.quantidade,
                    'subtotal',      i.valor_total)
                ORDER BY i.item) AS "itens",
       n.valor_total   AS "valorTotal"
FROM nota_fiscal n
JOIN clientes c ON c.id = n.cliente_codigo
JOIN itens i    ON i.nota = n.id
JOIN produtos p ON p.id = i.codigo_produto
GROUP BY n.id, c.id
ORDER BY n.codigo_nota;

-- 3) conferência: o mesmo número de item aparecendo em mais de uma nota
SELECT i.item, count(*) AS "qtdNotas", array_agg(n.codigo_nota ORDER BY n.codigo_nota) AS "notas"
FROM itens i
JOIN nota_fiscal n ON n.id = i.nota
GROUP BY i.item
HAVING count(*) > 1
ORDER BY i.item;
