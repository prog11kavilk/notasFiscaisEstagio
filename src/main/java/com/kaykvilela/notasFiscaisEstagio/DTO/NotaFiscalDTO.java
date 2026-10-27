package com.kaykvilela.notasFiscaisEstagio.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// usado na requisição e na resposta de nota.
// Na requisição só valem codigoNota, clienteCodigo e itens; data, clienteNome e valorTotal são calculados e só aparecem na resposta.
public record NotaFiscalDTO(Integer codigoNota,
                            LocalDateTime data,
                            String clienteCodigo,
                            String clienteNome,
                            List<ItemDTO> itens,
                            BigDecimal valorTotal) {
}
