package com.cricket.cms.repository;

import com.cricket.cms.model.BallEvent;
import com.cricket.cms.model.Innings;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BallEventRepository extends JpaRepository<BallEvent, Long> {

    List<BallEvent> findByInningsOrderByIdAsc(Innings innings);
}
