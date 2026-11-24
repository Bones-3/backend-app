package com.nativa.finanzas_service.service;

import com.nativa.finanzas_service.dto.PagoRequest;
import com.nativa.finanzas_service.dto.PagoResponse;
import com.nativa.finanzas_service.exception.BadRequestException;
import com.nativa.finanzas_service.mapper.PagoMapper;
import com.nativa.finanzas_service.model.*;
import com.nativa.finanzas_service.repository.CuotaVecinoRepository;
import com.nativa.finanzas_service.repository.MovimientoRepository;
import com.nativa.finanzas_service.repository.PagoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {
    @Mock private PagoRepository pagoRepository;
    @Mock private CuotaVecinoRepository cuotaVecinoRepository;
    @Mock private MovimientoRepository movimientoRepository;
    @Mock private PagoMapper pagoMapper;
    @InjectMocks private PagoService service;

    private CuotaVecino asignacion;
    private PagoRequest request;

    @BeforeEach
    void setUp() {
        Cuota cuota = new Cuota();
        cuota.setId(1L);
        cuota.setNombre("Cuota enero");
        cuota.setMonto(new BigDecimal("100.00"));
        cuota.setFechaVencimiento(LocalDate.now().plusDays(5));
        asignacion = new CuotaVecino();
        asignacion.setId(10L);
        asignacion.setCuota(cuota);
        asignacion.setVecinoId(5L);
        asignacion.setMontoPagado(BigDecimal.ZERO);
        asignacion.setEstado(EstadoCuotaVecino.PENDIENTE);
        request = new PagoRequest();
        request.setCuotaVecinoId(10L);
        request.setMetodoPago("TRANSFERENCIA");
    }

    @Test
    void createPago_shouldPayFullOutstandingBalance() {
        when(cuotaVecinoRepository.findById(10L)).thenReturn(Optional.of(asignacion));
        when(pagoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pagoMapper.toResponse(any())).thenReturn(
                PagoResponse.builder().id(1L).total(new BigDecimal("100.00")).build());

        PagoResponse response = service.createPago(request);

        assertThat(response.getTotal()).isEqualByComparingTo("100.00");
        assertThat(asignacion.getEstado()).isEqualTo(EstadoCuotaVecino.PAGADA);
        verify(movimientoRepository).save(argThat(m -> m.getTipo() == TipoMovimiento.INGRESO));
    }

    @Test
    void createPago_shouldAllowPartialPayment() {
        request.setMonto(new BigDecimal("40.00"));
        when(cuotaVecinoRepository.findById(10L)).thenReturn(Optional.of(asignacion));
        when(pagoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(pagoMapper.toResponse(any())).thenReturn(
                PagoResponse.builder().total(new BigDecimal("40.00")).build());

        service.createPago(request);

        assertThat(asignacion.getMontoPagado()).isEqualByComparingTo("40.00");
        assertThat(asignacion.getEstado()).isEqualTo(EstadoCuotaVecino.PENDIENTE);
    }

    @Test
    void createPago_shouldRejectAmountAboveBalance() {
        request.setMonto(new BigDecimal("101.00"));
        when(cuotaVecinoRepository.findById(10L)).thenReturn(Optional.of(asignacion));
        assertThatThrownBy(() -> service.createPago(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El monto supera el saldo pendiente");
        verifyNoInteractions(pagoRepository, movimientoRepository);
    }
}
