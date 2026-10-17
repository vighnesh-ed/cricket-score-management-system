package com.cricket.cms.repository;

import com.cricket.cms.model.Innings;
import com.cricket.cms.model.Match;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InningsRepository extends JpaRepository<Innings, Long> {

    Optional<Innings> findFirstByMatchOrderByInningsNumberDesc(Match match);
}
