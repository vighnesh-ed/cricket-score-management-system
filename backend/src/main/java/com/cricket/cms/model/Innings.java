package com.cricket.cms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "innings")
@Getter
@Setter
@NoArgsConstructor
public class Innings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batting_team_id")
    private Team battingTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bowling_team_id")
    private Team bowlingTeam;

    @Column(nullable = false)
    private int inningsNumber;

    @Column(nullable = false)
    private boolean completed;

    // Runs needed to win, only set for the second innings.
    private Integer target;

    // Null once the batting side is all out.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "striker_id")
    private Player striker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "non_striker_id")
    private Player nonStriker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bowler_id")
    private Player bowler;

    public Innings(Match match, int inningsNumber, Team battingTeam, Team bowlingTeam, Integer target) {
        this.match = match;
        this.inningsNumber = inningsNumber;
        this.battingTeam = battingTeam;
        this.bowlingTeam = bowlingTeam;
        this.target = target;
    }
}
