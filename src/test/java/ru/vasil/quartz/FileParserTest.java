package ru.vasil.quartz;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import ru.vasil.file.FileParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * vasil: 01.09.15
 */
public class FileParserTest {
    private static final String TEST_CRONTAB = "" +
            "*/10 * * * * ? echo \"Test\"\n" +
            " # comment\n" +
            "\n" +
            " 1  2 3 4 5 6  test ";

    @Test
    public void testParse() throws Exception {
        Path tempFile = Files.createTempFile("quartz", "crontab");
        Files.write(tempFile, ImmutableList.of(TEST_CRONTAB), Charsets.UTF_8);
        Map<String, FileParser.JobDetails> parsed = FileParser.parse(tempFile).get();
        assertEquals(2, parsed.size());

        FileParser.JobDetails details;

        details = parsed.get("*/10 * * * * ? echo \"Test\"");
        assertEquals("*/10 * * * * ?", details.schedule);
        assertEquals("echo \"Test\"", details.command);

        details = parsed.get(" 1  2 3 4 5 6  test ");
        assertEquals("1  2 3 4 5 6", details.schedule);
        assertEquals("test ", details.command);
    }
}