package com.kaykvilela.notasFiscaisEstagio.controller;

import com.kaykvilela.notasFiscaisEstagio.model.Cliente;
import com.kaykvilela.notasFiscaisEstagio.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/nf/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping()
    public List<Cliente> buscaClientes() {
        return clienteService.buscaClientes();
    }

    @GetMapping("/{codigo}")
    public Cliente buscaClientePeloCodigo(@PathVariable Integer codigo) {
        return clienteService.buscaCliente(codigo);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente saveCliente(@RequestBody Cliente cliente) {
        return clienteService.addCliente(cliente);
    }

    @PutMapping("/update/{codigo}")
    public Cliente updateCliente(
            @PathVariable Integer codigo,
            @RequestBody Cliente cliente) {
        return clienteService.atualizaCliente(codigo, cliente);
    }

    @DeleteMapping({ "/delete/{codigo}"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCliente(@PathVariable Integer codigo) {
        clienteService.deletaCliente(codigo);
    }
}
