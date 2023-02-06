package com.paj.project.bettingapp.hibernate;

import com.paj.project.bettingapp.bet.model.Bet;
import com.paj.project.bettingapp.bet.model.Ticket;
import com.paj.project.bettingapp.match.model.Match;
import com.paj.project.bettingapp.util.TicketListCreator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.paj.project.bettingapp.util.TicketListCreator.updateTicketList;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class HibernateBenchmark {

    @State(Scope.Thread)
    public static class MyState {
        List<Ticket> tickets;
        long time1;

        SessionFactory sessionFactory = createSessionFactory();
        Session session = sessionFactory.openSession();

        public static SessionFactory createSessionFactory() {
            Configuration configuration = new Configuration();
            configuration.configure().addAnnotatedClass(Ticket.class).addAnnotatedClass(Bet.class).addAnnotatedClass(Match.class);
            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().
                    applySettings(configuration.getProperties()).build();
            return configuration.buildSessionFactory(serviceRegistry);
        }

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
                .include(".*" + com.paj.project.bettingapp.hibernate.HibernateBenchmark.class.getCanonicalName() + ".*")
                .timeout(TimeValue.hours(24))
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @TearDown(Level.Trial)
    public void doTearDown(MyState state) {
        state.session.close();
        state.sessionFactory.close();
    }

    @Benchmark
    public void createReadUpdateDelete(MyState state, Blackhole bh) {
        state.start();
        state.session.getTransaction().begin();
        for (Ticket ticket : state.tickets) {
            state.session.persist(ticket);
        }
        state.session.getTransaction().commit();
        state.create();

        CriteriaBuilder builder = state.session.getCriteriaBuilder();
        CriteriaQuery<Ticket> criteria = builder.createQuery(Ticket.class);
        criteria.from(Ticket.class);
        bh.consume(state.session.createQuery(criteria).getResultList());

        state.read();

        state.updateTickets();

        state.session.getTransaction().begin();
        for (Ticket ticket : state.tickets) {
            state.session.update(ticket);
        }
        state.session.getTransaction().commit();

        state.update();

        state.session.getTransaction().begin();
        for (Ticket ticket : state.tickets) {
            state.session.remove(ticket);
        }
        state.session.getTransaction().commit();

        state.delete();
    }

}