package com.kaykvilela.notasFiscaisEstagio.DTO;

import java.math.BigDecimal;

// Produto dentro da nota: nome, valor unitário valor unitário
public record ProdutoDTO(
        String produtoNome,
        BigDecimal produtoValor,
        Integer quantidade) {
}
