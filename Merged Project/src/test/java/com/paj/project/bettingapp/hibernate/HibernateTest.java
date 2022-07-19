package com.paj.project.bettingapp.hibernate;

import com.paj.project.bettingapp.bet.model.Bet;
import com.paj.project.bettingapp.bet.model.Ticket;
import com.paj.project.bettingapp.bet.model.TicketStatus;
import com.paj.project.bettingapp.match.model.Match;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.Test;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

import static com.paj.project.bettingapp.util.TicketListCreator.buildTicketList;

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
            long timeDiffC = (time2 - time1) / 1000_000;

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
            long timeDiffU = (time2 - time1) / 1000_000;

            time1 = System.nanoTime();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Ticket> criteria = builder.createQuery(Ticket.class);
            criteria.from(Ticket.class);
            session.createQuery(criteria).getResultList();
            time2 = System.nanoTime();
            long timeDiffR = (time2 - time1) / 1000_000;

            time1 = System.nanoTime();
            session.beginTransaction();
            for (Ticket ticket : tickets) {
                session.remove(ticket);
            }
            session.getTransaction().commit();
            time2 = System.nanoTime();
            long timeDiffD = (time2 - time1) / 1000_000;

            System.out.println("Insert execution time: " + timeDiffC);
            System.out.println("Retrieve execution time: " + timeDiffR);
            System.out.println("Update execution time: " + timeDiffU);
            System.out.println("Delete execution time: " + timeDiffD);
        }
    }

}