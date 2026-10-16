package com.kaykvilela.notasFiscaisEstagio.service;

import com.kaykvilela.notasFiscaisEstagio.model.Cliente;
import com.kaykvilela.notasFiscaisEstagio.model.NotaFiscal;
import com.kaykvilela.notasFiscaisEstagio.repository.IClienteRepository;
import com.kaykvilela.notasFiscaisEstagio.repository.INotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
//gerencia o crud de clientes seguindo a regra de negócio
public class ClienteService {

    private final IClienteRepository clientesRepository;
    private final INotaRepository notaRepository;

    //aponta todos os clientes do banco
    @Transactional(readOnly = true)
    public List<Cliente> buscaClientes() {
        return clientesRepository.findAll();
    }

    //busca um cliente em específico pelo seu código
    @Transactional(readOnly = true)
    public Cliente buscaCliente(Integer codigo) {
        return clientesRepository.findById(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado!\nID:"+codigo));
    }
    //adiciona um cliente se o cpf dele não for duplicado
    @Transactional
    public Cliente addCliente(Cliente newClient) {
        validaCliente(newClient);

        if (clientesRepository.existsByCpf(newClient.getCpf())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Já existe um cliente com esse CPF");
        }

        Cliente cliente = Cliente.builder()
                .nome(newClient.getNome())
                .cpf(newClient.getCpf())
                .build();
        return clientesRepository.save(cliente);
    }

    //atualiza os dados de um cliente cadastrado
    @Transactional
    public Cliente atualizaCliente(Integer codigo, Cliente dados) {
        Cliente cliente = buscaCliente(codigo);
        validaCliente(dados);

        if (!dados.getCpf().equals(cliente.getCpf()) && clientesRepository.existsByCpf(dados.getCpf())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Já existe um cliente com esse CPF");
        }
        cliente.setNome(dados.getNome());
        cliente.setCpf(dados.getCpf());
        //o nome gravado nas notas do cliente acompanha a mudança
        for (NotaFiscal nota : notaRepository.findByClienteCodigo(codigo)) {
            nota.setClienteNome(cliente.getNome());
        }
        return clientesRepository.save(cliente);
    }

    //remove o cliente pelo codigo
    @Transactional
    public void deletaCliente(Integer codigo) {
        Cliente cliente = buscaCliente(codigo);
        try {
            clientesRepository.delete(cliente);
            clientesRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível excluir um cliente que possui notas fiscais.", ex);
        }
    }

    //nome e CPF são obrigatórios
    private void validaCliente(Cliente cliente) {
        if (cliente == null || cliente.getNome() == null || cliente.getNome().isBlank()
                || cliente.getCpf() == null || cliente.getCpf().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe nome e CPF do cliente");
        }
    }
}
