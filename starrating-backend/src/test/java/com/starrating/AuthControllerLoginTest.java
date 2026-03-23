package com.starrating;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class AuthControllerLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminSeedUserCanLoginWithSeedPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"6666666\",\"password\":\"88888888\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("6666666"))
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.redirect").value("/admin"));
    }
}
