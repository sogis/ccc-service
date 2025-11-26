package ch.so.agi.cccservice;

import ch.so.agi.cccservice.session.MockWebSocketSession;
import ch.so.agi.cccservice.session.Session;
import ch.so.agi.cccservice.session.Sessions;
import ch.so.agi.cccservice.session.SockConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VersionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void tearDown() {
        Sessions.removeAll();
    }

    @Test
    void returnsVersionAndSessionCount() throws Exception {
        Session session = new Session(
                UUID.randomUUID(),
                new SockConnection("app", SockConnection.PROTOCOL_V1, MockWebSocketSession.create()),
                true
        );
        Sessions.addOrReplace(session);

        mockMvc.perform(get("/version").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("ccc-service version 1.1\nSession count: 1"));
    }

    @Test
    void returnsZeroSessionCountWhenEmpty() throws Exception {
        mockMvc.perform(get("/version").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("ccc-service version 1.1\nSession count: 0"));
    }
}
