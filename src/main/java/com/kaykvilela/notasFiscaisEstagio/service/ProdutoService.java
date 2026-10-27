package com.kaykvilela.notasFiscaisEstagio.service;

import com.kaykvilela.notasFiscaisEstagio.DTO.ProdutoDTO;
import com.kaykvilela.notasFiscaisEstagio.model.Produto;
import com.kaykvilela.notasFiscaisEstagio.repository.IProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final IProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public List<ProdutoDTO> buscaProdutos() {
        return produtoRepository.findAll().stream().map(this::converteParaDTO).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoDTO buscarPeloCodigo(String codigo) {
        return converteParaDTO(buscaEntidade(codigo));
    }

    @Transactional
    public ProdutoDTO criaProduto(ProdutoDTO dados) {
        validar(dados);
        if (dados.codigo() == null || dados.codigo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código do produto");
        }
        if (produtoRepository.existsByCodigo(dados.codigo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Já existe um produto com esse código");
        }
        Produto produto = new Produto();
        produto.setCodigo(dados.codigo());
        produto.setNome(dados.nome());
        produto.setValor(dados.valor());
        return converteParaDTO(produtoRepository.save(produto));
    }

    //atualiza nome e valor (o código não muda)
    @Transactional
    public ProdutoDTO atualizaProduto(String codigo, ProdutoDTO dados) {
        Produto produto = buscaEntidade(codigo);
        validar(dados);
        produto.setNome(dados.nome());
        produto.setValor(dados.valor());
        return converteParaDTO(produtoRepository.save(produto));
    }

    @Transactional
    public void deletaProduto(String codigo) {
        Produto produto = buscaEntidade(codigo);
        try {
            produtoRepository.delete(produto);
            produtoRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível excluir um produto vinculado a notas fiscais", ex);
        }
    }

    private ProdutoDTO converteParaDTO(Produto produto) {
        return new ProdutoDTO(produto.getCodigo(), produto.getNome(), produto.getValor());
    }

    private Produto buscaEntidade(String codigo) {
        return produtoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produto não encontrado!\nCódigo: " + codigo));
    }

    private void validar(ProdutoDTO produto) {
        if (produto == null || produto.nome() == null || produto.nome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome do produto");
        }
        if (produto.valor() == null || produto.valor().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor deve ser zero ou positivo");
        }
    }
}
