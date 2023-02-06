package com.paj.project.bettingapp.jpa;

import com.paj.project.bettingapp.bet.model.Bet;
import com.paj.project.bettingapp.bet.model.Ticket;
import com.paj.project.bettingapp.bet.model.TicketStatus;
import com.paj.project.bettingapp.match.model.Match;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

import static com.paj.project.bettingapp.util.TicketListCreator.buildTicketList;

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
            em.getTransaction().begin();
            for (Ticket ticket : tickets) {
                em.merge(ticket);
            }
            em.getTransaction().commit();
            time2 = System.nanoTime();
            long timeDiffU = (time2 - time1) / 1000_000;

            time1 = System.nanoTime();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Ticket> cq = cb.createQuery(Ticket.class);
            Root<Ticket> rootEntry = cq.from(Ticket.class);
            CriteriaQuery<Ticket> all = cq.select(rootEntry);
            TypedQuery<Ticket> allQuery = em.createQuery(all);
            allQuery.getResultList();
            time2 = System.nanoTime();
            long timeDiffR = (time2 - time1) / 1000_000;

            time1 = System.nanoTime();
            em.getTransaction().begin();
            for (Ticket ticket : tickets) {
                em.remove(ticket);
            }
            em.getTransaction().commit();
            time2 = System.nanoTime();
            long timeDiffD = (time2 - time1) / 1000_000;

            System.out.println("Insert execution time: " + timeDiffC);
            System.out.println("Retrieve execution time: " + timeDiffR);
            System.out.println("Update execution time: " + timeDiffU);
            System.out.println("Delete execution time: " + timeDiffD);

            em.close();

        } finally {
            emf.close();
        }
    }

}