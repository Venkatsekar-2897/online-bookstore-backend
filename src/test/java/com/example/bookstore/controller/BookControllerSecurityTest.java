package com.example.bookstore.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerSecurityTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void bookListingIsPublic() throws Exception {
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "reader@example.com", authorities = "SCOPE_USER")
    void regularUserCannotCreateBook() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Test Book","author":"Test Author","price":12.50,"stock":3,"description":"Test"}
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "SCOPE_ADMIN")
    void adminCanCreateBook() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Security Test Book","author":"Test Author","price":12.50,"stock":3,"description":"Test"}
                    """))
            .andExpect(status().isCreated());
    }

    @Test
    void anonymousUserCannotAccessCart() throws Exception {
        mockMvc.perform(get("/api/cart"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@example.com", authorities = "SCOPE_ADMIN")
    void invalidBookRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"","author":"Test Author","price":-1,"stock":3,"description":"Test"}
                    """))
            .andExpect(status().isBadRequest());
    }
}
