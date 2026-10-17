package com.cricket.cms.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cricket.cms.repository.MatchRepository;
import com.cricket.cms.repository.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StatsControllerTest {

    private final MockMvc mockMvc;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    @Autowired
    StatsControllerTest(MockMvc mockMvc, MatchRepository matchRepository, PlayerRepository playerRepository) {
        this.mockMvc = mockMvc;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    private Long startedMatchId() throws Exception {
        Long matchId = matchRepository.findAll().get(0).getId();
        mockMvc.perform(post("/api/matches/{id}/start", matchId)).andExpect(status().isOk());
        return matchId;
    }

    private void ball(Long matchId, String outcome) throws Exception {
        mockMvc.perform(post("/api/matches/{id}/ball", matchId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"outcome\": \"" + outcome + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void scorecardShowsBattersBowlersAndExtras() throws Exception {
        Long matchId = startedMatchId();
        ball(matchId, "SIX");
        ball(matchId, "WIDE");

        mockMvc.perform(get("/api/matches/{id}/scorecard", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIVE"))
                .andExpect(jsonPath("$.innings.length()").value(1))
                .andExpect(jsonPath("$.innings[0].battingTeam.name").value("India"))
                .andExpect(jsonPath("$.innings[0].runs").value(7))
                .andExpect(jsonPath("$.innings[0].extras.wides").value(1))
                .andExpect(jsonPath("$.innings[0].batting[0].name").value("Rohit Sharma"))
                .andExpect(jsonPath("$.innings[0].batting[0].sixes").value(1))
                .andExpect(jsonPath("$.innings[0].batting[0].dismissal").value("not out"))
                .andExpect(jsonPath("$.innings[0].bowling[0].name").value("Josh Hazlewood"))
                .andExpect(jsonPath("$.innings[0].bowling[0].runs").value(7));
    }

    @Test
    void scorecardReturns404WithErrorBodyForUnknownMatch() throws Exception {
        mockMvc.perform(get("/api/matches/{id}/scorecard", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Match 999 not found"))
                .andExpect(jsonPath("$.path").value("/api/matches/999/scorecard"));
    }

    @Test
    void summaryOfLiveMatchHasInningsAndTopBatter() throws Exception {
        Long matchId = startedMatchId();
        ball(matchId, "FOUR");

        mockMvc.perform(get("/api/matches/{id}/summary", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.innings[0].runs").value(4))
                .andExpect(jsonPath("$.innings[0].runsPerOver[0]").value(4))
                .andExpect(jsonPath("$.topBatters[0].name").value("Rohit Sharma"))
                .andExpect(jsonPath("$.playerOfTheMatch.name").value("Rohit Sharma"));
    }

    @Test
    void summaryReturns404ForUnknownMatch() throws Exception {
        mockMvc.perform(get("/api/matches/{id}/summary", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Match 999 not found"));
    }

    @Test
    void playersListCanBeFilteredByTeam() throws Exception {
        Long indiaPlayerId = playerRepository.findAllByOrderByIdAsc().get(0).getId();
        Long indiaId = playerRepository.findById(indiaPlayerId).orElseThrow().getTeam().getId();

        mockMvc.perform(get("/api/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(44));
        mockMvc.perform(get("/api/players").param("teamId", indiaId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(11))
                .andExpect(jsonPath("$[0].name").value("Rohit Sharma"))
                .andExpect(jsonPath("$[0].team.name").value("India"));
    }

    @Test
    void playersListReturns404ForUnknownTeam() throws Exception {
        mockMvc.perform(get("/api/players").param("teamId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Team 999 not found"));
    }

    @Test
    void playerStatsReflectBallsPlayed() throws Exception {
        Long matchId = startedMatchId();
        ball(matchId, "SIX");
        Long openerId = playerRepository.findAllByOrderByIdAsc().get(0).getId();

        mockMvc.perform(get("/api/players/{id}/stats", openerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rohit Sharma"))
                .andExpect(jsonPath("$.matches").value(1))
                .andExpect(jsonPath("$.runs").value(6))
                .andExpect(jsonPath("$.balls").value(1))
                .andExpect(jsonPath("$.sixes").value(1))
                .andExpect(jsonPath("$.strikeRate").value(600.0));
    }

    @Test
    void playerStatsReturn404ForUnknownPlayer() throws Exception {
        mockMvc.perform(get("/api/players/{id}/stats", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Player 999 not found"))
                .andExpect(jsonPath("$.path").value("/api/players/999/stats"));
    }
}
