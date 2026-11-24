package com.nativa.vecino_service.service;

import com.nativa.vecino_service.dto.VecinoRequest;
import com.nativa.vecino_service.dto.VecinoResponse;
import com.nativa.vecino_service.exception.ResourceNotFoundException;
import com.nativa.vecino_service.mapper.VecinoMapper;
import com.nativa.vecino_service.model.Vecino;
import com.nativa.vecino_service.repository.VecinoRepository;

import org.junit.jupiter.api.BeforeEach;
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
class VecinoServiceTest {

    @Mock
    private VecinoRepository vecinoRepository;

    @Mock
    private VecinoMapper vecinoMapper;

    @InjectMocks
    private VecinoService vecinoService;

    private Vecino vecino;
    private VecinoResponse vecinoResponse;
    private VecinoRequest vecinoRequest;

    @BeforeEach
    void setUp() {
        vecino = new Vecino();
        vecino.setId(1L);
        vecino.setCorreoVecino("test@test.com");
        vecino.setNombres("Juan");
        vecino.setApellidos("Perez");
        vecino.setTelefonoVecino("123456789");
        vecino.setEstadoVecino(true);

        vecinoResponse = VecinoResponse.builder()
                .id(1L)
                .correoVecino("test@test.com")
                .nombre("Juan")
                .apellido("Perez")
                .telefonoVecino("123456789")
                .estadoVecino(true)
                .build();

        vecinoRequest = new VecinoRequest();
        vecinoRequest.setCorreoVecino("test@test.com");
        vecinoRequest.setNombres("Juan");
        vecinoRequest.setApellidos("Perez");
        vecinoRequest.setTelefonoVecino("123456789");
    }

    @Test
    void getVecinosActivo_shouldReturnList() {
        Vecino otroVecino = new Vecino();
        otroVecino.setId(2L);
        otroVecino.setEstadoVecino(true);
        when(vecinoRepository.findByEstadoVecinoTrue()).thenReturn(List.of(vecino, otroVecino));
        when(vecinoMapper.toResponse(vecino)).thenReturn(vecinoResponse);

        VecinoResponse otroResponse = VecinoResponse.builder().id(2L).estadoVecino(true).build();
        when(vecinoMapper.toResponse(otroVecino)).thenReturn(otroResponse);

        List<VecinoResponse> result = vecinoService.getVecinosActivo();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        verify(vecinoRepository).findByEstadoVecinoTrue();
        verify(vecinoMapper).toResponse(vecino);
        verify(vecinoMapper).toResponse(otroVecino);
    }

    @Test
    void getVecinosActivo_shouldReturnEmptyList() {
        when(vecinoRepository.findByEstadoVecinoTrue()).thenReturn(List.of());

        List<VecinoResponse> result = vecinoService.getVecinosActivo();

        assertThat(result).isEmpty();
        verify(vecinoRepository).findByEstadoVecinoTrue();
        verifyNoInteractions(vecinoMapper);
    }

    @Test
    void getAllVecinos_shouldReturnList() {
        Vecino otroVecino = new Vecino();
        otroVecino.setId(2L);
        when(vecinoRepository.findAll()).thenReturn(List.of(vecino, otroVecino));
        when(vecinoMapper.toResponse(vecino)).thenReturn(vecinoResponse);

        VecinoResponse otroResponse = VecinoResponse.builder().id(2L).build();
        when(vecinoMapper.toResponse(otroVecino)).thenReturn(otroResponse);

        List<VecinoResponse> result = vecinoService.getAllVecinos();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        verify(vecinoRepository).findAll();
        verify(vecinoMapper).toResponse(vecino);
        verify(vecinoMapper).toResponse(otroVecino);
    }

    @Test
    void getAllVecinos_shouldReturnEmptyList() {
        when(vecinoRepository.findAll()).thenReturn(List.of());

        List<VecinoResponse> result = vecinoService.getAllVecinos();

        assertThat(result).isEmpty();
        verify(vecinoRepository).findAll();
        verifyNoInteractions(vecinoMapper);
    }

