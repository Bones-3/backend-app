package com.nativa.reserva_service.service;

import com.nativa.reserva_service.dto.EspacioRequest;
import com.nativa.reserva_service.dto.EspacioResponse;
import com.nativa.reserva_service.exception.ResourceNotFoundException;
import com.nativa.reserva_service.mapper.EspacioMapper;
import com.nativa.reserva_service.model.Espacio;
import com.nativa.reserva_service.model.TipoEspacio;
import com.nativa.reserva_service.repository.EspacioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EspacioServiceTest {
    @Mock private EspacioRepository repository;
    @Mock private EspacioMapper mapper;
    @InjectMocks private EspacioService service;

    @Test
    void getActivos_shouldReturnCanchaAndSalon() {
        Espacio cancha = new Espacio(1L, "Cancha", TipoEspacio.CANCHA, true);
        Espacio salon = new Espacio(2L, "Salón", TipoEspacio.SALON, true);
        when(repository.findByActivoTrue()).thenReturn(List.of(cancha, salon));
        when(mapper.toResponse(cancha)).thenReturn(EspacioResponse.builder().id(1L).nombre("Cancha").build());
        when(mapper.toResponse(salon)).thenReturn(EspacioResponse.builder().id(2L).nombre("Salón").build());

        assertThat(service.getActivos()).extracting(EspacioResponse::getNombre)
                .containsExactly("Cancha", "Salón");
    }

    @Test
    void create_shouldPersistSpace() {
        EspacioRequest request = new EspacioRequest();
        request.setNombre("Cancha");
        request.setTipo(TipoEspacio.CANCHA);
        Espacio entity = new Espacio(null, "Cancha", TipoEspacio.CANCHA, true);
        Espacio saved = new Espacio(1L, "Cancha", TipoEspacio.CANCHA, true);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(EspacioResponse.builder().id(1L).nombre("Cancha").build());

        assertThat(service.create(request).getId()).isEqualTo(1L);
    }

    @Test
    void getById_shouldThrowWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Espacio no encontrado");
    }
}
