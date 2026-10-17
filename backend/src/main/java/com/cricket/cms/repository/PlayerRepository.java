package com.cricket.cms.repository;

import com.cricket.cms.model.Player;
import com.cricket.cms.model.PlayerRole;
import com.cricket.cms.model.Team;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    // Players are seeded in batting order, so id order is batting order.
    List<Player> findByTeamOrderByIdAsc(Team team);

    List<Player> findByTeamAndRoleInOrderByIdDesc(Team team, Collection<PlayerRole> roles);
}
