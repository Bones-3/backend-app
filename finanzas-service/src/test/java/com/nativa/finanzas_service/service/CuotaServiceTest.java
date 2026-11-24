package com.nativa.finanzas_service.service;

import com.nativa.finanzas_service.client.VecinoClient;
import com.nativa.finanzas_service.client.dto.VecinoResponse;
import com.nativa.finanzas_service.dto.*;
import com.nativa.finanzas_service.model.*;
import com.nativa.finanzas_service.repository.CuotaRepository;
import com.nativa.finanzas_service.repository.CuotaVecinoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuotaServiceTest {
    @Mock private CuotaRepository cuotaRepository;
    @Mock private CuotaVecinoRepository cuotaVecinoRepository;
    @Mock private VecinoClient vecinoClient;
    @InjectMocks private CuotaService service;

    private Cuota cuota() {
        Cuota cuota = new Cuota();
        cuota.setId(1L);
        cuota.setNombre("Cuota enero");
        cuota.setTipo(TipoCuota.ORDINARIA);
        cuota.setMonto(new BigDecimal("15000"));
        cuota.setFechaVencimiento(LocalDate.now().plusDays(10));
        cuota.setActiva(true);
        return cuota;
    }

    @Test
    void crear_shouldPersistOrdinaryFee() {
        CuotaRequest request = new CuotaRequest();
        request.setNombre("Cuota enero");
        request.setTipo(TipoCuota.ORDINARIA);
        request.setMonto(new BigDecimal("15000"));
        request.setFechaVencimiento(LocalDate.now().plusDays(10));
        when(cuotaRepository.save(any())).thenAnswer(inv -> {
            Cuota c = inv.getArgument(0); c.setId(1L); return c;
        });
        assertThat(service.crear(request).getTipo()).isEqualTo(TipoCuota.ORDINARIA);
    }

    @Test
    void asignar_shouldCreateNeighborObligation() {
        Cuota cuota = cuota();
        when(cuotaRepository.findById(1L)).thenReturn(Optional.of(cuota));
        VecinoResponse vecino = new VecinoResponse();
        vecino.setId(5L);
        when(vecinoClient.obtenerPorId(5L)).thenReturn(vecino);
        when(cuotaVecinoRepository.existsByCuotaIdAndVecinoId(1L, 5L)).thenReturn(false);
        when(cuotaVecinoRepository.save(any())).thenAnswer(inv -> {
            CuotaVecino cv = inv.getArgument(0); cv.setId(10L); return cv;
        });
        AsignacionCuotaRequest request = new AsignacionCuotaRequest();
        request.setVecinoIds(List.of(5L));

        List<CuotaVecinoResponse> result = service.asignar(1L, request);

        assertThat(result).singleElement().satisfies(r -> {
            assertThat(r.getVecinoId()).isEqualTo(5L);
            assertThat(r.getEstado()).isEqualTo(EstadoCuotaVecino.PENDIENTE);
        });
    }
}
