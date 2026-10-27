package com.kaykvilela.notasFiscaisEstagio.controller;

import com.kaykvilela.notasFiscaisEstagio.DTO.NotaFiscalDTO;
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
    public List<NotaFiscalDTO> listarTodasNotas() {
        return notaService.listarNotas();
    }

    @GetMapping("/{codigoNota}")
    @ResponseStatus(HttpStatus.OK)
    public NotaFiscalDTO buscarPeloCodigo(@PathVariable Integer codigoNota) {
        return notaService.devolveNota(codigoNota);
    }

    @GetMapping("/cliente/codigo/{codigoCliente}")
    @ResponseStatus(HttpStatus.OK)
    public List<NotaFiscalDTO> buscarPeloCodigoCliente(@PathVariable String codigoCliente) {
        return notaService.buscarNotaPeloCodigoCliente(codigoCliente);
    }

    @GetMapping("/cliente/nome/{nomeCliente}")
    @ResponseStatus(HttpStatus.OK)
    public List<NotaFiscalDTO> buscarPeloNomeCliente(@PathVariable String nomeCliente) {
        return notaService.buscarNotaPeloNomeCliente(nomeCliente);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public NotaFiscalDTO criarNota(@RequestBody NotaFiscalDTO request) {
        return notaService.createNota(request);
    }

    @PutMapping("/update/{codigoNota}")
    @ResponseStatus(HttpStatus.OK)
    public NotaFiscalDTO atualizarNota(
            @PathVariable Integer codigoNota,
            @RequestBody NotaFiscalDTO request) {
        return notaService.atualizarNota(codigoNota, request);
    }

    @DeleteMapping("/delete/{codigoNota}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Integer codigoNota) {
        notaService.excluirNota(codigoNota);
    }
}
