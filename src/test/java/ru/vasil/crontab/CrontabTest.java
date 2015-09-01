package ru.vasil.crontab;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.mockito.Matchers;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.*;

/**
 * vasil: 01.09.15
 */
public class CrontabTest {
    private static final String TEST_CRONTAB = "" +
            "*/10 * * * * ? echo \"Test\"\n" +
            " # comment\n" +
            "\n" +
            " 1  2 3 * * ?  test ";

    private static final String TEST_CRONTAB2 = "" +
            "*/10 * * * * ? echo \"Test\"\n" +
            " 1  2 3 * * ? new test";

    @Test
    public void testRefresh() throws Exception {
        Path tempFile = Files.createTempFile("quartz", "crontab");
        Files.write(tempFile, ImmutableList.of(TEST_CRONTAB), Charsets.UTF_8);

        Scheduler scheduler = mock(Scheduler.class);

        Crontab c = new Crontab(scheduler);

        c.refresh(tempFile);

        verify(scheduler, never()).deleteJob(Matchers.<JobKey>any());
        verify(scheduler, times(2)).scheduleJob(Matchers.<JobDetail>any(), Matchers.<Trigger>any());


        tempFile = Files.createTempFile("quartz", "crontab");
        Files.write(tempFile, ImmutableList.of(TEST_CRONTAB2), Charsets.UTF_8);

        c.refresh(tempFile);
        verify(scheduler, times(1)).deleteJob(Matchers.<JobKey>any());
        verify(scheduler, times(3)).scheduleJob(Matchers.<JobDetail>any(), Matchers.<Trigger>any());

    }
}