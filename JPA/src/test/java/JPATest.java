import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class JPATest {

    @Test
    void saveUpdateRetrieveDelete() {
        List<Ticket> tickets = buildTicketList();
        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("jpa");

        try {
            EntityManager em = emf.createEntityManager();

            long time1 = System.nanoTime();
            em.getTransaction().begin();
            for (Ticket ticket : tickets) {
                em.persist(ticket);
            }
            em.getTransaction().commit();
            long time2 = System.nanoTime();
            long timeDiff = (time2 - time1) / 1000_000;
            System.out.println("Insert execution time: " + timeDiff);

            for (Ticket ticket : tickets) {
                ticket.setTicketStatus(TicketStatus.Won);
                for (Bet bet : ticket.getBets()) {
                    bet.setResultChoice("2");
                    Match match = bet.getMatch();
                    match.setName("FCSB - Dinamo");
                }
            }

            time1 = System.nanoTime();
            em.getTransaction().begin();
            for (Ticket ticket : tickets) {
                em.merge(ticket);
            }
            em.getTransaction().commit();
            time2 = System.nanoTime();
            timeDiff = (time2 - time1) / 1000_000;
            System.out.println("Update execution time: " + timeDiff);

            time1 = System.nanoTime();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Ticket> cq = cb.createQuery(Ticket.class);
            Root<Ticket> rootEntry = cq.from(Ticket.class);
            CriteriaQuery<Ticket> all = cq.select(rootEntry);
            TypedQuery<Ticket> allQuery = em.createQuery(all);
            allQuery.getResultList();
            time2 = System.nanoTime();
            timeDiff = (time2 - time1) / 1000_000;
            System.out.println("Retrieve execution time: " + timeDiff);

            time1 = System.nanoTime();
            em.getTransaction().begin();
            for (Ticket ticket : tickets) {
                em.remove(ticket);
            }
            em.getTransaction().commit();
            time2 = System.nanoTime();
            timeDiff = (time2 - time1) / 1000_000;
            System.out.println("Delete execution time: " + timeDiff);

            em.close();

        } finally {
            emf.close();
        }
    }

    private List<Ticket> buildTicketList() {
        List<Ticket> ticketList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Ticket ticket = createTicket();
            ticketList.add(ticket);
        }
        return ticketList;
    }

    private Match createMatch() {
        return Match.builder()
                .drawOdd(2.33)
                .matchDate(new Date())
                .hostTeamWinOdd(2.33)
                .name("Fiorentina - Inter")
                .visitorTeamWinOdd(2.33)
                .score("2 - 3")
                .build();
    }

    private Bet createBet() {
        return Bet.builder()
                .match(createMatch())
                .resultChoice("X")
                .build();
    }

    private Ticket createTicket() {
        return Ticket.builder()
                .ticketStatus(TicketStatus.Pending)
                .sum(20.0)
                .bets(new LinkedList<>(List.of(createBet())))
                .build();
    }

}