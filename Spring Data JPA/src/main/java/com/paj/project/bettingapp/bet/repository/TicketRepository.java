package com.paj.project.bettingapp.bet.repository;

import com.paj.project.bettingapp.bet.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
}