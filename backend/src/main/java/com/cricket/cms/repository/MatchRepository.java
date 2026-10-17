package com.cricket.cms.repository;

import com.cricket.cms.model.Match;
import com.cricket.cms.model.MatchStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {

    // Loads both teams in the same query instead of one extra query per match.
    @EntityGraph(attributePaths = {"teamA", "teamB"})
    List<Match> findAllByOrderByStartTimeDesc();

    List<Match> findByStatus(MatchStatus status);
}
