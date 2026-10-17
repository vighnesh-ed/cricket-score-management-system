package com.cricket.cms.repository;

import com.cricket.cms.model.BallEvent;
import com.cricket.cms.model.Innings;
import com.cricket.cms.model.Player;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BallEventRepository extends JpaRepository<BallEvent, Long> {

    List<BallEvent> findByInningsOrderByIdAsc(Innings innings);

    @Query("select b from BallEvent b where b.batsman = :player or b.bowler = :player")
    List<BallEvent> findInvolving(@Param("player") Player player);
}
