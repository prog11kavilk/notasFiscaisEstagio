package com.kaykvilela.notasFiscaisEstagio.service;

import com.kaykvilela.notasFiscaisEstagio.DTO.ItemDTORequest;
import com.kaykvilela.notasFiscaisEstagio.DTO.ItemDTOResposta;
import com.kaykvilela.notasFiscaisEstagio.model.Item;
import com.kaykvilela.notasFiscaisEstagio.model.NotaFiscal;
import com.kaykvilela.notasFiscaisEstagio.model.Produto;
import com.kaykvilela.notasFiscaisEstagio.repository.INotaRepository;
import com.kaykvilela.notasFiscaisEstagio.repository.IProdutoRepository;
import com.kaykvilela.notasFiscaisEstagio.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
// regras de negócio dos itens de uma nota
public class ItemService {

    private final ItemRepository itemRepo;
    private final INotaRepository notaRepo;
    private final IProdutoRepository produtoRepo;

    // lista os itens de uma nota
    @Transactional(readOnly = true)
    public List<ItemDTOResposta> buscaItensDaNota(Integer numeroNota) {
        if (!notaRepo.existsById(numeroNota)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Nota não encontrada!\nID: " + numeroNota);
        }
        List<ItemDTOResposta> itensResposta = new ArrayList<>();
        for (Item item : itemRepo.findByNotaNumeroNota(numeroNota)) {
            itensResposta.add(converteParaItemResponse(item));
        }
        return itensResposta;
    }

    //busca um item qualquer
    @Transactional(readOnly = true)
    public ItemDTOResposta buscaItem(Integer codigo_item) {
        return converteParaItemResponse(buscaEntidadeItem(codigo_item));
    }

    // adiciona um item numa nota que já existe
    @Transactional
    public ItemDTOResposta addItem(Integer numeroNota, ItemDTORequest dados) {
        NotaFiscal nota = notaRepo.findById(numeroNota)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Nota não encontrada!\nID: " + numeroNota));
        if (dados == null || dados.produtoCodigo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código do produto do item");
        }
        Item item = montaItem(nota, dados.produtoCodigo(), dados.quantidade());
        nota.getItensNota().add(item);
        nota.atualizaTotais();
        notaRepo.flush(); //fecha a conexão c/ o banco de notas
        return converteParaItemResponse(item);
    }

    // muda só a quantidade de um item
    @Transactional
    public ItemDTOResposta atualizaQuantidade(Integer codigo_item, ItemDTORequest dados) {
        Item item = buscaEntidadeItem(codigo_item);
        validaQuantidade(dados == null ? null : dados.quantidade());
        item.setQuantidade(dados.quantidade());
        item.calculaValorTotal();
        item.getNota().atualizaTotais();
        return converteParaItemResponse(item);
    }

    // remove um item da nota
    @Transactional
    public void deletaItem(Integer codigo_item) {
        Item item = buscaEntidadeItem(codigo_item);
        NotaFiscal nota = item.getNota();
        if (nota.getItensNota().size() == 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A nota precisa ter pelo menos 1 item. Apague a nota.");
        }
        nota.getItensNota().remove(item); // O orphanRemoval apaga do banco
        nota.atualizaTotais();
    }

    // cria um Item para alguma nota, utilizando o valor do produto que o formou
    public Item montaItem(NotaFiscal nota, Integer codigoProduto, Integer quantidade) {
        if (codigoProduto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código do produto do item");
        }
        validaQuantidade(quantidade);
        Produto produto = produtoRepo.findById(codigoProduto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produto não encontrado!\nID: " + codigoProduto));

        Item item = new Item();
        item.setNota(nota);
        item.setProduto(produto);
        item.setProdutoNome(produto.getNome());
        item.setQuantidade(quantidade);
        item.setValorUnitario(produto.getValor());
        item.calculaValorTotal();
        return item;
    }

    // transforma o Item (entidade) no ItemDTO (o que a API devolve)
    public ItemDTOResposta converteParaItemResponse(Item item) {
        return new ItemDTOResposta(
                item.getCodigo(),
//                item.getProduto().getCodigo(),
                item.getProdutoNome(),
                item.getValorUnitario(),
                item.getQuantidade(),
                item.getValorTotal());
    }

    private Item buscaEntidadeItem(Integer codigo_item) {
        return itemRepo.findById(codigo_item)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item não encontrado! ID: " + codigo_item));
    }

    private void validaQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quantidade deve ser maior que zero");
        }
    }
}
