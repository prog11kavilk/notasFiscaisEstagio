package com.kaykvilela.notasFiscaisEstagio.service;

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
    public List<Produto> buscaProdutos() {
        return produtoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Produto buscaProduto(Integer codigo) {
        return produtoRepository.findById(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }

    @Transactional
    public Produto criaProduto(Produto produto) {
        validar(produto);
        produto.setCodigo(null);
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto atualizaProduto(Integer codigo, Produto dados) {
        Produto produto = buscaProduto(codigo);
        validar(dados);
        produto.setNome(dados.getNome());
        produto.setValor(dados.getValor());
        return produtoRepository.save(produto);
    }

    @Transactional
    public void deletaProduto(Integer codigo) {
        Produto produto = buscaProduto(codigo);
        try {
            produtoRepository.delete(produto);
            produtoRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível excluir um produto vinculado a notas fiscais", ex);
        }
    }

    private void validar(Produto produto) {
        if (produto == null || produto.getNome() == null || produto.getNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome do produto");
        }
        if (produto.getValor() == null || produto.getValor().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor deve ser zero ou positivo");
        }
    }
}
