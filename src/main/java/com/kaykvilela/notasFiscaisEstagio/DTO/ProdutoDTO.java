package com.kaykvilela.notasFiscaisEstagio.DTO;

import java.math.BigDecimal;

// usado na requisição e na resposta de produto
public record ProdutoDTO(String codigo,
                         String nome,
                         BigDecimal valor) {
}
