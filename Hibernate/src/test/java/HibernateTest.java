import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.Test;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class HibernateTest {

    private static SessionFactory createSessionFactory() {
        Configuration configuration = new Configuration();
        configuration.configure().addAnnotatedClass(Ticket.class).addAnnotatedClass(Bet.class).addAnnotatedClass(Match.class);
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().
                applySettings(configuration.getProperties()).build();
        return configuration.buildSessionFactory(serviceRegistry);
    }

    @Test
    void saveUpdateRetrieveDelete() {
        List<Ticket> tickets = buildTicketList();

        try (SessionFactory sessionFactory = createSessionFactory();
             Session session = sessionFactory.openSession()) {

            long time1 = System.nanoTime();
            session.beginTransaction();
            for (Ticket ticket : tickets) {
                session.persist(ticket);
            }
            session.getTransaction().commit();
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
            session.beginTransaction();
            for (Ticket ticket : tickets) {
                session.update(ticket);
            }
            session.getTransaction().commit();
            time2 = System.nanoTime();
            timeDiff = (time2 - time1) / 1000_000;
            System.out.println("Update execution time: " + timeDiff);

            time1 = System.nanoTime();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Ticket> criteria = builder.createQuery(Ticket.class);
            criteria.from(Ticket.class);
            session.createQuery(criteria).getResultList();
            time2 = System.nanoTime();
            timeDiff = (time2 - time1) / 1000_000;
            System.out.println("Retrieve execution time: " + timeDiff);

            time1 = System.nanoTime();
            session.beginTransaction();
            for (Ticket ticket : tickets) {
                session.remove(ticket);
            }
            session.getTransaction().commit();
            time2 = System.nanoTime();
            timeDiff = (time2 - time1) / 1000_000;
            System.out.println("Delete execution time: " + timeDiff);
        }
    }

    private List<Ticket> buildTicketList() {
        List<Ticket> ticketList = new ArrayList<>();
        for (int i = 0; i < 50000; i++) {
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