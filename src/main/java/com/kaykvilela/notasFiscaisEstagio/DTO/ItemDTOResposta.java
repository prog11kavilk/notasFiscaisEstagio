package com.kaykvilela.notasFiscaisEstagio.DTO;

import java.math.BigDecimal;

public record ItemDTOResposta(Integer codigo,
                              Integer produtoCodigo,
                              String produtoNome,
                              BigDecimal valorUnitario,
                              Integer quantidade,
                              BigDecimal valorTotal
                              ) {
}
