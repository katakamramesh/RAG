package com.example.ragchat.controller;

import com.example.ragchat.dto.ChatMessageDTO;
import com.example.ragchat.dto.ChatSessionDTO;
import com.example.ragchat.exception.SessionNotFoundException;
import com.example.ragchat.exception.InvalidInputException;
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
import static org.hamcrest.Matchers.*;

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
    void testCreateSession_Success() throws Exception {
        ChatSessionDTO dto = new ChatSessionDTO("user1", "SessionName");
        ChatSession session = ChatSession.builder()
                .id("session1")
                .userId("user1")
                .name("SessionName")
                .favorite(false)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        Mockito.when(service.createSession("user1", "SessionName")).thenReturn(session);

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("session1"))
                .andExpect(jsonPath("$.name").value("SessionName"))
                .andExpect(jsonPath("$.userId").value("user1"))
                .andExpect(jsonPath("$.favorite").value(false));
    }

    @Test
    void testCreateSession_ValidationError_EmptyUserId() throws Exception {
        ChatSessionDTO dto = new ChatSessionDTO("", "SessionName");

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void testCreateSession_ValidationError_EmptyName() throws Exception {
        ChatSessionDTO dto = new ChatSessionDTO("user1", "");

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testGetSession_Success() throws Exception {
        ChatSession session = ChatSession.builder()
                .id("session1")
                .userId("user1")
                .name("SessionName")
                .favorite(false)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        Mockito.when(service.getSessionById("session1")).thenReturn(session);

        mockMvc.perform(get("/api/sessions/session1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("session1"))
                .andExpect(jsonPath("$.name").value("SessionName"));
    }

    @Test
    void testGetSession_NotFound() throws Exception {
        Mockito.when(service.getSessionById("invalid-id"))
                .thenThrow(new SessionNotFoundException("invalid-id"));

        mockMvc.perform(get("/api/sessions/invalid-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Session not found with ID: invalid-id"));
    }

    @Test
    void testGetSessionsByUser_Success() throws Exception {
        ChatSession session1 = ChatSession.builder()
                .id("session1")
                .userId("user1")
                .name("Session 1")
                .favorite(false)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        ChatSession session2 = ChatSession.builder()
                .id("session2")
                .userId("user1")
                .name("Session 2")
                .favorite(true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        Mockito.when(service.getSessionsByUserId("user1"))
                .thenReturn(List.of(session1, session2));

        mockMvc.perform(get("/api/sessions")
                        .param("userId", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("session1"))
                .andExpect(jsonPath("$[1].id").value("session2"));
    }

    @Test
    void testRenameSession_Success() throws Exception {
        Mockito.doNothing().when(service).renameSession("session1", "NewName");

        mockMvc.perform(patch("/api/sessions/session1/rename")
                        .param("name", "NewName"))
                .andExpect(status().isOk());

        Mockito.verify(service).renameSession("session1", "NewName");
    }

    @Test
    void testRenameSession_NotFound() throws Exception {
        Mockito.doThrow(new SessionNotFoundException("invalid-id"))
                .when(service).renameSession("invalid-id", "NewName");

        mockMvc.perform(patch("/api/sessions/invalid-id/rename")
                        .param("name", "NewName"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Session not found with ID: invalid-id"));
    }

    @Test
    void testMarkFavorite_Success() throws Exception {
        Mockito.doNothing().when(service).markFavorite("session1", true);

        mockMvc.perform(patch("/api/sessions/session1/favorite")
                        .param("favorite", "true"))
                .andExpect(status().isOk());

        Mockito.verify(service).markFavorite("session1", true);
    }

    @Test
    void testMarkFavorite_Unmark() throws Exception {
        Mockito.doNothing().when(service).markFavorite("session1", false);

        mockMvc.perform(patch("/api/sessions/session1/favorite")
                        .param("favorite", "false"))
                .andExpect(status().isOk());

        Mockito.verify(service).markFavorite("session1", false);
    }

    @Test
    void testDeleteSession_Success() throws Exception {
        Mockito.doNothing().when(service).deleteSession("session1");

        mockMvc.perform(delete("/api/sessions/session1"))
                .andExpect(status().isNoContent());

        Mockito.verify(service).deleteSession("session1");
    }

    @Test
    void testDeleteSession_NotFound() throws Exception {
        Mockito.doThrow(new SessionNotFoundException("invalid-id"))
                .when(service).deleteSession("invalid-id");

        mockMvc.perform(delete("/api/sessions/invalid-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testAddMessage_Success() throws Exception {
        ChatMessageDTO dto = new ChatMessageDTO("user", "Hello, AI!", "Some context");
        ChatMessage msg = ChatMessage.builder()
                .id("msg1")
                .sessionId("session1")
                .sender("user")
                .content("Hello, AI!")
                .context("Some context")
                .timestamp(new Date())
                .build();

        Mockito.when(service.addMessage(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(msg);

        mockMvc.perform(post("/api/sessions/session1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("msg1"))
                .andExpect(jsonPath("$.content").value("Hello, AI!"))
                .andExpect(jsonPath("$.sender").value("user"));
    }

    @Test
    void testAddMessage_ValidationError_EmptySender() throws Exception {
        ChatMessageDTO dto = new ChatMessageDTO("", "Hello", "context");

        mockMvc.perform(post("/api/sessions/session1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testAddMessage_ValidationError_EmptyContent() throws Exception {
        ChatMessageDTO dto = new ChatMessageDTO("user", "", "context");

        mockMvc.perform(post("/api/sessions/session1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testAddMessage_SessionNotFound() throws Exception {
        ChatMessageDTO dto = new ChatMessageDTO("user", "Hello", "context");

        Mockito.when(service.addMessage(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new SessionNotFoundException("invalid-id"));

        mockMvc.perform(post("/api/sessions/invalid-id/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testGetMessages_Success() throws Exception {
        ChatMessage msg1 = ChatMessage.builder()
                .id("msg1")
                .sessionId("session1")
                .sender("user")
                .content("Hello")
                .timestamp(new Date())
                .build();

        ChatMessage msg2 = ChatMessage.builder()
                .id("msg2")
                .sessionId("session1")
                .sender("bot")
                .content("Hi there!")
                .timestamp(new Date())
                .build();

        Mockito.when(service.getMessages("session1", 0, 20))
                .thenReturn(List.of(msg1, msg2));

        mockMvc.perform(get("/api/sessions/session1/messages")
                        .param("skip", "0")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content").value("Hello"))
                .andExpect(jsonPath("$[1].content").value("Hi there!"));
    }

    @Test
    void testGetMessages_WithPagination() throws Exception {
        ChatMessage msg = ChatMessage.builder()
                .id("msg1")
                .sessionId("session1")
                .sender("user")
                .content("Test message")
                .timestamp(new Date())
                .build();

        Mockito.when(service.getMessages("session1", 10, 5))
                .thenReturn(List.of(msg));

        mockMvc.perform(get("/api/sessions/session1/messages")
                        .param("skip", "10")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        Mockito.verify(service).getMessages("session1", 10, 5);
    }

    @Test
    void testGetMessages_SessionNotFound() throws Exception {
        Mockito.when(service.getMessages("invalid-id", 0, 20))
                .thenThrow(new SessionNotFoundException("invalid-id"));

        mockMvc.perform(get("/api/sessions/invalid-id/messages")
                        .param("skip", "0")
                        .param("limit", "20"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testGetMessages_DefaultPagination() throws Exception {
        Mockito.when(service.getMessages("session1", 0, 20))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/sessions/session1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        Mockito.verify(service).getMessages("session1", 0, 20);
    }

    @Test
    void testHealth() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void testInvalidInputException_Handling() throws Exception {
        Mockito.when(service.getSessionById("session1"))
                .thenThrow(new InvalidInputException("Invalid session ID format"));

        mockMvc.perform(get("/api/sessions/session1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid session ID format"));
    }
}