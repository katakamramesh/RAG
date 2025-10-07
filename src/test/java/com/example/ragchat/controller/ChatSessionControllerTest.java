package com.example.ragchat.controller;

import com.example.ragchat.dto.ChatMessageDTO;
import com.example.ragchat.dto.ChatSessionDTO;
import com.example.ragchat.exception.SessionNotFoundException;
import com.example.ragchat.exception.InvalidInputException;
import com.example.ragchat.model.ChatMessage;
import com.example.ragchat.model.ChatSession;
import com.example.ragchat.service.ChatSessionService;
import com.example.ragchat.service.LLMService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(ChatSessionController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "api.keys=my-secret-key,admin-key-123"
})
class ChatSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatSessionService service;


    @MockBean
    private LLMService llmService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateSession_Success() throws Exception {
        ChatSessionDTO dto = new ChatSessionDTO("user1", "SessionName");
        ChatSession session = new ChatSession();
        session.setId(1L);
        session.setUserId("user1");
        session.setName("SessionName");
        session.setFavorite(false);
        session.setCreatedAt(new Date());
        session.setUpdatedAt(new Date());

        Mockito.when(service.createSession("user1", "SessionName")).thenReturn(session);

        mockMvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
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
                .andExpect(jsonPath("$.status").value(400));
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
        ChatSession session = new ChatSession();
        session.setId(1L);
        session.setUserId("user1");
        session.setName("SessionName");
        session.setFavorite(false);
        session.setCreatedAt(new Date());
        session.setUpdatedAt(new Date());

        Mockito.when(service.getSessionById(1L)).thenReturn(session);

        mockMvc.perform(get("/api/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("SessionName"));
    }

    @Test
    void testGetSession_NotFound() throws Exception {
        Mockito.when(service.getSessionById(999L))
                .thenThrow(new SessionNotFoundException("999"));

        mockMvc.perform(get("/api/sessions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testGetSessionsByUser_Success() throws Exception {
        ChatSession session1 = new ChatSession();
        session1.setId(1L);
        session1.setUserId("user1");
        session1.setName("Session 1");
        session1.setFavorite(false);

        ChatSession session2 = new ChatSession();
        session2.setId(2L);
        session2.setUserId("user1");
        session2.setName("Session 2");
        session2.setFavorite(true);

        Mockito.when(service.getSessionsByUserId("user1"))
                .thenReturn(List.of(session1, session2));

        mockMvc.perform(get("/api/sessions")
                        .param("userId", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void testRenameSession_Success() throws Exception {
        Mockito.doNothing().when(service).renameSession(1L, "NewName");

        mockMvc.perform(patch("/api/sessions/1/rename")
                        .param("name", "NewName"))
                .andExpect(status().isOk());

        Mockito.verify(service).renameSession(1L, "NewName");
    }

    @Test
    void testRenameSession_NotFound() throws Exception {
        Mockito.doThrow(new SessionNotFoundException("999"))
                .when(service).renameSession(999L, "NewName");

        mockMvc.perform(patch("/api/sessions/999/rename")
                        .param("name", "NewName"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testMarkFavorite_Success() throws Exception {
        Mockito.doNothing().when(service).markFavorite(1L, true);

        mockMvc.perform(patch("/api/sessions/1/favorite")
                        .param("favorite", "true"))
                .andExpect(status().isOk());

        Mockito.verify(service).markFavorite(1L, true);
    }

    @Test
    void testMarkFavorite_Unmark() throws Exception {
        Mockito.doNothing().when(service).markFavorite(1L, false);

        mockMvc.perform(patch("/api/sessions/1/favorite")
                        .param("favorite", "false"))
                .andExpect(status().isOk());

        Mockito.verify(service).markFavorite(1L, false);
    }

    @Test
    void testDeleteSession_Success() throws Exception {
        Mockito.doNothing().when(service).deleteSession(1L);

        mockMvc.perform(delete("/api/sessions/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(service).deleteSession(1L);
    }

    @Test
    void testDeleteSession_NotFound() throws Exception {
        Mockito.doThrow(new SessionNotFoundException("999"))
                .when(service).deleteSession(999L);

        mockMvc.perform(delete("/api/sessions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testAddMessage_Success() throws Exception {
        ChatMessageDTO dto = new ChatMessageDTO("user", "Hello, AI!", "Some context");
        ChatMessage msg = new ChatMessage();
        msg.setId(1L);
        msg.setSessionId(1L);
        msg.setSender("user");
        msg.setContent("Hello, AI!");
        msg.setContext("Some context");
        msg.setTimestamp(new Date());

        Mockito.when(service.addMessage(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(msg);

        mockMvc.perform(post("/api/sessions/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Hello, AI!"))
                .andExpect(jsonPath("$.sender").value("user"));
    }

    @Test
    void testGetMessages_Success() throws Exception {
        ChatMessage msg1 = new ChatMessage();
        msg1.setId(1L);
        msg1.setSessionId(1L);
        msg1.setSender("user");
        msg1.setContent("Hello");
        msg1.setTimestamp(new Date());

        ChatMessage msg2 = new ChatMessage();
        msg2.setId(2L);
        msg2.setSessionId(1L);
        msg2.setSender("bot");
        msg2.setContent("Hi there!");
        msg2.setTimestamp(new Date());

        Mockito.when(service.getMessages(1L, 0, 20))
                .thenReturn(List.of(msg1, msg2));

        mockMvc.perform(get("/api/sessions/1/messages")
                        .param("skip", "0")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content").value("Hello"))
                .andExpect(jsonPath("$[1].content").value("Hi there!"));
    }
}
