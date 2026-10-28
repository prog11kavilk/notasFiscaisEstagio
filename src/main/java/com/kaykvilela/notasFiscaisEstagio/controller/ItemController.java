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

    @GetMapping("/nota/{codigoNota}/item/{codigo}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDTO buscarPeloCodigo(@PathVariable Integer codigoNota, @PathVariable Integer codigo) {
        return itemService.buscarPeloCodigo(codigoNota, codigo);
    }

    @PostMapping("/create/{codigoNota}")
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDTO criar(@PathVariable Integer codigoNota, @RequestBody ItemDTO item) {
        return itemService.addItem(codigoNota, item);
    }

    @PutMapping("/nota/{codigoNota}/update/{codigo}")
    @ResponseStatus(HttpStatus.OK)
    public ItemDTO atualizar(@PathVariable Integer codigoNota, @PathVariable Integer codigo, @RequestBody ItemDTO item) {
        return itemService.atualizaQuantidade(codigoNota, codigo, item);
    }

    @DeleteMapping("/nota/{codigoNota}/delete/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer codigoNota, @PathVariable Integer codigo) {
        itemService.deletaItem(codigoNota, codigo);
    }
}
