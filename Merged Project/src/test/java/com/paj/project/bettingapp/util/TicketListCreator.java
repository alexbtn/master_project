package com.paj.project.bettingapp.util;

import com.paj.project.bettingapp.bet.model.Bet;
import com.paj.project.bettingapp.bet.model.Ticket;
import com.paj.project.bettingapp.bet.model.TicketStatus;
import com.paj.project.bettingapp.match.model.Match;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TicketListCreator {

    public static List<Ticket> buildTicketList() {
        List<Ticket> ticketList = new ArrayList<>();
        for (int i = 0; i < getIterations(); i++) {
            Ticket ticket = createTicket();
            ticketList.add(ticket);
        }
        return ticketList;
    }

    public static void updateTicketList(List<Ticket> ticketList) {
        for (Ticket ticket : ticketList) {
            ticket.setTicketStatus(TicketStatus.Won);
            for (Bet bet : ticket.getBets()) {
                bet.setResultChoice("2");
                Match match = bet.getMatch();
                match.setName("FCSB - Dinamo");
            }
        }

    }

    private static int getIterations() {
        try (InputStream input = new FileInputStream("D:\\Proiect-PAJ\\master_project\\Merged Project\\src\\test\\resources\\config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return Integer.parseInt(prop.getProperty("number-of-iterations"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Match createMatch() {
        return Match.builder()
                .drawOdd(2.33)
                .matchDate(new Date())
                .hostTeamWinOdd(2.33)
                .name("Fiorentina - Inter")
                .visitorTeamWinOdd(2.33)
                .score("2 - 3")
                .build();
    }

    private static Bet createBet() {
        return Bet.builder()
                .match(createMatch())
                .resultChoice("X")
                .build();
    }

    private static Ticket createTicket() {
        return Ticket.builder()
                .ticketStatus(TicketStatus.Pending)
                .sum(20.0)
                .bets(new LinkedList<>(List.of(createBet())))
                .build();
    }

}
