package com.kaykvilela.notasFiscaisEstagio.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record NotaFiscalResposta (Integer numeroNota,
                                 LocalDateTime data,
//                                 Integer clienteCodigo,
                                 String clienteNome,
                                 List<ItemDTOResposta> itens,
                                 BigDecimal valorTotal){}

