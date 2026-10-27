package com.kaykvilela.notasFiscaisEstagio.controller;

import com.kaykvilela.notasFiscaisEstagio.DTO.ClienteDTO;
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
    public List<ClienteDTO> buscaClientes() {
        return clienteService.buscaClientes();
    }

    @GetMapping("/{codigo}")
    public ClienteDTO buscarPeloCodigo(@PathVariable String codigo) {
        return clienteService.buscarPeloCodigo(codigo);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteDTO saveCliente(@RequestBody ClienteDTO cliente) {
        return clienteService.addCliente(cliente);
    }

    @PutMapping("/update/{codigo}")
    public ClienteDTO updateCliente(
            @PathVariable String codigo,
            @RequestBody ClienteDTO cliente) {
        return clienteService.atualizaCliente(codigo, cliente);
    }

    @DeleteMapping({ "/delete/{codigo}"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCliente(@PathVariable String codigo) {
        clienteService.deletaCliente(codigo);
    }
}
