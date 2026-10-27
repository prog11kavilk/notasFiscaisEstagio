package com.kaykvilela.notasFiscaisEstagio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "nota_fiscal")
@Getter
@Setter
@NoArgsConstructor
public class NotaFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name="codigo_nota", nullable = false, unique = true)
    private Integer codigoNota;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    // Muitas notas podem pertencer ao mesmo cliente.
    @ManyToOne
    @JoinColumn(name = "cliente_codigo", nullable = false)
    private Cliente cliente;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    // Uma nota tem vários itens. O ITEM é captado pela chave estrangeira, ao mapear pela nota.
    @OneToMany(mappedBy ="nota",cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("item ASC")
    //salva como uma lista de itens para a nota referenciada
    private List<Item> itensNota = new ArrayList<>();
}
