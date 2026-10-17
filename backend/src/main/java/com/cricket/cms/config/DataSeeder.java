package com.cricket.cms.config;

import static com.cricket.cms.model.PlayerRole.ALL_ROUNDER;
import static com.cricket.cms.model.PlayerRole.BATSMAN;
import static com.cricket.cms.model.PlayerRole.BOWLER;
import static com.cricket.cms.model.PlayerRole.WICKET_KEEPER;

import com.cricket.cms.model.Match;
import com.cricket.cms.model.Player;
import com.cricket.cms.model.Team;
import com.cricket.cms.repository.MatchRepository;
import com.cricket.cms.repository.PlayerRepository;
import com.cricket.cms.repository.TeamRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // The MySQL schema persists between restarts, so only seed an empty database.
        if (teamRepository.count() > 0) {
            log.info("Seed data already present, skipping");
            return;
        }

        Team india = teamRepository.save(new Team("India", "IND"));
        Team australia = teamRepository.save(new Team("Australia", "AUS"));

        playerRepository.saveAll(indiaPlayers(india));
        playerRepository.saveAll(australiaPlayers(australia));

        matchRepository.save(new Match(india, australia, "Wankhede Stadium, Mumbai", 20, LocalDateTime.now()));

        log.info("Seeded 2 teams, 22 players and 1 match");
    }

    // Both squads are listed in batting order.
    private List<Player> indiaPlayers(Team team) {
        return List.of(
                new Player("Rohit Sharma", BATSMAN, team),
                new Player("Shubman Gill", BATSMAN, team),
                new Player("Virat Kohli", BATSMAN, team),
                new Player("Shreyas Iyer", BATSMAN, team),
                new Player("KL Rahul", WICKET_KEEPER, team),
                new Player("Hardik Pandya", ALL_ROUNDER, team),
                new Player("Ravindra Jadeja", ALL_ROUNDER, team),
                new Player("Axar Patel", ALL_ROUNDER, team),
                new Player("Kuldeep Yadav", BOWLER, team),
                new Player("Jasprit Bumrah", BOWLER, team),
                new Player("Mohammed Siraj", BOWLER, team));
    }

    private List<Player> australiaPlayers(Team team) {
        return List.of(
                new Player("Travis Head", BATSMAN, team),
                new Player("Mitchell Marsh", ALL_ROUNDER, team),
                new Player("Steve Smith", BATSMAN, team),
                new Player("Marnus Labuschagne", BATSMAN, team),
                new Player("Glenn Maxwell", ALL_ROUNDER, team),
                new Player("Josh Inglis", WICKET_KEEPER, team),
                new Player("Cameron Green", ALL_ROUNDER, team),
                new Player("Pat Cummins", BOWLER, team),
                new Player("Mitchell Starc", BOWLER, team),
                new Player("Adam Zampa", BOWLER, team),
                new Player("Josh Hazlewood", BOWLER, team));
    }
}
