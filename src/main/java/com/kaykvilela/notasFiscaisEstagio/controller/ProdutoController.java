package com.kaykvilela.notasFiscaisEstagio.controller;

import com.kaykvilela.notasFiscaisEstagio.DTO.ProdutoDTO;
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
    public List<ProdutoDTO> listar() {
        return produtoService.buscaProdutos();
    }

    @GetMapping("/{codigo}")
    @ResponseStatus(HttpStatus.OK)
    public ProdutoDTO buscarPeloCodigo(@PathVariable String codigo) {
        return produtoService.buscarPeloCodigo(codigo);
    }

    @PostMapping({"/create"})
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoDTO criar(@RequestBody ProdutoDTO produto) {
        return produtoService.criaProduto(produto);
    }

    @PutMapping({"/update/{codigo}"})
    @ResponseStatus(HttpStatus.OK)
    public ProdutoDTO atualizar(
            @PathVariable String codigo,
            @RequestBody ProdutoDTO produto) {
        return produtoService.atualizaProduto(codigo, produto);
    }

    @DeleteMapping({"/delete/{codigo}"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable String codigo) {
        produtoService.deletaProduto(codigo);
    }
}
