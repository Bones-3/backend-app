package com.nativa.finanzas_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.nativa.finanzas_service.client.dto.VecinoResponse;

@FeignClient(name = "vecino-service")
public interface VecinoClient {

    @GetMapping("/vecino/vecinos/{id}")
    VecinoResponse obtenerPorId (@PathVariable Long id);
}

