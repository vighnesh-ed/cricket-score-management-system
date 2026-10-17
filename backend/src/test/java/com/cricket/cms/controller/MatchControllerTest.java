package com.cricket.cms.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cricket.cms.repository.MatchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// Each test rolls back, so every test sees the freshly seeded, not yet started match.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MatchControllerTest {

    private final MockMvc mockMvc;
    private final MatchRepository matchRepository;

    @Autowired
    MatchControllerTest(MockMvc mockMvc, MatchRepository matchRepository) {
        this.mockMvc = mockMvc;
        this.matchRepository = matchRepository;
    }

    @Test
    void getAllMatchesReturnsSeededMatch() throws Exception {
        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].teamA.name").value("India"))
                .andExpect(jsonPath("$[0].teamB.name").value("Australia"))
                .andExpect(jsonPath("$[0].totalOvers").value(20))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    void getAllMatchesAllowsAnyOrigin() throws Exception {
        mockMvc.perform(get("/api/matches").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "*"));
    }

    @Test
    void getScoreReflectsBallsRecordedAfterStart() throws Exception {
        Long matchId = matchRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/matches/{id}/start", matchId))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/matches/{id}/ball", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"outcome\": \"FOUR\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/matches/{id}/score", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIVE"))
                .andExpect(jsonPath("$.inningsNumber").value(1))
                .andExpect(jsonPath("$.battingTeam.name").value("India"))
                .andExpect(jsonPath("$.runs").value(4))
                .andExpect(jsonPath("$.wickets").value(0))
                .andExpect(jsonPath("$.overs").value("0.1"))
                .andExpect(jsonPath("$.runRate").value(24.0))
                .andExpect(jsonPath("$.striker.name").value("Rohit Sharma"))
                .andExpect(jsonPath("$.striker.runs").value(4))
                .andExpect(jsonPath("$.striker.balls").value(1))
                .andExpect(jsonPath("$.nonStriker.name").value("Shubman Gill"))
                .andExpect(jsonPath("$.bowler.name").value("Josh Hazlewood"))
                .andExpect(jsonPath("$.lastSixBalls[0]").value("4"))
                .andExpect(jsonPath("$.target").doesNotExist());
    }

    @Test
    void getScoreReturns404WithErrorBodyForUnknownMatch() throws Exception {
        mockMvc.perform(get("/api/matches/{id}/score", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Match 999 not found"))
                .andExpect(jsonPath("$.path").value("/api/matches/999/score"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
