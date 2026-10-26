package com.redbadger.martianrobots;

import com.redbadger.martianrobots.command.CommandRegistry;
import com.redbadger.martianrobots.input.InvalidInputException;
import com.redbadger.martianrobots.input.MissionParser;
import com.redbadger.martianrobots.mission.MissionControl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * End-to-end tests that drive the whole pipeline from input text to report lines.
 */
class MartianRobotsAppTest {

    private final MartianRobotsApp app = new MartianRobotsApp(
            new MissionParser(CommandRegistry.standard()),
            new MissionControl());

    @Test
    void producesTheSampleOutputFromTheSpecification() {
        List<String> input = List.of(
                "5 3",
                "1 1 E",
                "RFRFRFRF",
                "3 2 N",
                "FRRFLLFFRRFLL",
                "0 3 W",
                "LLFFFLFLFL");

        List<String> output = app.run(input);

        assertEquals(List.of(
                "1 1 E",
                "3 3 N LOST",
                "2 3 S"), output);
    }

    @Test
    void sampleFileShippedWithTheProjectProducesTheExpectedOutput() throws IOException {
        List<String> input = Files.readAllLines(Path.of("samples", "sample-input.txt"));

        assertEquals(List.of("1 1 E", "3 3 N LOST", "2 3 S"), app.run(input));
    }

    @Test
    void robotsLostFromDifferentEdgesEachLeaveTheirOwnScent() {
        List<String> input = List.of(
                "2 2",
                "0 0 S", "F",       // lost off the bottom edge
                "2 2 E", "F",       // lost off the right edge
                "0 0 S", "FLF",     // saved at (0, 0), then carries on
                "2 2 E", "FRF");    // saved at (2, 2), then carries on

        assertEquals(List.of(
                "0 0 S LOST",
                "2 2 E LOST",
                "1 0 E",
                "2 1 S"), app.run(input));
    }

    @Test
    void robotsOnASinglePointGridCanOnlyTurn() {
        List<String> input = List.of(
                "0 0",
                "0 0 N", "F",
                "0 0 N", "FRFRFRF");

        assertEquals(List.of("0 0 N LOST", "0 0 W"), app.run(input));
    }

    @Test
    void invalidInputIsRejectedBeforeAnyRobotMoves() {
        List<String> input = List.of(
                "5 3",
                "1 1 E", "F",
                "9 9 N", "F");

        assertThrows(InvalidInputException.class, () -> app.run(input));
    }
}
