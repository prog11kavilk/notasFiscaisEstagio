nota_fiscal.cliente_codigo e itens.codigo_produto guardam o ID numérico (FK), não o código de texto
por isso os JOINs usam c.id e p.id

1) nota em linhas: uma linha por item, com os dados da nota repetidos
SELECT n.codigo_nota,
       n.data,
       c.codigo        AS cliente_codigo,
       c.nome          AS cliente_nome,
       i.item,
       p.codigo        AS produto_codigo,
       p.nome          AS produto_nome,
       p.valor         AS valor_unitario,
       i.quantidade,
       i.valor_total   AS valor_total_item,
       n.valor_total   AS valor_total_nota
FROM nota_fiscal n
JOIN clientes c ON c.id = n.cliente_codigo
JOIN itens i    ON i.nota = n.id
JOIN produtos p ON p.id = i.codigo_produto
ORDER BY n.codigo_nota, i.item;


