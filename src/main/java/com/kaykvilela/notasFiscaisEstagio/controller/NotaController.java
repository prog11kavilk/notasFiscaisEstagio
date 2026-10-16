package com.kaykvilela.notasFiscaisEstagio.controller;

import com.kaykvilela.notasFiscaisEstagio.DTO.NotaFiscalDTO;
import com.kaykvilela.notasFiscaisEstagio.DTO.NotaFiscalResposta;
import com.kaykvilela.notasFiscaisEstagio.DTO.NotaItemResposta;
import com.kaykvilela.notasFiscaisEstagio.service.NotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nf/nota_fiscal")
@RequiredArgsConstructor
public class NotaController {

    private final NotaService notaService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<NotaItemResposta> listarTodasNotas() {
        return notaService.listarNotas();
    }

    @GetMapping("/{numeroNota}")
    @ResponseStatus(HttpStatus.OK)
    public List<NotaItemResposta> buscarNota(@PathVariable Integer numeroNota) {
        return notaService.devolveNota(numeroNota);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public NotaFiscalResposta criarNota(@RequestBody NotaFiscalDTO request) {
        return notaService.createNota(request);
    }

    @PutMapping("/update/{numeroNota}")
    @ResponseStatus(HttpStatus.OK)
    public NotaFiscalResposta atualizarNota(
            @PathVariable Integer numeroNota,
            @RequestBody NotaFiscalDTO request) {
        return notaService.atualizarNota(numeroNota, request);
    }

    @DeleteMapping("/delete/{numeroNota}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer numeroNota) {
        notaService.excluirNota(numeroNota);
    }
}
