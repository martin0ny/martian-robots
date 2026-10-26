package com.redbadger.martianrobots;

import com.redbadger.martianrobots.command.Command;
import com.redbadger.martianrobots.input.InvalidInputException;
import com.redbadger.martianrobots.input.MissionParser;
import com.redbadger.martianrobots.mission.Mission;
import com.redbadger.martianrobots.mission.MissionControl;
import com.redbadger.martianrobots.mission.RobotProgram;
import com.redbadger.martianrobots.model.Grid;
import com.redbadger.martianrobots.model.Position;
import com.redbadger.martianrobots.model.Robot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * A prompt-driven way to run a mission, used when the program is started without arguments. The
 * user can either point at an input file or type the mission in one answer at a time. Every
 * answer is validated as soon as it is entered, so a mistake can be corrected on the spot rather
 * than failing the whole mission at the end.
 */
public final class InteractiveSession {

    private static final String MENU_FILE = "1";
    private static final String MENU_GUIDED = "2";

    private final MissionParser parser;
    private final MissionControl missionControl;
    private final BufferedReader in;
    private final PrintStream out;

    public InteractiveSession(MissionParser parser, MissionControl missionControl, BufferedReader in, PrintStream out) {
        this.parser = Objects.requireNonNull(parser, "parser");
        this.missionControl = Objects.requireNonNull(missionControl, "missionControl");
        this.in = Objects.requireNonNull(in, "in");
        this.out = Objects.requireNonNull(out, "out");
    }

    public void run() {
        out.println("Martian Robots");
        out.println();
        out.println("How would you like to provide the mission?");
        out.println("  " + MENU_FILE + ") Load it from a file");
        out.println("  " + MENU_GUIDED + ") Enter it step by step");

        try {
            String choice = ask("Choose " + MENU_FILE + " or " + MENU_GUIDED + ": ", InteractiveSession::menuChoice);
            Mission mission = MENU_FILE.equals(choice) ? loadFromFile() : enterStepByStep();
            report(missionControl.run(mission));
        } catch (EndOfInput e) {
            out.println();
            out.println("No more input received. Exiting.");
        }
    }

    private static String menuChoice(String answer) {
        if (MENU_FILE.equals(answer) || MENU_GUIDED.equals(answer)) {
            return answer;
        }
        throw new InvalidAnswer("Please enter " + MENU_FILE + " or " + MENU_GUIDED + ".");
    }

    private Mission loadFromFile() {
        return ask("Path to the input file: ", this::readMissionFile);
    }

    private Mission readMissionFile(String answer) {
        if (answer.isEmpty()) {
            throw new InvalidAnswer("Please enter a file path.");
        }

        Path path = toPath(answer);
        try {
            return parser.parse(Files.readAllLines(path, StandardCharsets.UTF_8));
        } catch (NoSuchFileException e) {
            throw new InvalidAnswer("File not found: " + path.toAbsolutePath());
        } catch (IOException e) {
            throw new InvalidAnswer("Could not read " + path + ": " + e.getMessage());
        } catch (InvalidInputException e) {
            // Unlike typed answers, the line number is exactly what's needed to fix a file.
            throw new InvalidAnswer("That file is not a valid mission. " + e.getMessage());
        }
    }

    /**
     * Accepts paths the way people tend to paste them into a terminal: wrapped in quotes or
     * starting with {@code ~}.
     */
    private static Path toPath(String answer) {
        String path = answer;
        if (path.length() > 1 && (path.startsWith("\"") && path.endsWith("\"")
                || path.startsWith("'") && path.endsWith("'"))) {
            path = path.substring(1, path.length() - 1);
        }
        if (path.equals("~") || path.startsWith("~/")) {
            path = System.getProperty("user.home") + path.substring(1);
        }

        try {
            return Path.of(path);
        } catch (InvalidPathException e) {
            throw new InvalidAnswer("\"" + answer + "\" is not a valid file path.");
        }
    }

    private Mission enterStepByStep() {
        out.println();
        out.println("Enter the mission one line at a time.");
        out.println("Orientations are N, E, S or W. Instructions are L (turn left), R (turn right) and F (forward).");
        out.println();

        // Line numbers mirror where each answer would sit in an input file. They are never shown
        // here, but keep the parser's error reporting consistent across both ways of entering input.
        Grid grid = ask("Grid size, as the upper-right corner (e.g. 5 3): ", text -> parser.parseGrid(text, 1));

        List<RobotProgram> robots = new ArrayList<>();
        while (true) {
            int robotNumber = robots.size() + 1;
            int positionLine = robotNumber * 2;

            Optional<Position> start = ask(
                    "Robot " + robotNumber + " start position (e.g. 1 1 E), or press Enter to finish: ",
                    text -> text.isEmpty()
                            ? Optional.empty()
                            : Optional.of(parser.parseStartPosition(text, grid, positionLine)));
            if (start.isEmpty()) {
                return new Mission(grid, robots);
            }

            List<Command> commands = ask(
                    "Robot " + robotNumber + " instructions (e.g. RFRFRFRF): ",
                    text -> parser.parseInstructions(text, positionLine + 1));
            robots.add(new RobotProgram(start.get(), commands));
        }
    }

    private void report(List<Robot> robots) {
        out.println();
        if (robots.isEmpty()) {
            out.println("No robots were deployed, so there is nothing to report.");
            return;
        }
        out.println("Final positions:");
        robots.forEach(robot -> out.println(robot.report()));
    }

    /**
     * Keeps asking until {@code interpret} accepts the answer, explaining what was wrong each time.
     */
    private <T> T ask(String prompt, Function<String, T> interpret) {
        while (true) {
            out.print(prompt);
            out.flush();
            String answer = readLine().strip();
            try {
                return interpret.apply(answer);
            } catch (InvalidInputException e) {
                out.println("  Invalid input: " + e.reason() + ". Please try again.");
            } catch (InvalidAnswer e) {
                out.println("  " + e.getMessage());
            }
        }
    }

    private String readLine() {
        try {
            String line = in.readLine();
            if (line == null) {
                throw new EndOfInput();
            }
            return line;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** An answer that can't be used, with a message explaining what to do instead. */
    private static final class InvalidAnswer extends RuntimeException {

        private static final long serialVersionUID = 1L;

        InvalidAnswer(String message) {
            super(message);
        }
    }

    /** The user closed the input (e.g. Ctrl+D) before the session was finished. */
    private static final class EndOfInput extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
}
