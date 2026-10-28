package com.kaykvilela.notasFiscaisEstagio.DTO;

import java.math.BigDecimal;

// usado na requisição e na resposta de item.
// Na requisição só valem item, produtoCodigo e quantidade; o resto é calculado e só aparece na resposta.
public record ItemDTO(Integer item,
                      String produtoCodigo,
                      String produtoNome,
                      BigDecimal valorUnitario,
                      Integer quantidade,
                      BigDecimal subtotal) {
}
