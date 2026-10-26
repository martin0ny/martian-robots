package com.redbadger.martianrobots;

import com.redbadger.martianrobots.command.CommandRegistry;
import com.redbadger.martianrobots.input.MissionParser;
import com.redbadger.martianrobots.mission.MissionControl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractiveSessionTest {

    private static final String SAMPLE_FILE = Path.of("samples", "sample-input.txt").toString();
    private static final List<String> SAMPLE_OUTPUT = List.of("1 1 E", "3 3 N LOST", "2 3 S");

    @Nested
    class StepByStep {

        @Test
        void reportsFinalPositionsForTheRobotsEntered() {
            String output = runWith(
                    "2",
                    "5 3",
                    "1 1 E", "RFRFRFRF",
                    "3 2 N", "FRRFLLFFRRFLL",
                    "0 3 W", "LLFFFLFLFL",
                    "");

            assertEquals(SAMPLE_OUTPUT, lastLines(output, 3));
        }

        @Test
        void explainsInvalidAnswersAndAsksAgain() {
            String output = runWith(
                    "2",
                    "60 3", "5 3",
                    "9 9 N", "1 1 E",
                    "FXF", "", "F",
                    "");

            assertContains(output, "Invalid input: coordinate 60 must be between 0 and 50. Please try again.");
            assertContains(output, "Invalid input: starting position 9 9 is outside the grid. Please try again.");
            assertContains(output, "Invalid input: unknown instruction 'X' at column 2. Please try again.");
            assertContains(output, "Invalid input: instructions are missing. Please try again.");
            assertEquals(List.of("2 1 E"), lastLines(output, 1));
        }

        @Test
        void finishingWithoutRobotsReportsNothing() {
            String output = runWith("2", "5 3", "");

            assertContains(output, "No robots were deployed");
        }
    }

    @Nested
    class FromFile {

        @Test
        void runsTheMissionInTheFile() {
            String output = runWith("1", SAMPLE_FILE);

            assertEquals(SAMPLE_OUTPUT, lastLines(output, 3));
        }

        @Test
        void acceptsAQuotedPath() {
            String output = runWith("1", "\"" + SAMPLE_FILE + "\"");

            assertEquals(SAMPLE_OUTPUT, lastLines(output, 3));
        }

        @Test
        void asksAgainWhenTheFileDoesNotExist() {
            String output = runWith("1", "does-not-exist.txt", SAMPLE_FILE);

            assertContains(output, "File not found:");
            assertEquals(SAMPLE_OUTPUT, lastLines(output, 3));
        }

        @Test
        void pointsToTheBadLineWhenTheFileIsInvalid(@TempDir Path directory) throws IOException {
            Path badFile = Files.writeString(directory.resolve("bad.txt"), "5 3\n1 1 E\nFFZ\n");

            String output = runWith("1", badFile.toString(), SAMPLE_FILE);

            assertContains(output, "That file is not a valid mission. Line 3: unknown instruction 'Z' at column 3");
            assertEquals(SAMPLE_OUTPUT, lastLines(output, 3));
        }
    }

    @Test
    void asksAgainForAnUnknownMenuChoice() {
        String output = runWith("3", "2", "5 3", "");

        assertContains(output, "Please enter 1 or 2.");
    }

    @Test
    void exitsCleanlyWhenInputEndsEarly() {
        String output = runWith("2", "5 3", "1 1 E");

        assertContains(output, "No more input received. Exiting.");
    }

    private static String runWith(String... answers) {
        String input = String.join("\n", answers) + "\n";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        new InteractiveSession(
                new MissionParser(CommandRegistry.standard()),
                new MissionControl(),
                new BufferedReader(new StringReader(input)),
                out).run();

        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static List<String> lastLines(String output, int count) {
        List<String> lines = output.lines().toList();
        return lines.subList(lines.size() - count, lines.size());
    }

    private static void assertContains(String output, String expected) {
        assertTrue(output.contains(expected), () -> "Expected output to contain:\n" + expected + "\nbut was:\n" + output);
    }
}
