package com.example.ragchat.controller;

import com.example.ragchat.dto.ChatMessageDTO;
import com.example.ragchat.dto.ChatSessionDTO;
import com.example.ragchat.model.ChatMessage;
import com.example.ragchat.model.ChatSession;
import com.example.ragchat.service.ChatSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatSessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatSessionService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateSession() throws Exception {
        ChatSessionDTO dto = new ChatSessionDTO("u1", "SessionName");
        ChatSession session = ChatSession.builder()
                .id("s1").userId("u1").name("SessionName")
                .favorite(false).createdAt(new Date()).updatedAt(new Date()).build();

        Mockito.when(service.createSession("u1", "SessionName")).thenReturn(session);

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("SessionName"));
    }

    @Test
    void testRenameSession() throws Exception {
        mockMvc.perform(patch("/api/sessions/s1/rename")
                        .param("name", "NewName"))
                .andExpect(status().isOk());

        Mockito.verify(service).renameSession("s1", "NewName");
    }

    @Test
    void testMarkFavorite() throws Exception {
        mockMvc.perform(patch("/api/sessions/s1/favorite")
                        .param("favorite", "true"))
                .andExpect(status().isOk());

        Mockito.verify(service).markFavorite("s1", true);
    }

    @Test
    void testDeleteSession() throws Exception {
        mockMvc.perform(delete("/api/sessions/s1"))
                .andExpect(status().isOk());

        Mockito.verify(service).deleteSession("s1");
    }

    @Test
    void testAddMessage() throws Exception {
        ChatMessageDTO dto = new ChatMessageDTO("user1", "Hello", "context");
        ChatMessage msg = ChatMessage.builder()
                .id("m1").sessionId("s1").sender("user1").content("Hello").timestamp(new Date()).build();

        Mockito.when(service.addMessage(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(msg);

        mockMvc.perform(post("/api/sessions/s1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello"));
    }

    @Test
    void testGetMessages() throws Exception {
        ChatMessage msg = ChatMessage.builder().id("m1").sessionId("s1").sender("bot").content("hi").build();
        Mockito.when(service.getMessages("s1", 0, 20)).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/sessions/s1/messages")
                        .param("skip", "0").param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("hi"));
    }

    @Test
    void testHealth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }
}
