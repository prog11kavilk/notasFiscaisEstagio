package com.kaykvilela.notasFiscaisEstagio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
// o número do item é único só dentro da nota; notas diferentes podem repetir o mesmo número
@Table(name = "itens",
        uniqueConstraints = @UniqueConstraint(columnNames = {"nota", "item"}))
@Getter
@Setter
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name="item", nullable = false)
    private Integer item;

    // Vários itens podem usar o MESMO produto (em notas diferentes).
    @ManyToOne
    @JoinColumn(name = "codigo_produto", nullable = false)
    private Produto produto;

//     Vários itens pertencem a uma nota -> reconhece a chave estrangeira
    @ManyToOne
    @JoinColumn(name = "nota")
    private NotaFiscal nota;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;
}
