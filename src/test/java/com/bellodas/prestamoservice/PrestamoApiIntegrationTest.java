package com.bellodas.prestamoservice;

import com.bellodas.prestamoservice.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PrestamoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Test
    void sinTokenConTokenAlteradoOVencidoDevuelve401() throws Exception {
        mockMvc.perform(get("/prestamos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(get("/prestamos")
                        .header("Authorization", "Bearer tokenAlterado"))
                .andExpect(status().isUnauthorized());

        String tokenVencido = Jwts.builder()
                .subject("a@test.com")
                .issuedAt(Date.from(Instant.now().minusSeconds(3600)))
                .expiration(Date.from(Instant.now().minusSeconds(1800)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret)))
                .compact();

        mockMvc.perform(get("/prestamos")
                        .header("Authorization", "Bearer " + tokenVencido))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registroLoginYCrudAislanLaCarteraPorPrestamista() throws Exception {
        String tokenA = registrar("Prestamista A", "a@test.com");
        String tokenB = registrar("Prestamista B", "b@test.com");

        assertThat(usuarioRepository.findByEmail("a@test.com")).isPresent();
        assertThat(usuarioRepository.findByEmail("a@test.com").orElseThrow().getPassword())
                .startsWith("$2");

        String login = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "a@test.com", "password", "clave123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(login).get("token").asText()).isNotBlank();

        String respuestaCrear = mockMvc.perform(post("/prestamos")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prestamoJson("1000.00", "10.00", false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cliente").value("Juan Perez"))
                .andExpect(jsonPath("$.monto").value(1000.00))
                .andExpect(jsonPath("$.interes").value(10.00))
                .andExpect(jsonPath("$.total").value(1100.00))
                .andExpect(jsonPath("$.pagado").value(false))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(respuestaCrear).get("id").asLong();

        mockMvc.perform(get("/prestamos").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/prestamos").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/prestamos/" + id).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/prestamos/" + id)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(prestamoJson("1000.00", "10.00", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagado").value(true));

        mockMvc.perform(get("/prestamos/resumen").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capitalPrestado").value(1000.00))
                .andExpect(jsonPath("$.totalPorCobrar").value(0.00))
                .andExpect(jsonPath("$.gananciaEsperada").value(0.00))
                .andExpect(jsonPath("$.totalPrestamos").value(1));

        mockMvc.perform(delete("/prestamos/" + id).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/prestamos/" + id).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/prestamos").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void datosInvalidosDevuelven400YEmailDuplicado409() throws Exception {
        registrar("Unico", "unico@test.com");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", "Otro", "email", "unico@test.com", "password", "clave123"))))
                .andExpect(status().isConflict());

        String token = login("unico@test.com");
        mockMvc.perform(post("/prestamos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "cliente", "Ana", "dni", "123", "monto", new BigDecimal("10.00"),
                                "interes", new BigDecimal("5.00"), "fechaVencimiento", "2026-12-31"))))
                .andExpect(status().isBadRequest());
    }

    private String registrar(String nombre, String email) throws Exception {
        String respuesta = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", nombre, "email", email, "password", "clave123"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta).get("token").asText();
    }

    private String login(String email) throws Exception {
        String respuesta = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email, "password", "clave123"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta).get("token").asText();
    }

    private String prestamoJson(String monto, String interes, boolean pagado) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "cliente", "Juan Perez",
                "dni", "12345678",
                "monto", new BigDecimal(monto),
                "interes", new BigDecimal(interes),
                "fechaVencimiento", "2026-12-31",
                "pagado", pagado
        ));
    }
}
