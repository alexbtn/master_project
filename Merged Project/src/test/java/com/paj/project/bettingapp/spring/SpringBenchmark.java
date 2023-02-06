package com.paj.project.bettingapp.spring;

import com.paj.project.bettingapp.bet.model.Ticket;
import com.paj.project.bettingapp.bet.repository.TicketRepository;
import com.paj.project.bettingapp.hibernate.HibernateBenchmark;
import com.paj.project.bettingapp.util.TicketListCreator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.paj.project.bettingapp.util.TicketListCreator.updateTicketList;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 1)
@Measurement(iterations = 1)
public class SpringBenchmark {

    @State(Scope.Thread)
    public static class MyState {
        List<Ticket> tickets;
        long time1;

        TicketRepository ticketRepository;
        AnnotationConfigApplicationContext context = createContext();

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

        public AnnotationConfigApplicationContext createContext() {
            context = new AnnotationConfigApplicationContext();
            context.register(SpringDataConfiguration.class);
            context.refresh();
            ticketRepository = context.getBean(TicketRepository.class);
            return context;
        }
    }

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(".*" + com.paj.project.bettingapp.spring.SpringBenchmark.class.getCanonicalName() + ".*")
                .timeout(TimeValue.hours(44))
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @TearDown(Level.Trial)
    public void doTearDown(MyState state) {
        state.context.close();
    }

    @Benchmark
    public void testCreateOperations(MyState state, Blackhole bh) {
        state.start();
        bh.consume(state.ticketRepository.saveAll(state.tickets));
        state.create();

        bh.consume(state.ticketRepository.findAll());

        state.read();
        state.updateTickets();
        bh.consume(state.ticketRepository.saveAll(state.tickets));
        state.update();

        state.ticketRepository.deleteAll(state.tickets);
        state.delete();
    }
}



