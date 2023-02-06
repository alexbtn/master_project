package com.paj.project.bettingapp.jpa;

import com.paj.project.bettingapp.bet.model.Ticket;
import com.paj.project.bettingapp.util.TicketListCreator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.paj.project.bettingapp.util.TicketListCreator.updateTicketList;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class JPABenchmark {

    @State(Scope.Thread)
    public static class MyState {
        List<Ticket> tickets;
        long time1;

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpa");
        EntityManager em = emf.createEntityManager();

        public void updateTickets() {
            updateTicketList(tickets);
        }

        public void start() {
            tickets = TicketListCreator.buildTicketList();
            time1 = System.nanoTime();
        }

        public void create() {
            System.out.println("Insert execution time: " + (System.nanoTime() - time1) / 1000_000);
            time1 = System.nanoTime();
        }

        public void read() {
            System.out.println("Read execution time: " + (System.nanoTime() - time1) / 1000_000);
        }

        public void update() {
            System.out.println("Update execution time: " + (System.nanoTime() - time1) / 1000_000);
            time1 = System.nanoTime();
        }

        public void delete() {
            System.out.println("Delete execution time: " + (System.nanoTime() - time1) / 1000_000);
            time1 = System.nanoTime();
        }
    }

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(".*" + JPABenchmark.class.getCanonicalName() + ".*")
                .timeout(TimeValue.hours(24))
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @TearDown(Level.Trial)
    public void doTearDown(MyState state) {
        state.em.close();
        state.emf.close();
    }

    @Benchmark
    public void createReadUpdateDelete(MyState state, Blackhole bh) {
        state.start();
        state.em.getTransaction().begin();
        for (Ticket ticket : state.tickets) {
            state.em.persist(ticket);
        }
        state.em.getTransaction().commit();
        state.create();

        CriteriaBuilder cb = state.em.getCriteriaBuilder();
        CriteriaQuery<Ticket> cq = cb.createQuery(Ticket.class);
        Root<Ticket> rootEntry = cq.from(Ticket.class);
        CriteriaQuery<Ticket> all = cq.select(rootEntry);
        TypedQuery<Ticket> allQuery = state.em.createQuery(all);
        bh.consume(allQuery.getResultList());

        state.read();

        state.updateTickets();

        state.em.getTransaction().begin();
        for (Ticket ticket : state.tickets) {
            state.em.merge(ticket);
        }
        state.em.getTransaction().commit();

        state.update();

        state.em.getTransaction().begin();
        for (Ticket ticket : state.tickets) {
            state.em.remove(ticket);
        }
        state.em.getTransaction().commit();

        state.delete();
    }

}
