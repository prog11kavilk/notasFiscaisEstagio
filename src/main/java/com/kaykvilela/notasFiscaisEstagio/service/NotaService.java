package com.kaykvilela.notasFiscaisEstagio.service;

import com.kaykvilela.notasFiscaisEstagio.DTO.ItemDTORequest;
import com.kaykvilela.notasFiscaisEstagio.DTO.ItemDTOResposta;
import com.kaykvilela.notasFiscaisEstagio.DTO.NotaFiscalResposta;
import com.kaykvilela.notasFiscaisEstagio.DTO.NotaItemResposta;
import com.kaykvilela.notasFiscaisEstagio.DTO.NotaFiscalDTO;
import com.kaykvilela.notasFiscaisEstagio.model.Cliente;
import com.kaykvilela.notasFiscaisEstagio.model.Item;
import com.kaykvilela.notasFiscaisEstagio.model.NotaFiscal;
import com.kaykvilela.notasFiscaisEstagio.repository.IClienteRepository;
import com.kaykvilela.notasFiscaisEstagio.repository.INotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

//regra de negócio da entidade 'notaFiscal'
public class NotaService {

    //injeção dos objetos repository - acesso do banco
    private final INotaRepository notaFiscalRepo;
    private final IClienteRepository clienteRepo;
    private final ItemService itemService;

    @Transactional(readOnly = true)
    public List<NotaFiscal> buscaNotas() {
        return notaFiscalRepo.findAll();
    }

    @Transactional(readOnly = true)
    public NotaFiscal buscaNota(Integer codigo) {
        return notaFiscalRepo.findById(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Nota não encontrada!\nID: " + codigo));
    }

    //lista todos os itens de todas as notas, uma linha por item
    @Transactional(readOnly = true)
    public List<NotaItemResposta> listarNotas() {
        List<NotaItemResposta> resposta = new ArrayList<>();
        for (NotaFiscal nota : notaFiscalRepo.findAll()) {
            resposta.addAll(converteEmLinhasDeItens(nota));
        }
        return resposta;
    }

    //devolve os itens de uma nota, uma linha por item
    @Transactional(readOnly = true)
    public List<NotaItemResposta> devolveNota(Integer numeroNota) {
        return converteEmLinhasDeItens(buscaNota(numeroNota));
    }

    //cria a nota com o cliente e os itens informados
    @Transactional
    public NotaFiscalResposta createNota(NotaFiscalDTO request) {
        validaNotaFiscal(request);
        NotaFiscal nota = new NotaFiscal();
        nota.setData(LocalDateTime.now());
        nota.setCliente(buscaCliente(request.clienteCodigo()));
        preencheItens(nota, request.itens());
        nota.atualizaTotais();
        return converteEmNotaResponse(notaFiscalRepo.save(nota));
    }

    //troca o cliente e substitui todos os itens da nota
    @Transactional
    public NotaFiscalResposta atualizarNota(Integer numeroNota, NotaFiscalDTO request) {
        NotaFiscal nota = buscaNota(numeroNota);
        validaNotaFiscal(request);
        nota.setCliente(buscaCliente(request.clienteCodigo()));
        nota.getItensNota().clear(); // orphanRemoval apaga os antigos
        preencheItens(nota, request.itens());
        nota.atualizaTotais();
        return converteEmNotaResponse(notaFiscalRepo.saveAndFlush(nota));
    }

    //apaga a nota e, em cascata, os itens
    @Transactional
    public void excluirNota(Integer numeroNota) {
        notaFiscalRepo.delete(buscaNota(numeroNota));
    }

    // uma linha por item: numero da nota, código do item, cliente_nome, produto, valor unitário, quantidade e valor total
    private List<NotaItemResposta> converteEmLinhasDeItens(NotaFiscal nota) {
        List<NotaItemResposta> linhas = new ArrayList<>();
        for (Item item : nota.getItensNota()) {
            linhas.add(new NotaItemResposta(
                    nota.getNumeroNota(),
                    item.getCodigo(),
                    nota.getClienteNome(),
                    item.getProdutoNome(),
                    item.getValorUnitario(),
                    item.getQuantidade(),
                    item.getValorTotal()));
        }
        return linhas;
    }

    // converte a NotaFiscal entidade em NotaFiscalDTO (cliente_nome e valor_total na nota)
    public NotaFiscalResposta converteEmNotaResponse(NotaFiscal nota) {
        List<ItemDTOResposta> itens = new ArrayList<>();
        for (Item item : nota.getItensNota()) {
            itens.add(itemService.converteParaItemResponse(item));
        }
        return new NotaFiscalResposta(
                nota.getNumeroNota(),
                nota.getData(),
//                nota.getCliente().getCodigo(),
                nota.getClienteNome(),
                itens,
                nota.getValorTotal());
    }

    private void validaNotaFiscal(NotaFiscalDTO notaFiscalDTO) {
        if (notaFiscalDTO == null || notaFiscalDTO.clienteCodigo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código do cliente da nota");
        }
        if (notaFiscalDTO.itens() == null || notaFiscalDTO.itens().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A nota precisa ter pelo menos 1 item");
        }
    }

    private Cliente buscaCliente(Integer codigo) {
        return clienteRepo.findById(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado!\nID:" + codigo));
    }

    private void preencheItens(NotaFiscal nota, List<ItemDTORequest> itensRequest) {
        List<Item> itens  = nota.getItensNota();
        for (ItemDTORequest dto : itensRequest) {
            itens.add( itemService.montaItem(nota, dto.produtoCodigo(), dto.quantidade()));
        }
        nota.setItensNota(itens);
    }
}
