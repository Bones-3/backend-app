package com.example.auth_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.auth_service.client.dto.VecinoSyncRequest;
import com.example.auth_service.client.dto.VecinoSyncResponse;

@FeignClient(name = "vecino-service", configuration = VecinoClientConfig.class)
public interface VecinoClient {
    @PostMapping("/vecino/vecinos/sincronizar")
    VecinoSyncResponse sincronizar(@RequestBody VecinoSyncRequest request);
}
