package com.kaykvilela.notasFiscaisEstagio.controller;

import com.kaykvilela.notasFiscaisEstagio.DTO.ItemDTORequest;
import com.kaykvilela.notasFiscaisEstagio.DTO.ItemDTOResposta;
import com.kaykvilela.notasFiscaisEstagio.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/nf/itens")

public class ItemController {

    private final ItemService itemService;

    @GetMapping("/nota/{numeroNota}")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDTOResposta> listarDaNota(@PathVariable Integer numeroNota) {
        return itemService.buscaItensDaNota(numeroNota);
    }

    @GetMapping("/item/{codigo}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDTOResposta listarItem(@PathVariable Integer codigo) {
        return itemService.buscaItem(codigo);
    }

    // corpo: {"produtoCodigo":1,"quantidade":2}
    @PostMapping("/create/{numeroNota}")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDTOResposta criar(@PathVariable Integer numeroNota, @RequestBody ItemDTORequest item) {
        return itemService.addItem(numeroNota, item);
    }

    @PutMapping("/update/{codigo}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDTOResposta atualizar(@PathVariable Integer codigo, @RequestBody ItemDTORequest item) {
        return itemService.atualizaQuantidade(codigo, item);
    }

    @DeleteMapping("/delete/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer codigo) {
        itemService.deletaItem(codigo);
    }
}
