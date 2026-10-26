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
 * Command-line entry point. With no arguments it starts an {@link InteractiveSession}; with a
 * file path it reads the mission from that file; with {@code -} it reads the mission from
 * standard input so it can be piped in. The file and standard input modes print one line per
 * robot and nothing else, which keeps them script-friendly.
 */
public final class MartianRobotsApp {

    private static final String STDIN_ARGUMENT = "-";
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
            System.err.println("Usage: java -jar martian-robots.jar [input-file | -]");
            System.exit(EXIT_USAGE);
        }

        MissionParser parser = new MissionParser(CommandRegistry.standard());
        MissionControl missionControl = new MissionControl();

        if (args.length == 0) {
            new InteractiveSession(parser, missionControl, standardInput(), System.out).run();
            return;
        }

        MartianRobotsApp app = new MartianRobotsApp(parser, missionControl);
        try {
            app.run(readInput(args[0])).forEach(System.out::println);
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

    private static List<String> readInput(String source) throws IOException {
        if (STDIN_ARGUMENT.equals(source)) {
            try (BufferedReader reader = standardInput()) {
                return reader.lines().toList();
            }
        }
        return Files.readAllLines(Path.of(source), StandardCharsets.UTF_8);
    }

    private static BufferedReader standardInput() {
        return new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    }
}
