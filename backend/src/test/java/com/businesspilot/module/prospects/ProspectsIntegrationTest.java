package com.businesspilot.module.prospects;

import com.businesspilot.module.auth.dto.AuthResponse;
import com.businesspilot.module.auth.dto.RegisterRequest;
import java.util.UUID;
import com.businesspilot.module.prospects.dto.InteractionRequest;
import com.businesspilot.module.prospects.dto.ProspectRequest;
import com.businesspilot.module.prospects.dto.StatusUpdateRequest;
import com.businesspilot.module.prospects.domain.InteractionType;
import com.businesspilot.module.prospects.domain.ProspectStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
class ProspectsIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bp_test").withUsername("test").withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired WebApplicationContext wac;
    @Autowired ObjectMapper mapper;

    MockMvc mvc;
    String tokenOrg1;
    String tokenOrg2;

    @BeforeEach
    void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity()).build();
        tokenOrg1 = register("prospect-org1@test.com", "ProspectOrg1");
        tokenOrg2 = register("prospect-org2@test.com", "ProspectOrg2");
    }

    private String register(String email, String orgName) throws Exception {
        String uniqueEmail = email.replace("@", "." + UUID.randomUUID().toString().substring(0, 8) + "@");
        var req = new RegisterRequest(uniqueEmail, "password123!", "Test", "User", orgName);
        MvcResult res = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated()).andReturn();
        return mapper.readValue(res.getResponse().getContentAsString(), AuthResponse.class).accessToken();
    }

    @Test
    void createAndListProspect() throws Exception {
        var req = new ProspectRequest("Marie", "Dupont", "marie@test.com", "+33612345678", "Acme SA", "Prospect chaud");
        mvc.perform(post("/api/prospects")
                        .header("Authorization", "Bearer " + tokenOrg1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Marie"))
                .andExpect(jsonPath("$.status").value("LEAD"));

        mvc.perform(get("/api/prospects").header("Authorization", "Bearer " + tokenOrg1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void searchProspect() throws Exception {
        var req = new ProspectRequest("Jean", "Martin", "jean.martin@acme.com", null, "Acme Corp", null);
        mvc.perform(post("/api/prospects")
                        .header("Authorization", "Bearer " + tokenOrg1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/prospects?q=acme")
                        .header("Authorization", "Bearer " + tokenOrg1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].company").value("Acme Corp"));
    }

    @Test
    void updateStatus() throws Exception {
        var createReq = new ProspectRequest("Paul", "Test", null, null, null, null);
        MvcResult created = mvc.perform(post("/api/prospects")
                        .header("Authorization", "Bearer " + tokenOrg1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated()).andReturn();
        String id = mapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(patch("/api/prospects/" + id + "/status")
                        .header("Authorization", "Bearer " + tokenOrg1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new StatusUpdateRequest(ProspectStatus.QUALIFIED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUALIFIED"));
    }

    @Test
    void addInteractionAndGetDetail() throws Exception {
        var createReq = new ProspectRequest("Claire", "Dubois", "claire@test.com", null, "StartupXYZ", null);
        MvcResult created = mvc.perform(post("/api/prospects")
                        .header("Authorization", "Bearer " + tokenOrg1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated()).andReturn();
        String id = mapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(post("/api/prospects/" + id + "/interactions")
                        .header("Authorization", "Bearer " + tokenOrg1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new InteractionRequest(InteractionType.CALL, "Appel de qualification — intéressée"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("CALL"));

        mvc.perform(get("/api/prospects/" + id).header("Authorization", "Bearer " + tokenOrg1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interactions", hasSize(1)))
                .andExpect(jsonPath("$.interactions[0].content").value("Appel de qualification — intéressée"));
    }

    @Test
    void tenantIsolation() throws Exception {
        var req = new ProspectRequest("Secret", "Lead", null, null, "TopSecret Inc", null);
        mvc.perform(post("/api/prospects")
                        .header("Authorization", "Bearer " + tokenOrg1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/prospects").header("Authorization", "Bearer " + tokenOrg2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
