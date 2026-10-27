package com.kaykvilela.notasFiscaisEstagio.controller;

import com.kaykvilela.notasFiscaisEstagio.DTO.ItemDTO;
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

    @GetMapping("/nota/{codigoNota}")
    @ResponseStatus(HttpStatus.OK)
    public List<ItemDTO> listarDaNota(@PathVariable Integer codigoNota) {
        return itemService.buscaItensDaNota(codigoNota);
    }

    @GetMapping("/item/{codigo}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDTO buscarPeloCodigo(@PathVariable Integer codigo) {
        return itemService.buscarPeloCodigo(codigo);
    }

    @PostMapping("/create/{codigoNota}")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDTO criar(@PathVariable Integer codigoNota, @RequestBody ItemDTO item) {
        return itemService.addItem(codigoNota, item);
    }

    @PutMapping("/update/{codigo}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDTO atualizar(@PathVariable Integer codigo, @RequestBody ItemDTO item) {
        return itemService.atualizaQuantidade(codigo, item);
    }

    @DeleteMapping("/delete/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer codigo) {
        itemService.deletaItem(codigo);
    }
}
