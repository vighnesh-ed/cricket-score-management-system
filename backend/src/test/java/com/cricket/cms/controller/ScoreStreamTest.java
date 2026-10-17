package com.cricket.cms.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cricket.cms.model.BallOutcome;
import com.cricket.cms.repository.MatchRepository;
import com.cricket.cms.service.ScoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

// Not @Transactional: scores are only broadcast after a real commit.
// The context is discarded afterwards because these tests change the seeded match for good.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
class ScoreStreamTest {

    private final MockMvc mockMvc;
    private final ScoringService scoringService;
    private final MatchRepository matchRepository;

    @Autowired
    ScoreStreamTest(MockMvc mockMvc, ScoringService scoringService, MatchRepository matchRepository) {
        this.mockMvc = mockMvc;
        this.scoringService = scoringService;
        this.matchRepository = matchRepository;
    }

    @Test
    void streamReturnsEventStreamForExistingMatchAnd404ForMissingOne() throws Exception {
        Long matchId = matchRepository.findAll().get(0).getId();
        mockMvc.perform(get("/api/matches/{id}/stream", matchId))
                .andExpect(request().asyncStarted())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("event:score")));

        mockMvc.perform(get("/api/matches/{id}/stream", 999).accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Match 999 not found"));
    }

    @Test
    void ballRecordedThroughServiceReachesStreamSubscriber() throws Exception {
        Long matchId = matchRepository.findAll().get(0).getId();
        MvcResult stream = mockMvc.perform(get("/api/matches/{id}/stream", matchId))
                .andExpect(request().asyncStarted())
                .andReturn();

        scoringService.startMatch(matchId);
        scoringService.recordBall(matchId, BallOutcome.FOUR, null, null);

        // One event on connect, one after the start and one after the ball.
        String events = stream.getResponse().getContentAsString();
        assertEquals(3, events.lines().filter("event:score"::equals).count());
        assertTrue(events.contains("\"status\":\"LIVE\""));
        assertTrue(events.contains("\"overs\":\"0.1\""));
    }
}
