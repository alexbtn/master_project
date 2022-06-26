package com.paj.project.bettingapp.bet.model;

import com.paj.project.bettingapp.match.model.Match;
import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Builder
@Table(name = "bets")
@AllArgsConstructor
@NoArgsConstructor
public class Bet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    private String resultChoice;

//    @ToString.Exclude
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "ticket_id", nullable = false)
//    private Ticket ticket;
}