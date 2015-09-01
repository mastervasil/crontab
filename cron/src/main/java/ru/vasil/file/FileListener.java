package ru.vasil.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vasil.crontab.Crontab;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * vasil: 30.08.15
 */
public class FileListener {
    private static final Logger log = LoggerFactory.getLogger(FileListener.class);
    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    private final String filename;
    private final long periodSeconds;
    private final Crontab crontab;
    private final ScheduledExecutorService executor;
    private volatile long lastModificationTime = 0;

    public FileListener(String filename, long periodSeconds, Crontab crontab) {
        this.filename = filename;
        this.periodSeconds = periodSeconds;
        this.crontab = crontab;
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        log.info("Starting crontab from file {}, refresh period {} millis", filename, periodSeconds);
        executor.scheduleAtFixedRate(new CrontabRefresher(), 0, periodSeconds, TimeUnit.SECONDS);
    }

    public void shutdown() throws InterruptedException {
        log.info("Stopping file listener");
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    private class CrontabRefresher implements Runnable {
        @Override
        public void run() {
            Path path = Paths.get(filename);
            try {
                long lastModified = Files.getLastModifiedTime(path).toMillis();
                if (lastModified != lastModificationTime) {
                    lastModificationTime = lastModified;
                    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                    String lastModificationString = sdf.format(lastModified);
                    log.info("Crontab file {} modified, modification time {}, reloading", filename, lastModificationString);
                    crontab.refresh(path);
                }
            } catch (Exception e) {
                log.error("Failed to check file " + filename, e);
            }
        }
    }
}
