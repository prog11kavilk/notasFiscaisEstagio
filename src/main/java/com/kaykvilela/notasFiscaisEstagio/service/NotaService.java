package com.kaykvilela.notasFiscaisEstagio.service;

import com.kaykvilela.notasFiscaisEstagio.DTO.ItemDTO;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor

public class NotaService {

    private final INotaRepository notaFiscalRepo;
    private final IClienteRepository clienteRepo;
    private final ItemService itemService;

    @Transactional(readOnly = true)
    public List<NotaFiscal> buscaNotas() {
        return notaFiscalRepo.findAllByOrderByCodigoNota();
    }

    @Transactional(readOnly = true)
    public NotaFiscal buscarPeloCodigo(Integer codigoNota) {
        return notaFiscalRepo.findByCodigoNota(codigoNota)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Nota não encontrada!\nCódigo: " + codigoNota));
    }

    @Transactional(readOnly = true)
    public List<NotaFiscalDTO> listarNotas() {
        return converteEmListaResponse(notaFiscalRepo.findAllByOrderByCodigoNota());
    }

    @Transactional(readOnly = true)
    public NotaFiscalDTO devolveNota(Integer codigoNota) {
        return converteEmNotaResponse(buscarPeloCodigo(codigoNota));
    }

    @Transactional(readOnly = true)
    public List<NotaFiscalDTO> buscarNotaPeloCodigoCliente(String codigoCliente) {
        buscaCliente(codigoCliente);
        return converteEmListaResponse(notaFiscalRepo.findByClienteCodigoOrderByCodigoNota(codigoCliente));
    }

    //devolve as notas dos clientes com o nome que possui o texto informado (sem diferenciar maiúsculas)
    @Transactional(readOnly = true)
    public List<NotaFiscalDTO> buscarNotaPeloNomeCliente(String nomeCliente) {
        return converteEmListaResponse(notaFiscalRepo.findByClienteNomeContainingIgnoreCaseOrderByCodigoNota(nomeCliente));
    }

    @Transactional
    public NotaFiscalDTO createNota(NotaFiscalDTO request) {
        validaNotaFiscal(request);
        if (request.codigoNota() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código da nota");
        }
        if (notaFiscalRepo.existsByCodigoNota(request.codigoNota())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma nota com esse código");
        }
        NotaFiscal nota = new NotaFiscal();
        nota.setCodigoNota(request.codigoNota());
        nota.setData(LocalDateTime.now());
        nota.setCliente(buscaCliente(request.clienteCodigo()));
        preencheItens(nota, request.itens());
        atualizaTotais(nota);
        return converteEmNotaResponse(notaFiscalRepo.save(nota));
    }

    //troca os dados do cliente e substitui todos os itens da nota
    @Transactional
    public NotaFiscalDTO atualizarNota(Integer codigoNota, NotaFiscalDTO request) {
        NotaFiscal nota = buscarPeloCodigo(codigoNota);
        validaNotaFiscal(request);
        nota.setCliente(buscaCliente(request.clienteCodigo()));
        nota.getItensNota().clear();
        notaFiscalRepo.flush(); // apaga antes de inserir, senão o código dos itens novos trava com os antigos
        preencheItens(nota, request.itens());
        atualizaTotais(nota);
        return converteEmNotaResponse(notaFiscalRepo.saveAndFlush(nota));
    }

    @Transactional
    public void excluirNota(Integer codigoNota) {
        notaFiscalRepo.delete(buscarPeloCodigo(codigoNota));
    }

    private List<NotaFiscalDTO> converteEmListaResponse(List<NotaFiscal> notas) {
        List<NotaFiscalDTO> resposta = new ArrayList<>();
        for (NotaFiscal nota : notas) {
            resposta.add(converteEmNotaResponse(nota));
        }
        return resposta;
    }

    public NotaFiscalDTO converteEmNotaResponse(NotaFiscal nota) {
        List<ItemDTO> itens = new ArrayList<>();
        for (Item item : nota.getItensNota()) {
            itens.add(itemService.converteParaItemResponse(item));
        }
        return new NotaFiscalDTO(
                nota.getCodigoNota(),
                nota.getData(),
                nota.getCliente().getCodigo(),
                nota.getCliente().getNome(),
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

    private Cliente buscaCliente(String codigo) {
        return clienteRepo.findByCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado!\nCódigo: " + codigo));
    }

    private void preencheItens(NotaFiscal nota, List<ItemDTO> itensRequest) {
        List<Item> itens  = nota.getItensNota();
        Set<Integer> codigos = new HashSet<>();
        for (ItemDTO dto : itensRequest) {
            if (!codigos.add(dto.item())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de item repetido na nota: " + dto.item());
            }
            itens.add(itemService.montaItem(nota, dto.item(), dto.produtoCodigo(), dto.quantidade()));
        }
        nota.setItensNota(itens);
    }

    // refaz o valor total da somando o valor dos itens presentes nela
    public void atualizaTotais(NotaFiscal nota) {
        BigDecimal total = BigDecimal.ZERO;
        for (Item item : nota.getItensNota()) {
            total = total.add(item.getSubtotal());
        }
        nota.setValorTotal(total);
    }
}
