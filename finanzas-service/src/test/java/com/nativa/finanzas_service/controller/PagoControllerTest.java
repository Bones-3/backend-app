package com.nativa.finanzas_service.controller;

import com.nativa.finanzas_service.dto.PagoResponse;
import com.nativa.finanzas_service.security.JwtUtil;
import com.nativa.finanzas_service.service.PagoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagoController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "TESORERO")
class PagoControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private PagoService pagoService;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    void buscar_shouldReturnPage() throws Exception {
        PagoResponse pago = PagoResponse.builder().id(1L).cuotaVecinoId(10L)
                .vecino_id(5L).total(new BigDecimal("15000")).build();
        when(pagoService.buscar(isNull(), isNull(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(pago)));
        mockMvc.perform(get("/finanzas/pagos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].cuotaVecinoId").value(10))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }
}
