package com.kaykvilela.notasFiscaisEstagio;

import com.kaykvilela.notasFiscaisEstagio.DTO.ClienteDTO;
import com.kaykvilela.notasFiscaisEstagio.DTO.ItemDTO;
import com.kaykvilela.notasFiscaisEstagio.DTO.NotaFiscalDTO;
import com.kaykvilela.notasFiscaisEstagio.DTO.ProdutoDTO;
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
import com.kaykvilela.notasFiscaisEstagio.service.ProdutoService;
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
    @Autowired ProdutoService produtoService;
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
        ana = clienteRepo.save(Cliente.builder().codigo("C1").nome("Ana").cpf("111").build());
        teclado = produtoRepo.save(new Produto(null, "P1", "Teclado", new BigDecimal("120.50")));
        mouse = produtoRepo.save(new Produto(null, "P2", "Mouse", new BigDecimal("50.00")));
    }

    // grava no banco e descarta o cache, forçando a próxima leitura a ir ao banco
    private void sincroniza() {
        em.flush();
        em.clear();
    }

    // só o que vale na requisição: código da nota, cliente e itens
    private NotaFiscalDTO requestCom(Integer codigoNota, String clienteCodigo, ItemDTO... itens) {
        return new NotaFiscalDTO(codigoNota, null, clienteCodigo, null, List.of(itens), null);
    }

    private ItemDTO pedido(Integer item, Produto produto, int quantidade) {
        return new ItemDTO(item, produto.getCodigo(), null, null, quantidade, null);
    }

    private HttpStatus statusDe(Runnable acao) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, acao::run);
        return HttpStatus.valueOf(ex.getStatusCode().value());
    }

    private BigDecimal valorTotalNoBanco(Integer codigoNota) {
        return notaRepo.findByCodigoNota(codigoNota).orElseThrow().getValorTotal();
    }

    @Test
    void notaGuardaClienteEItensEValorTotal() {
        NotaFiscalDTO criada = notaService.createNota(
                requestCom(100, "C1", pedido(1, teclado, 2), pedido(2, mouse, 3)));
        sincroniza();

        NotaFiscal nota = notaRepo.findByCodigoNota(100).orElseThrow();
        assertEquals(100, nota.getCodigoNota());
        assertEquals("C1", nota.getCliente().getCodigo());
        assertEquals(2, nota.getItensNota().size());
        for (Item item : nota.getItensNota()) {
            assertEquals(nota.getId(), item.getNota().getId());
        }

        // 2 x 120.50 + 3 x 50.00 = 391.00
        assertEquals(0, new BigDecimal("391.00").compareTo(nota.getValorTotal()));
        assertEquals(0, new BigDecimal("391.00").compareTo(criada.valorTotal()));
        assertEquals(100, criada.codigoNota());
        assertEquals("C1", criada.clienteCodigo());
        assertEquals("Ana", criada.clienteNome());
        assertEquals(2, criada.itens().size());
        assertNotNull(criada.data());
    }

    @Test
    void itemDaRespostaTrazDadosDoProduto() {
        NotaFiscalDTO criada = notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 2)));
        ItemDTO item = criada.itens().get(0);

        assertEquals(1, item.item());
        assertEquals("P1", item.produtoCodigo());
        assertEquals("Teclado", item.produtoNome());
        assertEquals(0, new BigDecimal("120.50").compareTo(item.valorUnitario()));
        assertEquals(2, item.quantidade());
        assertEquals(0, new BigDecimal("241.00").compareTo(item.valorTotal()));
    }

    @Test
    void camposCalculadosNaRequisicaoSaoIgnorados() {
        // quem manda valorTotal, nome e data na requisição não consegue alterá-los
        ItemDTO item = new ItemDTO(1, "P1", "Nome falso", new BigDecimal("1.00"), 2, new BigDecimal("1.00"));
        NotaFiscalDTO req = new NotaFiscalDTO(100, null, "C1", "Outro nome", List.of(item), new BigDecimal("1.00"));

        NotaFiscalDTO criada = notaService.createNota(req);

        assertEquals("Ana", criada.clienteNome());
        assertEquals("Teclado", criada.itens().get(0).produtoNome());
        assertEquals(0, new BigDecimal("241.00").compareTo(criada.valorTotal()));
    }

    @Test
    void itemAchadoPeloCodigoDaNotaESoDaquelaNota() {
        notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 1)));
        notaService.createNota(requestCom(200, "C1", pedido(2, mouse, 1), pedido(3, teclado, 4)));
        sincroniza();

        assertEquals(1, itemRepo.findByNotaCodigoNota(100).size());
        assertEquals(2, itemService.buscaItensDaNota(200).size());
    }

    @Test
    void buscarPeloCodigoEmCadaService() {
        notaService.createNota(requestCom(100, "C1", pedido(7, teclado, 1)));
        sincroniza();

        assertEquals("Ana", clienteService.buscarPeloCodigo("C1").nome());
        assertEquals("Mouse", produtoService.buscarPeloCodigo("P2").nome());
        assertEquals(100, notaService.buscarPeloCodigo(100).getCodigoNota());
        assertEquals("P1", itemService.buscarPeloCodigo(7).produtoCodigo());
        // o id interno não vale como código
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> clienteService.buscarPeloCodigo(String.valueOf(ana.getId() + 1000))));
    }

    @Test
    void mesmoProdutoPodeEstarEmVariosItensDeNotasDiferentes() {
        notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 1)));
        notaService.createNota(requestCom(200, "C1", pedido(2, teclado, 5)));
        sincroniza();

        long itensDoTeclado = itemRepo.findAll().stream()
                .filter(i -> i.getProduto().getCodigo().equals("P1")).count();
        assertEquals(2, itensDoTeclado);
    }

    @Test
    void valorUnitarioAcompanhaOProdutoEOTotalDaNotaFicaGravado() {
        // sem snapshot: o valor unitário vem do produto atual
        notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 1)));
        sincroniza();

        produtoService.atualizaProduto("P1", new ProdutoDTO("P1", "Teclado", new BigDecimal("999.00")));
        sincroniza();

        ItemDTO linha = notaService.devolveNota(100).itens().get(0);
        assertEquals(0, new BigDecimal("999.00").compareTo(linha.valorUnitario()));
        // o total do item e da nota foi gravado na criação
        assertEquals(0, new BigDecimal("120.50").compareTo(linha.valorTotal()));
        assertEquals(0, new BigDecimal("120.50").compareTo(notaService.devolveNota(100).valorTotal()));
    }

    @Test
    void getDaNotaDevolveANotaComSeusItens() {
        notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 2), pedido(2, mouse, 1)));
        sincroniza();

        NotaFiscalDTO nota = notaService.devolveNota(100);
        assertEquals(2, nota.itens().size());
        assertEquals(100, nota.codigoNota());
        assertEquals("Ana", nota.clienteNome());
        assertNotNull(nota.data());
        assertEquals(1, notaService.listarNotas().size());
    }

    @Test
    void excluirNotaApagaItensMasPreservaClienteEProduto() {
        notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 1), pedido(2, mouse, 1)));
        sincroniza();

        notaService.excluirNota(100);
        sincroniza();

        assertFalse(notaRepo.existsByCodigoNota(100));
        assertEquals(0, itemRepo.count());
        assertTrue(clienteRepo.existsByCodigo("C1"));
        assertEquals(2, produtoRepo.count());
    }

    @Test
    void atualizarNotaTrocaClienteESubstituiItensMesmoReusandoOCodigoDoItem() {
        clienteRepo.save(Cliente.builder().codigo("C2").nome("Bia").cpf("222").build());
        notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 1), pedido(2, mouse, 1)));
        sincroniza();

        NotaFiscalDTO atualizada = notaService.atualizarNota(100, requestCom(100, "C2", pedido(1, mouse, 7)));
        sincroniza();

        NotaFiscal nota = notaRepo.findByCodigoNota(100).orElseThrow();
        assertEquals("C2", nota.getCliente().getCodigo());
        assertEquals(1, nota.getItensNota().size());
        assertEquals(7, nota.getItensNota().get(0).getQuantidade());
        assertEquals(1, itemRepo.count()); // os antigos foram removidos do banco
        assertEquals("Bia", atualizada.clienteNome());
        assertEquals(0, new BigDecimal("350.00").compareTo(nota.getValorTotal()));
    }

    @Test
    void itemAvulso_adicionaAtualizaERemove() {
        notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 1)));
        sincroniza();

        ItemDTO add = itemService.addItem(100, pedido(2, mouse, 2));
        sincroniza();
        assertEquals(2, itemService.buscaItensDaNota(100).size());
        assertEquals(0, new BigDecimal("100.00").compareTo(add.valorTotal()));

        ItemDTO atualizado = itemService.atualizaQuantidade(2, new ItemDTO(null, null, null, null, 10, null));
        assertEquals(0, new BigDecimal("500.00").compareTo(atualizado.valorTotal()));

        itemService.deletaItem(2);
        sincroniza();
        assertEquals(1, itemService.buscaItensDaNota(100).size());
    }

    @Test
    void totalGravadoNaNotaAcompanhaAsOperacoesDeItem() {
        notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 1)));
        sincroniza();

        itemService.addItem(100, pedido(2, mouse, 2)); // +100.00
        sincroniza();
        assertEquals(0, new BigDecimal("220.50").compareTo(valorTotalNoBanco(100)));

        itemService.atualizaQuantidade(2, new ItemDTO(null, null, null, null, 4, null)); // 200.00
        sincroniza();
        assertEquals(0, new BigDecimal("320.50").compareTo(valorTotalNoBanco(100)));

        itemService.deletaItem(2);
        sincroniza();
        assertEquals(0, new BigDecimal("120.50").compareTo(valorTotalNoBanco(100)));
        assertEquals(0, new BigDecimal("120.50").compareTo(notaService.devolveNota(100).valorTotal()));
    }

    @Test
    void naoPermiteRemoverOUltimoItemDaNota() {
        notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 1)));
        sincroniza();

        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() -> itemService.deletaItem(1)));
    }

    @Test
    void codigosDuplicadosSaoRejeitados() {
        notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 1)));
        sincroniza();

        // nota
        assertEquals(HttpStatus.CONFLICT, statusDe(() ->
                notaService.createNota(requestCom(100, "C1", pedido(2, mouse, 1)))));
        // item já usado em outra nota
        assertEquals(HttpStatus.CONFLICT, statusDe(() ->
                notaService.createNota(requestCom(200, "C1", pedido(1, mouse, 1)))));
        assertEquals(HttpStatus.CONFLICT, statusDe(() -> itemService.addItem(100, pedido(1, mouse, 1))));
        // item repetido na mesma requisição
        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() ->
                notaService.createNota(requestCom(300, "C1", pedido(5, mouse, 1), pedido(5, teclado, 1)))));
        // cliente e produto
        assertEquals(HttpStatus.CONFLICT, statusDe(() ->
                clienteService.addCliente(new ClienteDTO("C1", "Outra", "999"))));
        assertEquals(HttpStatus.CONFLICT, statusDe(() ->
                produtoService.criaProduto(new ProdutoDTO("P1", "Outro", BigDecimal.ONE))));
    }

    @Test
    void erros_deNotaClienteProdutoEItem() {
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> notaService.buscarPeloCodigo(9999)));
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> itemService.buscaItensDaNota(9999)));
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> itemService.buscarPeloCodigo(9999)));
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> notaService.excluirNota(9999)));
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> clienteService.buscarPeloCodigo("X")));
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> produtoService.buscarPeloCodigo("X")));

        assertEquals(HttpStatus.NOT_FOUND, statusDe(() ->
                notaService.createNota(requestCom(100, "X", pedido(1, teclado, 1)))));
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() ->
                notaService.createNota(requestCom(100, "C1", new ItemDTO(1, "X", null, null, 1, null)))));

        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() -> notaService.createNota(null)));
        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() ->
                notaService.createNota(requestCom(100, null, pedido(1, teclado, 1)))));
        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() ->
                notaService.createNota(requestCom(null, "C1", pedido(1, teclado, 1)))));
        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() ->
                notaService.createNota(requestCom(100, "C1", pedido(null, teclado, 1)))));
        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() ->
                notaService.createNota(requestCom(100, "C1"))));
        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() ->
                notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 0)))));
        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() ->
                notaService.createNota(requestCom(100, "C1", pedido(1, teclado, -3)))));
    }

    @Test
    void erroNaCriacaoNaoDeixaNotaPelaMetade() {
        long antes = notaRepo.count();
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> notaService.createNota(
                requestCom(100, "C1", pedido(1, teclado, 1), new ItemDTO(2, "X", null, null, 1, null)))));
        assertEquals(antes, notaRepo.count());
    }

    @Test
    void clienteComNotaNaoPodeSerExcluido() {
        notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 1)));
        sincroniza();
        assertEquals(HttpStatus.CONFLICT, statusDe(() -> clienteService.deletaCliente("C1")));
    }

    @Test
    void produtoEmNotaNaoPodeSerExcluido() {
        notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 1)));
        sincroniza();
        assertEquals(HttpStatus.CONFLICT, statusDe(() -> produtoService.deletaProduto("P1")));
    }

    @Test
    void crudDeClienteEProdutoPeloCodigo() {
        ClienteDTO criado = clienteService.addCliente(new ClienteDTO("C9", "Caio", "999"));
        assertEquals("C9", criado.codigo());
        clienteService.atualizaCliente("C9", new ClienteDTO("IGNORADO", "Caio Novo", "999"));
        assertEquals("Caio Novo", clienteService.buscarPeloCodigo("C9").nome());
        assertEquals("C9", clienteService.buscarPeloCodigo("C9").codigo()); // o código não muda
        clienteService.deletaCliente("C9");
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> clienteService.buscarPeloCodigo("C9")));

        produtoService.criaProduto(new ProdutoDTO("P9", "Cabo", new BigDecimal("9.90")));
        produtoService.atualizaProduto("P9", new ProdutoDTO("IGNORADO", "Cabo HDMI", new BigDecimal("19.90")));
        assertEquals("Cabo HDMI", produtoService.buscarPeloCodigo("P9").nome());
        produtoService.deletaProduto("P9");
        assertEquals(HttpStatus.NOT_FOUND, statusDe(() -> produtoService.buscarPeloCodigo("P9")));

        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() ->
                clienteService.addCliente(new ClienteDTO(null, "Sem código", "888"))));
        assertEquals(HttpStatus.BAD_REQUEST, statusDe(() ->
                produtoService.criaProduto(new ProdutoDTO(null, "Sem código", BigDecimal.ONE))));
    }

    @Test
    void cpfDuplicadoNaAtualizacaoDeCliente() {
        clienteRepo.save(Cliente.builder().codigo("C2").nome("Bia").cpf("222").build());
        sincroniza();
        assertEquals(HttpStatus.CONFLICT, statusDe(() ->
                clienteService.atualizaCliente("C2", new ClienteDTO(null, "Bia", "111"))));

        // manter o próprio CPF é permitido
        assertEquals("Bia Nova",
                clienteService.atualizaCliente("C2", new ClienteDTO(null, "Bia Nova", "222")).nome());
    }

    @Test
    void renomearClienteAparecenaNota() {
        notaService.createNota(requestCom(100, "C1", pedido(1, teclado, 1)));
        sincroniza();

        clienteService.atualizaCliente("C1", new ClienteDTO(null, "Ana Maria", "111"));
        sincroniza();

        assertEquals("Ana Maria", notaService.devolveNota(100).clienteNome());
    }
}
