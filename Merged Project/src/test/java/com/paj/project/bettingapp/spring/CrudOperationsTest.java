package com.paj.project.bettingapp.spring;

import com.paj.project.bettingapp.bet.model.Bet;
import com.paj.project.bettingapp.bet.model.Ticket;
import com.paj.project.bettingapp.bet.model.TicketStatus;
import com.paj.project.bettingapp.bet.repository.TicketRepository;
import com.paj.project.bettingapp.match.model.Match;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static com.paj.project.bettingapp.util.TicketListCreator.buildTicketList;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringDataConfiguration.class})
public class CrudOperationsTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    public void testCreateOperations() {
        List<Ticket> ticketList = buildTicketList();

        long time1 = System.nanoTime();
        ticketRepository.saveAll(ticketList);
        long time2 = System.nanoTime();
        long timeDiffC = (time2 - time1) / 1000_000;

        for (Ticket ticket : ticketList) {
            ticket.setTicketStatus(TicketStatus.Won);
            for (Bet bet : ticket.getBets()) {
                bet.setResultChoice("2");
                Match match = bet.getMatch();
                match.setName("FCSB - Dinamo");
            }
        }

        time1 = System.nanoTime();
        ticketRepository.saveAll(ticketList);
        time2 = System.nanoTime();
        long timeDiffU = (time2 - time1) / 1000_000;

        time1 = System.nanoTime();
        ticketRepository.findAll();
        time2 = System.nanoTime();
        long timeDiffR = (time2 - time1) / 1000_000;

        time1 = System.nanoTime();
        ticketRepository.deleteAll(ticketList);
        time2 = System.nanoTime();
        long timeDiffD = (time2 - time1) / 1000_000;

        System.out.println("Insert execution time: " + timeDiffC);
        System.out.println("Read execution time: " + timeDiffR);
        System.out.println("Update execution time: " + timeDiffU);
        System.out.println("Delete execution time: " + timeDiffD);
    }
}
