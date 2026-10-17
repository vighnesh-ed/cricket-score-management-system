package com.cricket.cms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ball_events")
@Getter
@Setter
@NoArgsConstructor
public class BallEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innings_id")
    private Innings innings;

    @Column(nullable = false)
    private int overNumber;

    // Wides and no-balls are re-bowled, so they share the ball number of the next legal delivery.
    @Column(nullable = false)
    private int ballNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batsman_id")
    private Player batsman;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bowler_id")
    private Player bowler;

    @Column(nullable = false)
    private int runs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExtraType extraType = ExtraType.NONE;

    @Column(nullable = false)
    private int extraRuns;

    @Column(nullable = false)
    private boolean wicket;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public BallEvent(Innings innings, int overNumber, int ballNumber, Player batsman, Player bowler,
            BallOutcome outcome) {
        this.innings = innings;
        this.overNumber = overNumber;
        this.ballNumber = ballNumber;
        this.batsman = batsman;
        this.bowler = bowler;
        this.runs = outcome.getRuns();
        this.extraType = outcome.getExtraType();
        this.extraRuns = outcome.getExtraRuns();
        this.wicket = outcome.isWicket();
    }

    public int totalRuns() {
        return runs + extraRuns;
    }

    public boolean isLegalDelivery() {
        return extraType == ExtraType.NONE;
    }

    // A no-ball counts as a ball faced by the batsman, a wide does not.
    public boolean countsAsBallFaced() {
        return extraType != ExtraType.WIDE;
    }
}
