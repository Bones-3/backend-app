package com.nativa.vecino_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VecinoServiceApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void swaggerNoExponeSortNiPageable() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paths['/vecino/vecinos'].get.parameters[?(@.name == 'page')]")
						.isNotEmpty())
				.andExpect(jsonPath("$.paths['/vecino/vecinos'].get.parameters[?(@.name == 'size')]")
						.isNotEmpty())
				.andExpect(jsonPath("$.paths['/vecino/vecinos'].get.parameters[?(@.name == 'sort')]")
						.isEmpty())
				.andExpect(jsonPath("$.paths['/vecino/vecinos'].get.parameters[?(@.name == 'pageable')]")
						.isEmpty());
	}

	@Test
	void listadoFuncionaSinSort() throws Exception {
		mockMvc.perform(get("/vecino/vecinos")
				.with(user("directiva").roles("DIRECTIVA"))
				.param("page", "0")
				.param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.contenido").isArray());
		mockMvc.perform(get("/vecino/vecinos")
				.with(user("directiva").roles("DIRECTIVA"))
				.param("sort", "[\"string\"]"))
				.andExpect(status().isOk());
	}

}

