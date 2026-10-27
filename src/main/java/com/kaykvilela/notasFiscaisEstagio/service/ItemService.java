package com.kaykvilela.notasFiscaisEstagio.service;

import com.kaykvilela.notasFiscaisEstagio.DTO.ItemDTO;
import com.kaykvilela.notasFiscaisEstagio.model.Item;
import com.kaykvilela.notasFiscaisEstagio.model.NotaFiscal;
import com.kaykvilela.notasFiscaisEstagio.model.Produto;
import com.kaykvilela.notasFiscaisEstagio.repository.INotaRepository;
import com.kaykvilela.notasFiscaisEstagio.repository.IProdutoRepository;
import com.kaykvilela.notasFiscaisEstagio.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
// regras de negócio dos itens de uma nota
public class ItemService {

    private final ItemRepository itemRepo;
    private final INotaRepository notaRepo;
    private final IProdutoRepository produtoRepo;

    // NotaService já depende do ItemService (montaItem), então esta ponta é lazy para não fechar um ciclo
    @Lazy
    @Autowired
    private NotaService notaService;

    // lista os itens de uma nota
    @Transactional(readOnly = true)
    public List<ItemDTO> buscaItensDaNota(Integer codigoNota) {
        if (!notaRepo.existsByCodigoNota(codigoNota)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Nota não encontrada!\nCódigo: " + codigoNota);
        }
        List<ItemDTO> itensResposta = new ArrayList<>();
        for (Item item : itemRepo.findByNotaCodigoNota(codigoNota)) {
            itensResposta.add(converteParaItemResponse(item));
        }
        return itensResposta;
    }

    //busca um item pelo seu código
    @Transactional(readOnly = true)
    public ItemDTO buscarPeloCodigo(Integer codigoItem) {
        return converteParaItemResponse(buscaEntidadeItem(codigoItem));
    }

    // adiciona um item numa nota que já existe
    @Transactional
    public ItemDTO addItem(Integer codigoNota, ItemDTO dados) {
        NotaFiscal nota = notaService.buscarPeloCodigo(codigoNota);
        if (dados == null || dados.produtoCodigo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código do produto do item");
        }
        Item item = montaItem(nota, dados.item(), dados.produtoCodigo(), dados.quantidade());
        nota.getItensNota().add(item);
        notaService.atualizaTotais(nota);
        notaRepo.flush(); //fecha a conexão c/ o banco de notas
        return converteParaItemResponse(item);
    }

    // muda só a quantidade de um item
    @Transactional
    public ItemDTO atualizaQuantidade(Integer codigoItem, ItemDTO dados) {
        Item item = buscaEntidadeItem(codigoItem);
        validaQuantidade(dados == null ? null : dados.quantidade());
        item.setQuantidade(dados.quantidade());
        calculaValorTotal(item);
        notaService.atualizaTotais(item.getNota());
        return converteParaItemResponse(item);
    }

    // remove um item da nota
    @Transactional
    public void deletaItem(Integer codigoItem) {
        Item item = buscaEntidadeItem(codigoItem);
        NotaFiscal nota = item.getNota();
        if (nota.getItensNota().size() == 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A nota precisa ter pelo menos 1 item. Apague a nota.");
        }
        nota.getItensNota().remove(item); // O orphanRemoval apaga do banco
        notaService.atualizaTotais(nota);
    }

    // cria um Item para alguma nota, utilizando o valor do produto que o formou
    public Item montaItem(NotaFiscal nota, Integer codigoItem, String codigoProduto, Integer quantidade) {
        if (codigoItem == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código do item");
        }
        if (itemRepo.existsByItem(codigoItem)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um item com esse código");
        }
        if (codigoProduto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código do produto do item");
        }
        validaQuantidade(quantidade);
        Produto produto = produtoRepo.findByCodigo(codigoProduto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produto não encontrado!\nCódigo: " + codigoProduto));

        Item item = new Item();
        item.setItem(codigoItem);
        item.setNota(nota);
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        calculaValorTotal(item);
        return item;
    }

    //valor total = valor unitário (do produto) x quantidade
    public void calculaValorTotal(Item item) {
        item.setValorTotal(item.getProduto().getValor().multiply(BigDecimal.valueOf(item.getQuantidade())));
    }

    // transforma o Item (entidade) no ItemDTO (o que a API devolve)
    public ItemDTO converteParaItemResponse(Item item) {
        return new ItemDTO(
                item.getItem(),
                item.getProduto().getCodigo(),
                item.getProduto().getNome(),
                item.getProduto().getValor(),
                item.getQuantidade(),
                item.getValorTotal());
    }

    private Item buscaEntidadeItem(Integer codigoItem) {
        return itemRepo.findByItem(codigoItem)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item não encontrado!\nCódigo: " + codigoItem));
    }

    private void validaQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quantidade deve ser maior que zero");
        }
    }
}
