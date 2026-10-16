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
    @Column(name = "numero_nota")
    private Integer numeroNota;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    // Muitas notas podem pertencer ao mesmo cliente.
    @ManyToOne
    @JoinColumn(name = "cliente_codigo", nullable = false)
    private Cliente cliente;

    //     Nome do cliente e total da nota ficam gravados na própria nota (colunas do GET).
    @Column(name = "cliente_nome", nullable = false)
    private String clienteNome;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    // Uma nota tem vários itens. O ITEM é captado pela chave estrangeira, ao mapear pela nota.
    @OneToMany(mappedBy ="nota",cascade = CascadeType.ALL, orphanRemoval = true)
    //salva como uma lista de itens para a nota referenciada
    private List<Item> itensNota = new ArrayList<>();

    // refaz cliente_nome e valor_total a partir do cliente e dos itens atuais da nota
    public void atualizaTotais() {
        this.clienteNome = this.cliente.getNome();
        BigDecimal total = BigDecimal.ZERO;
        for (Item item : this.itensNota) {
            total = total.add(item.getValorTotal());
        }
        this.valorTotal = total;
    }
}
