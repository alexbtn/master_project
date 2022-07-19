package com.paj.project.bettingapp.bet.model;

import lombok.*;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Data
@Entity
@Builder
@Table(name = "tickets")
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private TicketStatus ticketStatus;

    private Double sum;

    @OneToMany(cascade=CascadeType.ALL)
    private List<Bet> bets = new LinkedList<>();

}