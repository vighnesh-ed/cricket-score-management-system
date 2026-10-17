package com.cricket.cms.service;

// Published whenever a match's score changes. Stream clients are updated from it once the change commits.
public record ScoreChangedEvent(Long matchId) {
}
