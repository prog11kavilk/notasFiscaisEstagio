package com.kaykvilela.notasFiscaisEstagio.DTO;

import java.time.LocalDateTime;
import java.util.List;

// Nota com seus produtos e, ao fim, o valor total (soma do valorTotal de cada item).
// Na entrada, só clienteCodigo e item (produtoCodigo + quantidade) são usados.
public record NotaFiscalDTO(
        Integer clienteCodigo,
        List<ItemDTORequest>itens){
}