    @Test
    void getVecinoById_shouldReturnUser() {
        when(vecinoRepository.findById(1L)).thenReturn(Optional.of(vecino));
        when(vecinoMapper.toResponse(vecino)).thenReturn(vecinoResponse);

        VecinoResponse result = vecinoService.getVecinoById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCorreoVecino()).isEqualTo("test@test.com");
        assertThat(result.getNombre()).isEqualTo("Juan");
        assertThat(result.getApellido()).isEqualTo("Perez");
        verify(vecinoRepository).findById(1L);
        verify(vecinoMapper).toResponse(vecino);
    }

    @Test
    void getVecinoById_shouldThrowNotFound() {
        when(vecinoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vecinoService.getVecinoById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vecino no encontrado");

        verify(vecinoRepository).findById(99L);
        verifyNoInteractions(vecinoMapper);
    }

    @Test
    void createVecino_shouldReturnSavedUser() {
        Vecino entityToSave = new Vecino();
        entityToSave.setCorreoVecino("test@test.com");
        entityToSave.setNombres("Juan");
        entityToSave.setApellidos("Perez");
        entityToSave.setTelefonoVecino("123456789");
        entityToSave.setEstadoVecino(true);

        when(vecinoMapper.toEntity(vecinoRequest)).thenReturn(entityToSave);

        Vecino savedEntity = new Vecino();
        savedEntity.setId(1L);
        savedEntity.setCorreoVecino("test@test.com");
        savedEntity.setNombres("Juan");
        savedEntity.setApellidos("Perez");
        savedEntity.setTelefonoVecino("123456789");
        savedEntity.setEstadoVecino(true);

        when(vecinoRepository.save(entityToSave)).thenReturn(savedEntity);
        when(vecinoMapper.toResponse(savedEntity)).thenReturn(vecinoResponse);

        VecinoResponse result = vecinoService.createVecino(vecinoRequest);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCorreoVecino()).isEqualTo("test@test.com");
        verify(vecinoMapper).toEntity(vecinoRequest);
        verify(vecinoRepository).save(entityToSave);
        verify(vecinoMapper).toResponse(savedEntity);
    }

    @Test
    void updateVecino_shouldReturnUpdatedUser() {
        VecinoRequest updateRequest = new VecinoRequest();
        updateRequest.setCorreoVecino("updated@test.com");
        updateRequest.setNombres("Maria");
        updateRequest.setApellidos("Gomez");
        updateRequest.setTelefonoVecino("987654321");

        when(vecinoRepository.findById(1L)).thenReturn(Optional.of(vecino));

        Vecino savedEntity = new Vecino();
        savedEntity.setId(1L);
        savedEntity.setCorreoVecino("updated@test.com");
        savedEntity.setNombres("Maria");
        savedEntity.setApellidos("Gomez");
        savedEntity.setTelefonoVecino("987654321");
        savedEntity.setEstadoVecino(true);

        when(vecinoRepository.save(vecino)).thenReturn(savedEntity);

        VecinoResponse updatedResponse = VecinoResponse.builder()
                .id(1L).correoVecino("updated@test.com").nombre("Maria")
                .apellido("Gomez").telefonoVecino("987654321").estadoVecino(true)
                .build();
        when(vecinoMapper.toResponse(savedEntity)).thenReturn(updatedResponse);

        VecinoResponse result = vecinoService.updateVecino(1L, updateRequest);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCorreoVecino()).isEqualTo("updated@test.com");
        assertThat(result.getNombre()).isEqualTo("Maria");
        assertThat(result.getApellido()).isEqualTo("Gomez");
        verify(vecinoRepository).findById(1L);
        verify(vecinoRepository).save(vecino);
        verify(vecinoMapper).toResponse(savedEntity);
    }

    @Test
    void updateVecino_shouldThrowNotFound() {
        when(vecinoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vecinoService.updateVecino(99L, vecinoRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vecino no encontrado");

        verify(vecinoRepository).findById(99L);
        verify(vecinoRepository, never()).save(any());
        verifyNoInteractions(vecinoMapper);
    }

    @Test
    void deleteVecino_shouldSetEstadoFalse() {
        when(vecinoRepository.findById(1L)).thenReturn(Optional.of(vecino));

        vecinoService.deleteVecino(1L);

        assertThat(vecino.getEstadoVecino()).isFalse();
        verify(vecinoRepository).findById(1L);
        verify(vecinoRepository).save(vecino);
    }

    @Test
    void deleteVecino_shouldThrowNotFound() {
        when(vecinoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vecinoService.deleteVecino(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vecino no encontrado");

        verify(vecinoRepository).findById(99L);
        verify(vecinoRepository, never()).save(any());
    }

    @Test
    void createVecino_shouldSaveVecinoWithCorreoValido() {
        // Given
        VecinoRequest request = new VecinoRequest();
        request.setCorreoVecino("cliente@nativa.com");
        request.setNombres("Juan");
        request.setApellidos("Pérez");

        Vecino vecinoEntity = new Vecino();
        vecinoEntity.setCorreoVecino("cliente@nativa.com");
        when(vecinoMapper.toEntity(request)).thenReturn(vecinoEntity);

        Vecino vecinoGuardado = new Vecino();
        vecinoGuardado.setId(1L);
        vecinoGuardado.setCorreoVecino("cliente@nativa.com");
        when(vecinoRepository.save(vecinoEntity)).thenReturn(vecinoGuardado);

        VecinoResponse response = VecinoResponse.builder()
                .id(1L)
                .correoVecino("cliente@nativa.com")
                .build();
        when(vecinoMapper.toResponse(vecinoGuardado)).thenReturn(response);

        // When
        VecinoResponse result = vecinoService.createVecino(request);

        // Then
        assertThat(result.getCorreoVecino()).isEqualTo("cliente@nativa.com");
        assertThat(result.getCorreoVecino()).contains("@");
        verify(vecinoRepository).save(vecinoEntity);
    }
}

