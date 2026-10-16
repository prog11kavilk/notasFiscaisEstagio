package com.kaykvilela.notasFiscaisEstagio.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.engine.internal.Cascade;

import java.math.BigDecimal;

@Entity
@Table(name = "itens")
@Getter
@Setter
@NoArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo")
    private Integer codigo;

    // Vários itens podem usar o MESMO produto (em notas diferentes).
    @ManyToOne
    @JoinColumn(name = "codigo_produto", referencedColumnName ="codigo",nullable = false)
    private Produto produto;

//     Vários itens pertencem a uma nota -> reconhece a chave estrangeira
    @ManyToOne
    @JoinColumn(name = "nota")
    private NotaFiscal nota;

    // Nome do produto copiado no momento da venda (como o valor unitário, não muda se o produto mudar).
    @Column(name = "produto_nome", nullable = false)
    private String produtoNome;

    @Column(nullable = false)
    private Integer quantidade;

    // Preço copiado do produto no momento da venda (não muda se o produto mudar depois).
    @Column(name = "valor_unitario", nullable = false)
    private BigDecimal valorUnitario;

    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;

    //valor total = valor unitário x quantidade
    public void calculaValorTotal() {
        this.valorTotal = this.valorUnitario.multiply(new BigDecimal(quantidade));
    }
}