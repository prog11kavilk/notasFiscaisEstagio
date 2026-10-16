package com.kaykvilela.notasFiscaisEstagio.DTO;

import java.util.List;

// o que vai no body do Json para criar/alterar uma nota: cliente e itens (produtoCodigo + quantidade)
public record NotaFiscalDTO(
        Integer clienteCodigo,
        List<ItemDTORequest> itens) {
}
