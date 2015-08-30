package ru.vasil.quartz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * vasil: 30.08.15
 */
public class Crontab {
    private static final Logger log = LoggerFactory.getLogger(Crontab.class);

    private final String filename;
    private final long periodMillis;
    private final ScheduledExecutorService executor;

    public Crontab(String filename, long periodMillis) {
        this.filename = filename;
        this.periodMillis = periodMillis;
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        log.info("Starting crontab from file {}, refresh period {} millis", filename, periodMillis);
        executor.scheduleAtFixedRate(new CrontabRefresher(), periodMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    public void shutdown() throws InterruptedException {
        log.info("Stopping crontab");
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    private static class CrontabRefresher implements Runnable {
        @Override
        public void run() {

        }
    }
}
