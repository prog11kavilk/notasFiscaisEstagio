package com.kaykvilela.notasFiscaisEstagio;

import com.kaykvilela.notasFiscaisEstagio.DTO.ItemDTORequest;
import com.kaykvilela.notasFiscaisEstagio.DTO.ItemDTOResposta;
import com.kaykvilela.notasFiscaisEstagio.DTO.NotaItemResposta;
import com.kaykvilela.notasFiscaisEstagio.DTO.NotaFiscalDTO;
import com.kaykvilela.notasFiscaisEstagio.DTO.NotaFiscalResposta;
import com.kaykvilela.notasFiscaisEstagio.model.Cliente;
import com.kaykvilela.notasFiscaisEstagio.model.Item;
import com.kaykvilela.notasFiscaisEstagio.model.NotaFiscal;
import com.kaykvilela.notasFiscaisEstagio.model.Produto;
import com.kaykvilela.notasFiscaisEstagio.repository.IClienteRepository;
import com.kaykvilela.notasFiscaisEstagio.repository.INotaRepository;
import com.kaykvilela.notasFiscaisEstagio.repository.IProdutoRepository;
import com.kaykvilela.notasFiscaisEstagio.repository.ItemRepository;
import com.kaykvilela.notasFiscaisEstagio.service.ClienteService;
import com.kaykvilela.notasFiscaisEstagio.service.ItemService;
import com.kaykvilela.notasFiscaisEstagio.service.NotaService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// cada teste roda numa transação que é desfeita no final
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:nota-rel-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class NotaFiscalRelacionamentoTests {

    @Autowired NotaService notaService;
    @Autowired ItemService itemService;
    @Autowired ClienteService clienteService;
    @Autowired IClienteRepository clienteRepo;
    @Autowired IProdutoRepository produtoRepo;
    @Autowired INotaRepository notaRepo;
    @Autowired ItemRepository itemRepo;
    @Autowired EntityManager em;

    Cliente ana;
    Produto teclado;
    Produto mouse;

    @BeforeEach
    void dadosBase() {
        ana = clienteRepo.save(Cliente.builder().nome("Ana").cpf("111").build());
        teclado = produtoRepo.save(new Produto(null, "Teclado", new BigDecimal("120.50")));
        mouse = produtoRepo.save(new Produto(null, "Mouse", new BigDecimal("50.00")));
    }

    // grava no banco e descarta o cache, forçando a próxima leitura a ir ao banco
    private void sincroniza() {
        em.flush();
        em.clear();
    }

    private NotaFiscalDTO requestCom(Integer clienteCodigo, ItemDTORequest... itens) {
        return new NotaFiscalDTO(clienteCodigo, List.of(itens));
    }

    private ItemDTORequest pedido(Produto produto, int quantidade) {
        return new ItemDTORequest(produto.getCodigo(), quantidade);
    }

    private HttpStatus statusDe(Runnable acao) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, acao::run);
        return HttpStatus.valueOf(ex.getStatusCode().value());
    }

    @Test
    void notaGuardaClienteEItensEValorTotal() {
        NotaFiscalResposta criada = notaService.createNota(
                requestCom(ana.getCodigo(), pedido(teclado, 2), pedido(mouse, 3)));
        sincroniza();

        NotaFiscal nota = notaRepo.findById(criada.numeroNota()).orElseThrow();
        assertEquals(ana.getCodigo(), nota.getCliente().getCodigo());
        assertEquals(2, nota.getItensNota().size());
        for (Item item : nota.getItensNota()) {
            assertEquals(nota.getNumeroNota(), item.getNota().getNumeroNota());
        }

        // cliente_nome e valor_total ficam gravados na própria nota
        assertEquals("Ana", nota.getClienteNome());
        assertEquals(0, new BigDecimal("391.00").compareTo(nota.getValorTotal()));

        // 2 x 120.50 + 3 x 50.00 = 391.00
        assertEquals(0, new BigDecimal("391.00").compareTo(criada.valorTotal()));
        assertEquals("Ana", criada.clienteNome());
        assertEquals(2, criada.itens().size());
        assertNotNull(criada.data());
    }

    @Test
    void itemAchadoPeloNumeroDaNotaESoDaquelaNota() {
        NotaFiscalResposta n1 = notaService.createNota(requestCom(ana.getCodigo(), pedido(teclado, 1)));
        NotaFiscalResposta n2 = notaService.createNota(
                requestCom(ana.getCodigo(), pedido(mouse, 1), pedido(teclado, 4)));
        sincroniza();

        assertEquals(1, itemRepo.findByNotaNumeroNota(n1.numeroNota()).size());
        assertEquals(2, itemService.buscaItensDaNota(n2.numeroNota()).size());
    }

    @Test
    void mesmoProdutoPodeEstarEmVariosItensDeNotasDiferentes() {
        notaService.createNota(requestCom(ana.getCodigo(), pedido(teclado, 1)));
        notaService.createNota(requestCom(ana.getCodigo(), pedido(teclado, 5)));
        sincroniza();

        long itensDoTeclado = itemRepo.findAll().stream()
                .filter(i -> i.getProduto().getCodigo().equals(teclado.getCodigo())).count();
        assertEquals(2, itensDoTeclado);
    }

    @Test
    void valorUnitarioFicaCongeladoQuandoProdutoMuda() {
        NotaFiscalResposta criada = notaService.createNota(requestCom(ana.getCodigo(), pedido(teclado, 1)));
        sincroniza();

        Produto p = produtoRepo.findById(teclado.getCodigo()).orElseThrow();
        p.setValor(new BigDecimal("999.00"));
        sincroniza();

        NotaItemResposta item = notaService.devolveNota(criada.numeroNota()).get(0);
        assertEquals(0, new BigDecimal("120.50").compareTo(item.valorUnitario()));
    }

    @Test
    void excluirNotaApagaItensMasPreservaClienteEProduto() {
        NotaFiscalResposta criada = notaService.createNota(
                requestCom(ana.getCodigo(), pedido(teclado, 1), pedido(mouse, 1)));
        sincroniza();

        notaService.excluirNota(criada.numeroNota());
        sincroniza();

        assertFalse(notaRepo.existsById(criada.numeroNota()));
        assertEquals(0, itemRepo.count());
        assertTrue(clienteRepo.existsById(ana.getCodigo()));
        assertEquals(2, produtoRepo.count());
    }

    @Test
    void atualizarNotaTrocaClienteESubstituiItens() {
        Cliente bia = clienteRepo.save(Cliente.builder().nome("Bia").cpf("222").build());
        NotaFiscalResposta criada = notaService.createNota(
                requestCom(ana.getCodigo(), pedido(teclado, 1), pedido(mouse, 1)));
        sincroniza();

        notaService.atualizarNota(criada.numeroNota(), requestCom(bia.getCodigo(), pedido(mouse, 7)));
        sincroniza();

        NotaFiscal nota = notaRepo.findById(criada.numeroNota()).orElseThrow();
        assertEquals(bia.getCodigo(), nota.getCliente().getCodigo());
        assertEquals(1, nota.getItensNota().size());
        assertEquals(7, nota.getItensNota().get(0).getQuantidade());
        assertEquals(1, itemRepo.count()); // os antigos foram removidos do banco
    }

    @Test
    void itemAvulso_adicionaAtualizaERemove() {
        NotaFiscalResposta criada = notaService.createNota(requestCom(ana.getCodigo(), pedido(teclado, 1)));
        sincroniza();

        ItemDTORequest novo = pedido(mouse, 2);
        ItemDTOResposta add = itemService.addItem(criada.numeroNota(), novo);
        sincroniza();
        assertEquals(2, itemService.buscaItensDaNota(criada.numeroNota()).size());
        assertEquals(0, new BigDecimal("100.00").compareTo(add.valorTotal()));

        Item item = itemRepo.findByNotaNumeroNota(criada.numeroNota()).stream()
                .filter(i -> i.getProduto().getCodigo().equals(mouse.getCodigo())).findFirst().orElseThrow();
        ItemDTORequest mudanca = new ItemDTORequest(null, 10);
        ItemDTOResposta atualizado = itemService.atualizaQuantidade(item.getCodigo(), mudanca);
        assertEquals(0, new BigDecimal("500.00").compareTo(atualizado.valorTotal()));

        itemService.deletaItem(item.getCodigo());
        sincroniza();
        assertEquals(1, itemService.buscaItensDaNota(criada.numeroNota()).size());
    }

    @Test
    void naoPermiteRemoverOUltimoItemDaNota() {
        NotaFiscalResposta criada = notaService.createNota(requestCom(ana.getCodigo(), pedido(teclado, 1)));
        sincroniza();
        Integer idItem = itemRepo.findByNotaNumeroNota(criada.numeroNota()).get(0).getCodigo();

        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() -> itemService.deletaItem(idItem)));
    }

    @Test
    void erros_deNotaClienteProdutoEItem() {
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> notaService.buscaNota(9999)));
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> itemService.buscaItensDaNota(9999)));
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> itemService.buscaItem(9999)));
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> notaService.excluirNota(9999)));

        assertEquals(HttpStatus.NOT_FOUND, statusDe(() ->
                notaService.createNota(requestCom(9999, pedido(teclado, 1)))));
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() ->
                notaService.createNota(requestCom(ana.getCodigo(), new ItemDTORequest(9999, 1)))));

        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() -> notaService.createNota(null)));
        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() ->
                notaService.createNota(requestCom(null, pedido(teclado, 1)))));
        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() ->
                notaService.createNota(requestCom(ana.getCodigo()))));
        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() ->
                notaService.createNota(requestCom(ana.getCodigo(), pedido(teclado, 0)))));
        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() ->
                notaService.createNota(requestCom(ana.getCodigo(), pedido(teclado, -3)))));
    }

    @Test
    void erroNaCriacaoNaoDeixaNotaPela_metade() {
        long antes = notaRepo.count();
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> notaService.createNota(
                requestCom(ana.getCodigo(), pedido(teclado, 1), new ItemDTORequest(9999, 1)))));
        assertEquals(antes, notaRepo.count());
    }

    @Test
    void clienteComNotaNaoPodeSerExcluido() {
        notaService.createNota(requestCom(ana.getCodigo(), pedido(teclado, 1)));
        sincroniza();
        assertEquals(HttpStatus.CONFLICT, statusDe(() -> clienteService.deletaCliente(ana.getCodigo())));
    }

    @Test
    void cpfDuplicadoNaAtualizacaoDeCliente() {
        Cliente bia = clienteRepo.save(Cliente.builder().nome("Bia").cpf("222").build());
        sincroniza();
        Cliente dados = Cliente.builder().nome("Bia").cpf("111").build();
        assertEquals(HttpStatus.CONFLICT, statusDe(() -> clienteService.atualizaCliente(bia.getCodigo(), dados)));

        // manter o próprio CPF é permitido
        Cliente igual = Cliente.builder().nome("Bia Nova").cpf("222").build();
        assertEquals("Bia Nova", clienteService.atualizaCliente(bia.getCodigo(), igual).getNome());
    }

    @Test
    void totalGravadoNaNotaAcompanhaAsOperacoesDeItem() {
        NotaFiscalResposta criada = notaService.createNota(requestCom(ana.getCodigo(), pedido(teclado, 1)));
        sincroniza();
        Integer numero = criada.numeroNota();

        ItemDTORequest novo = pedido(mouse, 2); // +100.00
        itemService.addItem(numero, novo);
        sincroniza();
        assertEquals(0, new BigDecimal("220.50").compareTo(notaRepo.findById(numero).orElseThrow().getValorTotal()));

        Item mouseItem = itemRepo.findByNotaNumeroNota(numero).stream()
                .filter(i -> i.getProduto().getCodigo().equals(mouse.getCodigo())).findFirst().orElseThrow();
        ItemDTORequest mudanca = new ItemDTORequest(null, 4); // 200.00
        itemService.atualizaQuantidade(mouseItem.getCodigo(), mudanca);
        sincroniza();
        assertEquals(0, new BigDecimal("320.50").compareTo(notaRepo.findById(numero).orElseThrow().getValorTotal()));

        itemService.deletaItem(mouseItem.getCodigo());
        sincroniza();
        assertEquals(0, new BigDecimal("120.50").compareTo(notaRepo.findById(numero).orElseThrow().getValorTotal()));
        assertEquals(0, new BigDecimal("120.50").compareTo(notaService.devolveNota(numero).get(0).valorTotal()));
    }

    @Test
    void atualizarNotaRecalculaClienteNomeEValorTotal() {
        Cliente bia = clienteRepo.save(Cliente.builder().nome("Bia").cpf("222").build());
        NotaFiscalResposta criada = notaService.createNota(requestCom(ana.getCodigo(), pedido(teclado, 1)));
        sincroniza();

        notaService.atualizarNota(criada.numeroNota(), requestCom(bia.getCodigo(), pedido(mouse, 2)));
        sincroniza();

        NotaFiscal nota = notaRepo.findById(criada.numeroNota()).orElseThrow();
        assertEquals("Bia", nota.getClienteNome());
        assertEquals(0, new BigDecimal("100.00").compareTo(nota.getValorTotal()));
    }

    @Test
    void renomearClienteAtualizaNomeGravadoNasNotas() {
        NotaFiscalResposta criada = notaService.createNota(requestCom(ana.getCodigo(), pedido(teclado, 1)));
        sincroniza();

        clienteService.atualizaCliente(ana.getCodigo(), Cliente.builder().nome("Ana Maria").cpf("111").build());
        sincroniza();

        assertEquals("Ana Maria", notaRepo.findById(criada.numeroNota()).orElseThrow().getClienteNome());
    }
}
