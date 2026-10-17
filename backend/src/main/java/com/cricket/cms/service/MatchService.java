package com.cricket.cms.service;

import com.cricket.cms.dto.MatchResponse;
import com.cricket.cms.repository.MatchRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    @Transactional(readOnly = true)
    public List<MatchResponse> getAllMatches() {
        return matchRepository.findAllByOrderByStartTimeDesc().stream()
                .map(MatchResponse::from)
                .toList();
    }
}
