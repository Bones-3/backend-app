package com.example.auth_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.auth_service.client.dto.VecinoSyncRequest;

@FeignClient(name = "vecino-service", configuration = VecinoClientConfig.class)
public interface VecinoClient {
    @PostMapping("/vecino/vecinos/sincronizar")
    void sincronizar(@RequestBody VecinoSyncRequest request);
}
