package pe.edu.cibertec.cybercomputer.inventory;

import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.cibertec.cybercomputer.inventory.model.Categoria;
import pe.edu.cibertec.cybercomputer.inventory.model.Producto;
import pe.edu.cibertec.cybercomputer.inventory.repository.CategoriaRepository;
import pe.edu.cibertec.cybercomputer.inventory.repository.ProductoRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cybercomputer-inventory-integration;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InventoryIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ProductoRepository productos;
    @Autowired CategoriaRepository categorias;
    @Value("${app.jwt.secret-base64}") String secret;
    private Categoria categoria;
    private String admin;
    private String cliente;
    private static final String API = "/api/v1/productos";

    @BeforeEach
    void preparar() {
        categoria = new Categoria();
        categoria.setNombre("Teclados");
        categoria = categorias.saveAndFlush(categoria);
        admin = token("admin", "ROLE_ADMIN", "cybercomputer", secret, 1800);
        cliente = token("cliente", "ROLE_CLIENTE", "cybercomputer", secret, 1800);
    }

    private String token(String user, String role, String issuer, String key, long seconds) {
        Instant now = Instant.now();
        return Jwts.builder().issuer(issuer).subject(user).claim("idusuario", 1)
                .claim("authorities", List.of(role)).issuedAt(Date.from(now.minusSeconds(60)))
                .expiration(Date.from(now.plusSeconds(seconds)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(key)), Jwts.SIG.HS256).compact();
    }

    private String body(String nombre, String precio, int stock, Integer idcategoria) {
        return """
                {"nombre":"%s","descripcion":"Descripcion de prueba","precio":%s,
                 "stock":%d,"imagen":"/assets/teclado.jpg","idcategoria":%d}
                """.formatted(nombre, precio, stock, idcategoria);
    }

    private String valido() {
        return body("Teclado USB", "89.90", 5, categoria.getIdcategoria());
    }

    private Producto guardar() {
        var p = new Producto();
        p.setNombre("Producto guardado");
        p.setPrecio(new BigDecimal("89.90"));
        p.setStock(5);
        p.setCategoria(categoria);
        return productos.saveAndFlush(p);
    }

    @Test
    void adminListaCategorias() throws Exception {
        mvc.perform(get("/api/v1/categorias").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk()).andExpect(jsonPath("$.response", hasSize(1)))
                .andExpect(jsonPath("$.response[0].nombre").value("Teclados"));
    }

    @Test
    void clienteListaCategorias() throws Exception {
        mvc.perform(get("/api/v1/categorias").header("Authorization", "Bearer " + cliente))
                .andExpect(status().isOk()).andExpect(jsonPath("$.response[0].idcategoria")
                        .value(categoria.getIdcategoria()));
    }

    @Test
    void adminRegistraProductoCon201YLoPersiste() throws Exception {
        String response = mvc.perform(post(API).header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content(valido()))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.response.nombre").value("Teclado USB"))
                .andExpect(jsonPath("$.response.precio").value(89.9))
                .andExpect(jsonPath("$.response.stock").value(5))
                .andExpect(jsonPath("$.response.categoria.nombre").value("Teclados"))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.response.idproducto");
        assertThat(productos.findById(id.intValue())).isPresent();
    }

    @Test
    void clienteListaProductos() throws Exception {
        guardar();
        mvc.perform(get(API).header("Authorization", "Bearer " + cliente))
                .andExpect(status().isOk()).andExpect(jsonPath("$.response", hasSize(1)))
                .andExpect(jsonPath("$.response[0].nombre").value("Producto guardado"));
    }

    @Test
    void clienteConsultaUnProducto() throws Exception {
        var p = guardar();
        mvc.perform(get(API + "/" + p.getIdproducto()).header("Authorization", "Bearer " + cliente))
                .andExpect(status().isOk()).andExpect(jsonPath("$.response.idproducto").value(p.getIdproducto()));
    }

    @Test
    void adminActualizaProducto() throws Exception {
        var p = guardar();
        mvc.perform(put(API + "/" + p.getIdproducto()).header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Producto actualizado", "99.50", 8, categoria.getIdcategoria())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.response.nombre").value("Producto actualizado"))
                .andExpect(jsonPath("$.response.precio").value(99.5)).andExpect(jsonPath("$.response.stock").value(8));
        assertThat(productos.findById(p.getIdproducto()).orElseThrow().getPrecio()).isEqualByComparingTo("99.50");
    }

    @Test
    void adminEliminaProductoCon204() throws Exception {
        var p = guardar();
        var id = p.getIdproducto();
        mvc.perform(delete(API + "/" + id).header("Authorization", "Bearer " + admin))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        assertThat(productos.findById(id)).isEmpty();
    }

    @Test
    void productoInexistenteDevuelve404() throws Exception {
        mvc.perform(get(API + "/2147483647").header("Authorization", "Bearer " + cliente))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error.message").value("El producto no existe"));
    }

    @Test
    void nombreVacioDevuelve400() throws Exception {
        mvc.perform(post(API).header("Authorization", "Bearer " + admin).contentType(MediaType.APPLICATION_JSON)
                        .content(body("   ", "89.90", 5, categoria.getIdcategoria())))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.fields.nombre").exists());
        assertThat(productos.count()).isZero();
    }

    @Test
    void precioCeroDevuelve400() throws Exception {
        mvc.perform(post(API).header("Authorization", "Bearer " + admin).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Producto", "0", 5, categoria.getIdcategoria())))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.fields.precio").exists());
        assertThat(productos.count()).isZero();
    }

    @Test
    void precioConTresDecimalesDevuelve400() throws Exception {
        mvc.perform(post(API).header("Authorization", "Bearer " + admin).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Producto", "9.999", 5, categoria.getIdcategoria())))
                .andExpect(status().isBadRequest());
        assertThat(productos.count()).isZero();
    }

    @Test
    void precioFueraDeRangoDevuelve400() throws Exception {
        mvc.perform(post(API).header("Authorization", "Bearer " + admin).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Producto", "100000000.00", 5, categoria.getIdcategoria())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void stockNegativoDevuelve400() throws Exception {
        mvc.perform(post(API).header("Authorization", "Bearer " + admin).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Producto", "89.90", -1, categoria.getIdcategoria())))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.fields.stock").exists());
        assertThat(productos.count()).isZero();
    }

    @Test
    void stockFraccionarioDevuelve400() throws Exception {
        String json = valido().replace("\"stock\":5", "\"stock\":1.5");
        mvc.perform(post(API).header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        assertThat(productos.count()).isZero();
    }

    @Test
    void categoriaNoIndicadaDevuelve400() throws Exception {
        mvc.perform(post(API).header("Authorization", "Bearer " + admin).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Producto","precio":10,"stock":1}
                                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.fields.idcategoria").exists());
    }

    @Test
    void categoriaInexistenteDevuelve400() throws Exception {
        mvc.perform(post(API).header("Authorization", "Bearer " + admin).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Producto", "89.90", 5, Integer.MAX_VALUE)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.fields.idcategoria").exists());
        assertThat(productos.count()).isZero();
    }

    @Test
    void catalogoSinTokenEsPublico() throws Exception {
        mvc.perform(get(API)).andExpect(status().isOk());
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mvc.perform(get(API).header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenVencidoDevuelve401() throws Exception {
        String vencido = token("admin", "ROLE_ADMIN", "cybercomputer", secret, -30);
        mvc.perform(get(API).header("Authorization", "Bearer " + vencido)).andExpect(status().isUnauthorized());
    }

    @Test
    void emisorIncorrectoDevuelve401() throws Exception {
        String otro = token("admin", "ROLE_ADMIN", "otro-emisor", secret, 1800);
        mvc.perform(get(API).header("Authorization", "Bearer " + otro)).andExpect(status().isUnauthorized());
    }

    @Test
    void firmaIncorrectaDevuelve401() throws Exception {
        byte[] bytes = new byte[32];
        java.util.Arrays.fill(bytes, (byte) 42);
        String otraClave = java.util.Base64.getEncoder().encodeToString(bytes);
        String otro = token("admin", "ROLE_ADMIN", "cybercomputer", otraClave, 1800);
        mvc.perform(get(API).header("Authorization", "Bearer " + otro)).andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeRegistrar() throws Exception {
        mvc.perform(post(API).header("Authorization", "Bearer " + cliente).contentType(MediaType.APPLICATION_JSON)
                        .content(valido())).andExpect(status().isForbidden());
        assertThat(productos.count()).isZero();
    }

    @Test
    void clienteNoPuedeActualizar() throws Exception {
        var p = guardar();
        mvc.perform(put(API + "/" + p.getIdproducto()).header("Authorization", "Bearer " + cliente)
                        .contentType(MediaType.APPLICATION_JSON).content(valido())).andExpect(status().isForbidden());
        assertThat(productos.findById(p.getIdproducto()).orElseThrow().getNombre()).isEqualTo("Producto guardado");
    }

    @Test
    void clienteNoPuedeEliminar() throws Exception {
        var p = guardar();
        mvc.perform(delete(API + "/" + p.getIdproducto()).header("Authorization", "Bearer " + cliente))
                .andExpect(status().isForbidden());
        assertThat(productos.findById(p.getIdproducto())).isPresent();
    }

    @Test
    void jsonInvalidoDevuelve400() throws Exception {
        mvc.perform(post(API).header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{incompleto"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void actualizarInexistenteDevuelve404() throws Exception {
        mvc.perform(put(API + "/2147483647").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content(valido())).andExpect(status().isNotFound());
        assertThat(productos.count()).isZero();
    }

    @Test
    void eliminarInexistenteDevuelve404() throws Exception {
        mvc.perform(delete(API + "/2147483647").header("Authorization", "Bearer " + admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void identificadorNegativoDevuelve400() throws Exception {
        mvc.perform(get(API + "/-1").header("Authorization", "Bearer " + cliente))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminPuedeCrearCategorias() throws Exception {
        mvc.perform(post("/api/v1/categorias").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"nombre":"Nueva"}
                                """))
                .andExpect(status().isCreated());
        assertThat(categorias.count()).isEqualTo(2);
    }

    @Test
    void rolNoAutorizadoNoPuedeRegistrarProductos() throws Exception {
        String otro = token("auditor", "ROLE_AUDITOR", "cybercomputer", secret, 1800);
        mvc.perform(post(API).header("Authorization", "Bearer " + otro).contentType(MediaType.APPLICATION_JSON)
                        .content(valido())).andExpect(status().isForbidden());
    }

    @Test
    void tokenSinVencimientoDevuelve401() throws Exception {
        String sinExp = Jwts.builder().issuer("cybercomputer").subject("admin").claim("idusuario", 1)
                .claim("authorities", List.of("ROLE_ADMIN")).issuedAt(new Date())
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)), Jwts.SIG.HS256).compact();
        mvc.perform(get(API).header("Authorization", "Bearer " + sinExp)).andExpect(status().isUnauthorized());
    }

    @Test
    void visitanteConsultaCategoriasYDetalleDeProducto() throws Exception {
        var producto = guardar();
        mvc.perform(get("/api/v1/categorias")).andExpect(status().isOk());
        mvc.perform(get(API + "/" + producto.getIdproducto())).andExpect(status().isOk())
                .andExpect(jsonPath("$.response.nombre").value(producto.getNombre()));
    }

    @Test
    void categoriaNuevaPuedeAsignarseAUnProducto() throws Exception {
        String json = mvc.perform(post("/api/v1/categorias").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"nombre":"  Monitores  "}
                                """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.response.nombre").value("Monitores"))
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(json, "$.response.idcategoria");
        mvc.perform(post(API).header("Authorization", "Bearer " + admin).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Monitor LED", "499.90", 4, id)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.response.categoria.nombre").value("Monitores"));
    }

    @Test
    void categoriaDuplicadaDevuelve409() throws Exception {
        mvc.perform(post("/api/v1/categorias").header("Authorization", "Bearer " + admin)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"nombre":" teclados "}
                                """))
                .andExpect(status().isConflict());
        assertThat(categorias.count()).isEqualTo(1);
    }

    @Test
    void categoriasVaciasOLargasDevuelven400() throws Exception {
        for (String nombre : new String[]{"", "   ", "x".repeat(101)}) {
            mvc.perform(post("/api/v1/categorias").header("Authorization", "Bearer " + admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"" + nombre + "\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void soloAdminPuedeCrearCategorias() throws Exception {
        mvc.perform(post("/api/v1/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Monitores\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/categorias").header("Authorization", "Bearer " + cliente)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Monitores\"}"))
                .andExpect(status().isForbidden());
        assertThat(categorias.count()).isEqualTo(1);
    }
}
