package ru.vasil.crontab;

import com.google.common.base.Optional;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vasil.file.FileParser;
import ru.vasil.job.SystemJob;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * vasil: 01.09.15
 */
public class Crontab {
    private static final Logger log = LoggerFactory.getLogger(Crontab.class);
    private final ConcurrentMap<String, Task> map = new ConcurrentHashMap<>();
    private final Scheduler scheduler;

    public Crontab(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void refresh(Path path) throws IOException, SchedulerException {
        log.info("Refreshing crontab from path {}", path);
        long time = System.currentTimeMillis();
        Optional<Map<String, FileParser.JobDetails>> parsedOptional = FileParser.parse(path);
        if (!parsedOptional.isPresent()) {
            log.error("Crontab not parsed");
            return;
        }

        Map<String, FileParser.JobDetails> parsed = parsedOptional.get();

        Set<String> usedLines = new HashSet<>();
        for (Map.Entry<String, FileParser.JobDetails> entry : parsed.entrySet()) {
            String line = entry.getKey();
            if (map.containsKey(line)) {
                log.info("Task didn't change, skipping: {}", line);
                usedLines.add(line);
            } else {
                FileParser.JobDetails details = entry.getValue();
                Task task = scheduleTask(details);
                log.info("Scheduled {} with command {}, first run at {}", details.schedule, details.command, task.firstRun);
                map.put(line, task);
                usedLines.add(line);
            }
        }

        Set<String> toStop = new HashSet<>(map.keySet());
        toStop.removeAll(usedLines);
        for (String line : toStop) {
            log.info("Stopping disappeared command {}", line);
            Task task = map.remove(line);
            scheduler.deleteJob(task.job.getKey());
        }

        log.info("Crontab refresh completed in {} millis", System.currentTimeMillis() - time);
    }

    private Task scheduleTask(FileParser.JobDetails details) throws SchedulerException {
        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(details.schedule))
                .build();
        JobDetail job = JobBuilder.newJob(SystemJob.class)
                .usingJobData(SystemJob.COMMAND_TAG, details.command)
                .build();
        Date firstRun = scheduler.scheduleJob(job, trigger);
        return new Task(details.schedule, details.command, trigger, job, firstRun);
    }

    private static class Task {
        final String schedule;
        final String command;
        final Trigger trigger;
        final JobDetail job;
        final Date firstRun;

        public Task(String schedule, String command, Trigger trigger, JobDetail job, Date firstRun) {
            this.schedule = schedule;
            this.command = command;
            this.trigger = trigger;
            this.job = job;
            this.firstRun = firstRun;
        }


        @Override
        public String toString() {
            return "Task{" +
                    "schedule='" + schedule + '\'' +
                    ", command='" + command + '\'' +
                    ", trigger=" + trigger +
                    ", job=" + job +
                    ", firstRun=" + firstRun +
                    '}';
        }
    }
}
