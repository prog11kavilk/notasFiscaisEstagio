package com.kaykvilela.notasFiscaisEstagio.controller;

import com.kaykvilela.notasFiscaisEstagio.model.Produto;
import com.kaykvilela.notasFiscaisEstagio.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nf/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<Produto> listar() {
        return produtoService.buscaProdutos();
    }

    @GetMapping("/{codigo}")
    @ResponseStatus(HttpStatus.OK)
    public Produto buscar(@PathVariable Integer codigo) {
        return produtoService.buscaProduto(codigo);
    }

    @PostMapping({"/create"})
    @ResponseStatus(HttpStatus.CREATED)
    public Produto criar(@RequestBody Produto produto) {
        return produtoService.criaProduto(produto);
    }

    @PutMapping({"/update/{codigo}"})
    @ResponseStatus(HttpStatus.OK)
    public Produto atualizar(
            @PathVariable Integer codigo,
            @RequestBody Produto produto) {
        return produtoService.atualizaProduto(codigo, produto);
    }

    @DeleteMapping({"/delete/{codigo}"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer codigo) {
        produtoService.deletaProduto(codigo);
    }
}
