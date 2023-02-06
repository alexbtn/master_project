package com.paj.project.bettingapp.match.repository;

import com.paj.project.bettingapp.match.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
}
