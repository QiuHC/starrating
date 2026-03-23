package com.starrating;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class StarRegistrationSubmissionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(authorities = "shop_portal")
    void shopCanSubmitRegistrationWithChineseStarAndAttachmentFields() throws Exception {
        mockMvc.perform(post("/api/registrations/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"shopCode\":\"G100W\",\"shopName\":\"比亚迪广东店\",\"targetStar\":\"三星级\",\"paymentUrl\":\"pay.pdf\",\"canopyUrl\":\"\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string("提交成功"));

        mockMvc.perform(get("/api/registrations/list").param("shopCode", "G100W"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].shopCode").value("G100W"))
            .andExpect(jsonPath("$[0].shopName").value("比亚迪广东店"))
            .andExpect(jsonPath("$[0].targetStar").value("三星级"))
            .andExpect(jsonPath("$[0].paymentUrl").value("pay.pdf"))
            .andExpect(jsonPath("$[0].canopyUrl").value(""));
    }
}
