package pe.edu.cibertec.cybercomputer.inventory;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.cibertec.cybercomputer.inventory.dto.*;
import pe.edu.cibertec.cybercomputer.inventory.exception.StockException;
import pe.edu.cibertec.cybercomputer.inventory.model.*;
import pe.edu.cibertec.cybercomputer.inventory.repository.*;
import pe.edu.cibertec.cybercomputer.inventory.service.StockService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:cybercomputer-stock-integration;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
    "spring.datasource.password=", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StockIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ProductoRepository productos;
    @Autowired CategoriaRepository categorias;
    @Autowired StockService stock;
    @Value("${app.jwt.secret-base64}") String secret;
    private Producto uno, dos;
    private String interno, cliente, admin;
    private static final String API = "/api/v1/internal/stock";

    @BeforeEach
    void preparar() {
        productos.deleteAll();
        categorias.deleteAll();
        var c = new Categoria(); c.setNombre("Prueba stock"); c = categorias.saveAndFlush(c);
        uno = producto("Teclado", "89.90", 5, c);
        dos = producto("Mouse", "35.50", 2, c);
        interno = token("order-service", "ROLE_ORDER_SERVICE");
        cliente = token("cliente", "ROLE_CLIENTE");
        admin = token("admin", "ROLE_ADMIN");
    }

    private Producto producto(String nombre, String precio, int cantidad, Categoria categoria) {
        var p = new Producto(); p.setNombre(nombre); p.setPrecio(new BigDecimal(precio));
        p.setStock(cantidad); p.setCategoria(categoria); return productos.saveAndFlush(p);
    }

    private String token(String user, String role) {
        Instant now = Instant.now();
        return "Bearer " + Jwts.builder().issuer("cybercomputer").subject(user).claim("idusuario", 2)
                .claim("authorities", List.of(role)).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(600)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)), Jwts.SIG.HS256).compact();
    }

    private String body(int cantidadUno, int cantidadDos) {
        return "{\"items\":[{\"idproducto\":%d,\"cantidad\":%d},{\"idproducto\":%d,\"cantidad\":%d}]}"
                .formatted(uno.getIdproducto(), cantidadUno, dos.getIdproducto(), cantidadDos);
    }
    private int cantidad(Producto p) { return productos.findById(p.getIdproducto()).orElseThrow().getStock(); }
    private void estado(int a, int b) { assertThat(cantidad(uno)).isEqualTo(a); assertThat(cantidad(dos)).isEqualTo(b); }

    @Test
    void descuentaTodosYDevuelvePreciosReales() throws Exception {
        mvc.perform(post(API + "/descontar").header("Authorization", interno)
                .contentType(MediaType.APPLICATION_JSON).content(body(2, 1)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.response[0].precio").value(89.9))
                .andExpect(jsonPath("$.response[0].cantidad").value(2))
                .andExpect(jsonPath("$.response[0].stock").value(3));
        estado(3, 1);
    }

    @Test
    void insuficienteNoDescuentaElPrimerProducto() throws Exception {
        mvc.perform(post(API + "/descontar").header("Authorization", interno)
                .contentType(MediaType.APPLICATION_JSON).content(body(2, 3))).andExpect(status().isConflict());
        estado(5, 2);
    }

    @Test
    void productoInexistenteNoDescuentaNinguno() throws Exception {
        String b = "{\"items\":[{\"idproducto\":%d,\"cantidad\":1},{\"idproducto\":2147483647,\"cantidad\":1}]}".formatted(uno.getIdproducto());
        mvc.perform(post(API + "/descontar").header("Authorization", interno)
                .contentType(MediaType.APPLICATION_JSON).content(b)).andExpect(status().isNotFound());
        estado(5, 2);
    }

    @Test
    void reponerRecuperaCantidades() throws Exception {
        mvc.perform(post(API + "/descontar").header("Authorization", interno)
                .contentType(MediaType.APPLICATION_JSON).content(body(2, 1))).andExpect(status().isOk());
        mvc.perform(post(API + "/reponer").header("Authorization", interno)
                .contentType(MediaType.APPLICATION_JSON).content(body(2, 1))).andExpect(status().isNoContent());
        estado(5, 2);
    }

    @Test
    void clienteNoPuedeDescontar() throws Exception {
        mvc.perform(post(API + "/descontar").header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON).content(body(1, 1))).andExpect(status().isForbidden());
        estado(5, 2);
    }

    @Test
    void clienteNoPuedeReponer() throws Exception {
        mvc.perform(post(API + "/reponer").header("Authorization", cliente)
                .contentType(MediaType.APPLICATION_JSON).content(body(1, 1))).andExpect(status().isForbidden());
        estado(5, 2);
    }

    @Test
    void adminNoPuedeDescontar() throws Exception {
        mvc.perform(post(API + "/descontar").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(body(1, 1))).andExpect(status().isForbidden());
        estado(5, 2);
    }

    @Test
    void adminNoPuedeReponer() throws Exception {
        mvc.perform(post(API + "/reponer").header("Authorization", admin)
                .contentType(MediaType.APPLICATION_JSON).content(body(1, 1))).andExpect(status().isForbidden());
        estado(5, 2);
    }

    @Test
    void sinTokenEsRechazado() throws Exception {
        mvc.perform(post(API + "/descontar").contentType(MediaType.APPLICATION_JSON).content(body(1, 1)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenInvalidoEsRechazado() throws Exception {
        mvc.perform(post(API + "/descontar").header("Authorization", "Bearer invalido")
                .contentType(MediaType.APPLICATION_JSON).content(body(1, 1))).andExpect(status().isUnauthorized());
    }

    @Test
    void cantidadCero() throws Exception {
        mvc.perform(post(API + "/descontar").header("Authorization", interno)
                .contentType(MediaType.APPLICATION_JSON).content(body(0, 1))).andExpect(status().isBadRequest());
        estado(5, 2);
    }

    @Test
    void cantidadNegativa() throws Exception {
        mvc.perform(post(API + "/descontar").header("Authorization", interno)
                .contentType(MediaType.APPLICATION_JSON).content(body(-1, 1))).andExpect(status().isBadRequest());
        estado(5, 2);
    }

    @Test
    void cantidadExcesiva() throws Exception {
        mvc.perform(post(API + "/descontar").header("Authorization", interno)
                .contentType(MediaType.APPLICATION_JSON).content(body(10000, 1))).andExpect(status().isBadRequest());
        estado(5, 2);
    }

    @Test
    void carritoVacio() throws Exception {
        mvc.perform(post(API + "/descontar").header("Authorization", interno)
                .contentType(MediaType.APPLICATION_JSON).content("{\"items\":[]}")).andExpect(status().isBadRequest());
        estado(5, 2);
    }

    @Test
    void itemNulo() throws Exception {
        mvc.perform(post(API + "/descontar").header("Authorization", interno)
                .contentType(MediaType.APPLICATION_JSON).content("{\"items\":[null]}")).andExpect(status().isBadRequest());
        estado(5, 2);
    }

    @Test
    void duplicadosRechazados() throws Exception {
        String b = "{\"items\":[{\"idproducto\":%d,\"cantidad\":1},{\"idproducto\":%d,\"cantidad\":1}]}"
                .formatted(uno.getIdproducto(), uno.getIdproducto());
        mvc.perform(post(API + "/descontar").header("Authorization", interno)
                .contentType(MediaType.APPLICATION_JSON).content(b)).andExpect(status().isBadRequest());
        estado(5, 2);
    }

    @Test
    void compraExactaPermiteStockCero() throws Exception {
        mvc.perform(post(API + "/descontar").header("Authorization", interno)
                .contentType(MediaType.APPLICATION_JSON).content(body(5, 2))).andExpect(status().isOk());
        estado(0, 0);
    }

    @Test
    void desbordamientoDeReposicionNoCambiaNinguno() throws Exception {
        dos.setStock(Integer.MAX_VALUE); productos.saveAndFlush(dos);
        mvc.perform(post(API + "/reponer").header("Authorization", interno)
                .contentType(MediaType.APPLICATION_JSON).content(body(1, 1))).andExpect(status().isConflict());
        estado(5, Integer.MAX_VALUE);
    }

    @Test
    void tokenInternoNoPuedeAdministrarCatalogo() throws Exception {
        mvc.perform(delete("/api/v1/productos/" + uno.getIdproducto()).header("Authorization", interno))
                .andExpect(status().isForbidden());
        estado(5, 2);
    }

    @Test
    void dosComprasConcurrentesNoSobrevenden() throws Exception {
        var request = new CompraRequest(List.of(new ItemCompraRequest(uno.getIdproducto(), 4)));
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> compra = () -> {
                start.await(5, TimeUnit.SECONDS);
                try { stock.descontar(request); return true; }
                catch (StockException ex) { if (ex.getStatus() == 409) return false; throw ex; }
            };
            var a = pool.submit(compra); var b = pool.submit(compra); start.countDown();
            int correctas = (a.get(10, TimeUnit.SECONDS) ? 1 : 0) + (b.get(10, TimeUnit.SECONDS) ? 1 : 0);
            assertThat(correctas).isEqualTo(1);
            estado(1, 2);
        } finally { pool.shutdownNow(); }
    }
}
