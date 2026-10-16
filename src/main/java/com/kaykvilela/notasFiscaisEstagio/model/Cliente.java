package com.kaykvilela.notasFiscaisEstagio.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo", nullable = false)
    private Integer codigo;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "cpf", unique = true, nullable = false)
    private String cpf;
}
