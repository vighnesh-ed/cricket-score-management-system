package com.cricket.cms.service;

import com.cricket.cms.dto.BatsmanScore;
import com.cricket.cms.dto.PlayerSummary;
import com.cricket.cms.dto.ScoreResponse;
import com.cricket.cms.dto.TeamResponse;
import com.cricket.cms.exception.ResourceNotFoundException;
import com.cricket.cms.model.BallEvent;
import com.cricket.cms.model.Innings;
import com.cricket.cms.model.Match;
import com.cricket.cms.model.Player;
import com.cricket.cms.repository.BallEventRepository;
import com.cricket.cms.repository.InningsRepository;
import com.cricket.cms.repository.MatchRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScoreboardService {

    private static final int RECENT_BALLS = 6;

    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final BallEventRepository ballEventRepository;

    @Transactional(readOnly = true)
    public ScoreResponse getScore(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match " + matchId + " not found"));
        return inningsRepository.findFirstByMatchOrderByInningsNumberDesc(match)
                .map(innings -> inningsScore(match, innings))
                .orElseGet(() -> notStarted(match));
    }

    private ScoreResponse notStarted(Match match) {
        return ScoreResponse.builder()
                .matchId(match.getId())
                .status(match.getStatus())
                .overs("0.0")
                .lastSixBalls(List.of())
                .build();
    }

    private ScoreResponse inningsScore(Match match, Innings innings) {
        List<BallEvent> balls = ballEventRepository.findByInningsOrderByIdAsc(innings);
        InningsTotals totals = InningsTotals.from(balls);
        Integer target = innings.getTarget();
        return ScoreResponse.builder()
                .matchId(match.getId())
                .status(match.getStatus())
                .result(match.getResult())
                .inningsNumber(innings.getInningsNumber())
                .battingTeam(TeamResponse.from(innings.getBattingTeam()))
                .runs(totals.runs())
                .wickets(totals.wickets())
                .overs(totals.overs())
                .runRate(totals.runRate())
                .striker(batsmanScore(innings.getStriker(), balls))
                .nonStriker(batsmanScore(innings.getNonStriker(), balls))
                .bowler(PlayerSummary.from(innings.getBowler()))
                .lastSixBalls(lastSixBalls(balls))
                .target(target)
                .runsRequired(target == null ? null : Math.max(0, target - totals.runs()))
                .build();
    }

    private BatsmanScore batsmanScore(Player batsman, List<BallEvent> balls) {
        if (batsman == null) {
            return null;
        }
        int runs = 0;
        int ballsFaced = 0;
        for (BallEvent ball : balls) {
            if (ball.getBatsman().getId().equals(batsman.getId())) {
                runs += ball.getRuns();
                ballsFaced += ball.countsAsBallFaced() ? 1 : 0;
            }
        }
        return new BatsmanScore(batsman.getId(), batsman.getName(), runs, ballsFaced);
    }

    private List<String> lastSixBalls(List<BallEvent> balls) {
        int from = Math.max(0, balls.size() - RECENT_BALLS);
        return balls.subList(from, balls.size()).stream().map(this::label).toList();
    }

    private String label(BallEvent ball) {
        if (ball.isWicket()) {
            return "W";
        }
        return switch (ball.getExtraType()) {
            case WIDE -> "Wd";
            case NO_BALL -> "Nb";
            case NONE -> String.valueOf(ball.getRuns());
        };
    }
}
