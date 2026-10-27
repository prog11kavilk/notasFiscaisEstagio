package com.kaykvilela.notasFiscaisEstagio.DTO;

// usado na requisição e na resposta de cliente
public record ClienteDTO(String codigo,
                         String nome,
                         String cpf) {
}
