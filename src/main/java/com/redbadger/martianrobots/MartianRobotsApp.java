package com.redbadger.martianrobots;

import com.redbadger.martianrobots.command.CommandRegistry;
import com.redbadger.martianrobots.input.InvalidInputException;
import com.redbadger.martianrobots.input.MissionParser;
import com.redbadger.martianrobots.mission.MissionControl;
import com.redbadger.martianrobots.model.Robot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Command-line entry point. Reads a mission from the file given as the only argument, or from
 * standard input when no argument is given, and prints one line per robot.
 */
public final class MartianRobotsApp {

    private static final int EXIT_INVALID_INPUT = 1;
    private static final int EXIT_USAGE = 2;

    private final MissionParser parser;
    private final MissionControl missionControl;

    public MartianRobotsApp(MissionParser parser, MissionControl missionControl) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.missionControl = Objects.requireNonNull(missionControl, "missionControl");
    }

    /**
     * Runs a complete mission from raw input lines to the report lines sent back to Earth.
     */
    public List<String> run(List<String> inputLines) {
        return missionControl.run(parser.parse(inputLines)).stream()
                .map(Robot::report)
                .toList();
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage: java -jar martian-robots.jar [input-file]");
            System.exit(EXIT_USAGE);
        }

        MartianRobotsApp app = new MartianRobotsApp(
                new MissionParser(CommandRegistry.standard()),
                new MissionControl());

        try {
            app.run(readInput(args)).forEach(System.out::println);
        } catch (InvalidInputException e) {
            System.err.println("Invalid input. " + e.getMessage());
            System.exit(EXIT_INVALID_INPUT);
        } catch (NoSuchFileException e) {
            System.err.println("Input file not found: " + e.getFile());
            System.exit(EXIT_INVALID_INPUT);
        } catch (IOException e) {
            System.err.println("Could not read input: " + e.getMessage());
            System.exit(EXIT_INVALID_INPUT);
        }
    }

    private static List<String> readInput(String[] args) throws IOException {
        if (args.length == 1) {
            return Files.readAllLines(Path.of(args[0]), StandardCharsets.UTF_8);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            return reader.lines().toList();
        }
    }
}
