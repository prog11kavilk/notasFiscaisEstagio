package com.kaykvilela.notasFiscaisEstagio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// sobe a aplicação inteira (H2) e chama os endpoints reais pelo código
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:nota-http-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class EndpointsTests {

    @Autowired MockMvc mvc;

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder b, String corpo) {
        return b.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    @Test
    void fluxoCompletoPeloCodigo() throws Exception {
        mvc.perform(json(post("/nf/clientes/create"), "{\"codigo\":\"H1\",\"nome\":\"Hugo\",\"cpf\":\"500\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("H1"))
                .andExpect(jsonPath("$.id").doesNotExist());
        mvc.perform(json(post("/nf/produtos/create"), "{\"codigo\":\"HP1\",\"nome\":\"Cadeira\",\"valor\":300.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("HP1"));

        mvc.perform(get("/nf/clientes/H1")).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Hugo"));
        mvc.perform(get("/nf/produtos/HP1")).andExpect(status().isOk()).andExpect(jsonPath("$.valor").value(300.00));
        mvc.perform(get("/nf/clientes/NAO")).andExpect(status().isNotFound());

        mvc.perform(json(post("/nf/nota_fiscal/create"),
                        "{\"codigoNota\":900,\"clienteCodigo\":\"H1\",\"itens\":[{\"item\":9001,\"produtoCodigo\":\"HP1\",\"quantidade\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoNota").value(900))
                .andExpect(jsonPath("$.clienteNome").value("Hugo"))
                .andExpect(jsonPath("$.valorTotal").value(600.00))
                .andExpect(jsonPath("$.itens[0].item").value(9001))
                .andExpect(jsonPath("$.itens[0].produtoNome").value("Cadeira"));

        mvc.perform(get("/nf/nota_fiscal/900"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoNota").value(900))
                .andExpect(jsonPath("$.clienteNome").value("Hugo"))
                .andExpect(jsonPath("$.itens[0].item").value(9001))
                .andExpect(jsonPath("$.valorTotal").value(600.00));
        mvc.perform(get("/nf/nota_fiscal")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].itens[0].item").value(9001));
        mvc.perform(get("/nf/nota_fiscal/12345")).andExpect(status().isNotFound());
        mvc.perform(get("/nf/nota_fiscal/cliente/codigo/H1")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoNota").value(900))
                .andExpect(jsonPath("$[0].itens[0].item").value(9001));
        mvc.perform(get("/nf/nota_fiscal/cliente/codigo/NAO")).andExpect(status().isNotFound());
        mvc.perform(get("/nf/nota_fiscal/cliente/nome/hug")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteNome").value("Hugo"));
        mvc.perform(get("/nf/nota_fiscal/cliente/nome/zzz")).andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        mvc.perform(get("/nf/itens/item/9001")).andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoCodigo").value("HP1"));
        mvc.perform(get("/nf/itens/nota/900")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].item").value(9001));

        mvc.perform(json(post("/nf/itens/create/900"), "{\"item\":9002,\"produtoCodigo\":\"HP1\",\"quantidade\":1}"))
                .andExpect(status().isCreated());
        mvc.perform(json(put("/nf/itens/update/9002"), "{\"quantidade\":3}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valorTotal").value(900.00));
        mvc.perform(delete("/nf/itens/delete/9002")).andExpect(status().isNoContent());

        mvc.perform(json(put("/nf/nota_fiscal/update/900"),
                        "{\"clienteCodigo\":\"H1\",\"itens\":[{\"item\":9001,\"produtoCodigo\":\"HP1\",\"quantidade\":1}]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valorTotal").value(300.00));

        mvc.perform(delete("/nf/clientes/delete/H1")).andExpect(status().isConflict());
        mvc.perform(delete("/nf/nota_fiscal/delete/900")).andExpect(status().isNoContent());
        mvc.perform(delete("/nf/clientes/delete/H1")).andExpect(status().isNoContent());
        mvc.perform(delete("/nf/produtos/delete/HP1")).andExpect(status().isNoContent());
    }
}
