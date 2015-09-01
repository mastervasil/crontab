package ru.vasil.file;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * vasil: 01.09.15
 */
public class FileParser {
    private static final Logger log = LoggerFactory.getLogger(FileParser.class);
    private static final Pattern emptyPattern = Pattern.compile("^\\s*$");
    private static final Pattern commentPattern = Pattern.compile("^\\s*#.*$");
    private static final Pattern jobPattern = Pattern.compile("^\\s*((?:\\S+\\s+){5}\\S+)\\s+(\\S.*)$");

    private FileParser() {
    }

    public static Optional<Map<String, JobDetails>> parse(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            log.error("Cannot open path {}", path);
            return Optional.absent();
        }

        Map<String, JobDetails> map = new HashMap<>();
        List<String> lines = Files.readAllLines(path, Charsets.UTF_8);

        for (String line : lines) {
            if (emptyPattern.matcher(line).matches()) {
                continue;
            }
            if (commentPattern.matcher(line).matches()) {
                log.debug("#{}", line);
                continue;
            }
            Matcher matcher = jobPattern.matcher(line);
            if (matcher.matches()) {
                String schedule = matcher.group(1);
                String command = matcher.group(2);
                log.debug("Schedule {}, job {}", schedule, command);
                map.put(line, new JobDetails(schedule, command));
            } else {
                log.error("", new RuntimeException("File " + path + ", wrong line " + line));
            }
        }

        return Optional.of(map);
    }

    public static class JobDetails {
        public final String schedule;
        public final String command;

        public JobDetails(String schedule, String command) {
            this.schedule = schedule;
            this.command = command;
        }
    }
}
