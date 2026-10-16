package com.kaykvilela.notasFiscaisEstagio.DTO;

import java.math.BigDecimal;

// uma linha por item da nota: é o formato do GET de notas
public record NotaItemResposta(Integer numeroNota,
                               Integer codigoItem,
                               String clienteNome,
                               String produtoNome,
                               BigDecimal valorUnitario,
                               Integer quantidade,
                               BigDecimal valorTotal) {
}
