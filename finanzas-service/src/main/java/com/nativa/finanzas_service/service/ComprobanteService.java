package com.nativa.finanzas_service.service;

import com.nativa.finanzas_service.dto.ComprobanteRequest;
import com.nativa.finanzas_service.dto.ComprobanteResponse;
import com.nativa.finanzas_service.exception.BadRequestException;
import com.nativa.finanzas_service.exception.ResourceNotFoundException;
import com.nativa.finanzas_service.model.Comprobante;
import com.nativa.finanzas_service.repository.ComprobanteRepository;
import com.nativa.finanzas_service.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ComprobanteService {
    private final ComprobanteRepository repository;
    private final PagoRepository pagoRepository;

    @Transactional
    public ComprobanteResponse crear(ComprobanteRequest request) {
        if (repository.existsByPagoId(request.getPagoId())) {
            throw new BadRequestException("El pago ya tiene comprobante");
        }
        var pago = pagoRepository.findById(request.getPagoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));
        Comprobante comprobante = new Comprobante();
        comprobante.setPago(pago);
        comprobante.setNombreArchivo(request.getNombreArchivo());
        comprobante.setUrl(request.getUrl());
        comprobante.setMimeType(request.getMimeType());
        comprobante.setFechaCarga(LocalDateTime.now());
        return toResponse(repository.save(comprobante));
    }

    @Transactional(readOnly = true)
    public ComprobanteResponse porPago(Long pagoId) {
        return repository.findByPagoId(pagoId).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante no encontrado"));
    }

    private ComprobanteResponse toResponse(Comprobante c) {
        return ComprobanteResponse.builder()
                .id(c.getId()).pagoId(c.getPago().getId())
                .nombreArchivo(c.getNombreArchivo()).url(c.getUrl())
                .mimeType(c.getMimeType()).fechaCarga(c.getFechaCarga()).build();
    }
}
