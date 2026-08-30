package com.opsconsole.apitester;

import com.opsconsole.auth.domain.AppUser;
import com.opsconsole.auth.repository.AppUserRepository;
import com.opsconsole.auth.domain.OpsUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "opsconsole.features.api-tester-enabled=true")
class ApiTesterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Test
    void apiTesterPage_renders() throws Exception {
        AppUser admin = userRepository.findByAzureAdId("dev-admin").orElseThrow();
        mockMvc.perform(get("/api-tester").with(user(OpsUserPrincipal.fromUser(admin))))
                .andExpect(status().isOk())
                .andExpect(view().name("api-tester"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("api-tester.js")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("atSendBtn")));
    }

    @Test
    void proxy_healthEndpoint() throws Exception {
        AppUser admin = userRepository.findByAzureAdId("dev-admin").orElseThrow();
        String body = """
                {"method":"GET","url":"http://localhost/actuator/health","headers":[],"body":null}
                """;
        mockMvc.perform(post("/api/api-tester/proxy")
                        .with(user(OpsUserPrincipal.fromUser(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMs").exists());
    }

    @Test
    void proxy_rejectsInvalidUrl() throws Exception {
        AppUser admin = userRepository.findByAzureAdId("dev-admin").orElseThrow();
        String body = """
                {"method":"GET","url":"ftp://bad.example","headers":[],"body":null}
                """;
        mockMvc.perform(post("/api/api-tester/proxy")
                        .with(user(OpsUserPrincipal.fromUser(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
