package com.kaykvilela.notasFiscaisEstagio.service;

import com.kaykvilela.notasFiscaisEstagio.DTO.ClienteDTO;
import com.kaykvilela.notasFiscaisEstagio.model.Cliente;
import com.kaykvilela.notasFiscaisEstagio.repository.IClienteRepository;
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

    //aponta todos os clientes do banco
    @Transactional(readOnly = true)
    public List<ClienteDTO> buscaClientes() {
        return clientesRepository.findAll().stream().map(this::converteParaDTO).toList();
    }

    //busca um cliente em específico pelo seu código
    @Transactional(readOnly = true)
    public ClienteDTO buscarPeloCodigo(String codigo) {
        return converteParaDTO(buscaEntidade(codigo));
    }

    //adiciona um cliente se o código e o cpf dele não forem duplicados
    @Transactional
    public ClienteDTO addCliente(ClienteDTO dados) {
        validaCliente(dados);
        if (dados.codigo() == null || dados.codigo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código do cliente");
        }
        if (clientesRepository.existsByCodigo(dados.codigo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Já existe um cliente com esse código");
        }
        if (clientesRepository.existsByCpf(dados.cpf())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Já existe um cliente com esse CPF");
        }

        Cliente cliente = Cliente.builder()
                .codigo(dados.codigo())
                .nome(dados.nome())
                .cpf(dados.cpf())
                .build();
        return converteParaDTO(clientesRepository.save(cliente));
    }

    //atualiza nome e cpf de um cliente cadastrado (o código não muda)
    @Transactional
    public ClienteDTO atualizaCliente(String codigo, ClienteDTO dados) {
        Cliente cliente = buscaEntidade(codigo);
        validaCliente(dados);

        if (!dados.cpf().equals(cliente.getCpf()) && clientesRepository.existsByCpf(dados.cpf())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Já existe um cliente com esse CPF");
        }
        cliente.setNome(dados.nome());
        cliente.setCpf(dados.cpf());
        return converteParaDTO(clientesRepository.save(cliente));
    }

    //remove o cliente pelo codigo
    @Transactional
    public void deletaCliente(String codigo) {
        Cliente cliente = buscaEntidade(codigo);
        try {
            clientesRepository.delete(cliente);
            clientesRepository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não é possível excluir um cliente que possui notas fiscais.", ex);
        }
    }

    private ClienteDTO converteParaDTO(Cliente cliente) {
        return new ClienteDTO(cliente.getCodigo(), cliente.getNome(), cliente.getCpf());
    }

    private Cliente buscaEntidade(String codigo) {
        return clientesRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado!\nCódigo: " + codigo));
    }

    //nome e CPF são obrigatórios
    private void validaCliente(ClienteDTO cliente) {
        if (cliente == null || cliente.nome() == null || cliente.nome().isBlank()
                || cliente.cpf() == null || cliente.cpf().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe nome e CPF do cliente");
        }
    }
}
