package ru.vasil.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * vasil: 01.09.15
 */
public class SystemJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(SystemJob.class);
    public static final String COMMAND_TAG = "COMMAND";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String command = context.getJobDetail().getJobDataMap().getString(COMMAND_TAG);
        log.info("Starting command {}, next run at {}", command, context.getNextFireTime());
        long time = System.currentTimeMillis();
        StringBuilder outBuilder = new StringBuilder();
        StringBuilder errBuilder = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec(command);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            String line;
            while ((line = stdInput.readLine()) != null) {
                outBuilder.append(line).append("\n");
            }

            while ((line = stdError.readLine()) != null) {
                errBuilder.append(line).append("\n");
            }
            log.info("Completed command {} in {} millis", command, System.currentTimeMillis() - time);
        } catch (IOException e) {
            log.error("Failed to execute command " + command, e);
        } finally {
            if (outBuilder.length() > 0) {
                log.info("Output of command {} is:\n {}", command, outBuilder);
            }
            if (errBuilder.length() > 0) {
                log.info("Output of command {} is:\n {}", command, errBuilder);
            }
        }
    }
}
