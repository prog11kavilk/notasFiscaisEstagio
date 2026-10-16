package com.kaykvilela.notasFiscaisEstagio.DTO;

//o que vai no body do Json para criar/alterar um item
public record ItemDTORequest(Integer produtoCodigo,
                             Integer quantidade){
}
