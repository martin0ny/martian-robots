package com.redbadger.martianrobots.input;

import com.redbadger.martianrobots.command.Command;
import com.redbadger.martianrobots.command.CommandRegistry;
import com.redbadger.martianrobots.mission.Mission;
import com.redbadger.martianrobots.mission.RobotProgram;
import com.redbadger.martianrobots.model.Coordinate;
import com.redbadger.martianrobots.model.Grid;
import com.redbadger.martianrobots.model.Orientation;
import com.redbadger.martianrobots.model.Position;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Turns the textual transmission from Earth into a {@link Mission}. The expected format is the
 * grid's upper-right corner on the first line, followed by two lines per robot: its starting
 * position and its instructions. Blank lines are ignored so that robots can be visually
 * separated. All validation happens here, up front, so that a bad transmission is rejected before
 * any robot is moved. The individual line parsers are public so that input gathered one answer at
 * a time (see {@code InteractiveSession}) is validated by exactly the same rules as a whole file.
 */
public final class MissionParser {

    /** The specification says instruction strings are "less than 100 characters in length". */
    static final int MAX_INSTRUCTION_LENGTH = 99;

    private final CommandRegistry commands;

    public MissionParser(CommandRegistry commands) {
        this.commands = Objects.requireNonNull(commands, "commands");
    }

    public Mission parse(List<String> lines) {
        Iterator<Line> content = nonBlankLines(lines).iterator();
        if (!content.hasNext()) {
            throw new InvalidInputException(1, "input is empty; expected the grid size");
        }

        Line gridLine = content.next();
        Grid grid = parseGrid(gridLine.text(), gridLine.number());
        List<RobotProgram> robots = new ArrayList<>();
        while (content.hasNext()) {
            Line positionLine = content.next();
            Position start = parseStartPosition(positionLine.text(), grid, positionLine.number());
            if (!content.hasNext()) {
                throw new InvalidInputException(positionLine.number(),
                        "robot has a starting position but no instructions");
            }
            Line instructionLine = content.next();
            robots.add(new RobotProgram(start, parseInstructions(instructionLine.text(), instructionLine.number())));
        }
        return new Mission(grid, robots);
    }

    /**
     * Parses the grid's upper-right corner, e.g. {@code "5 3"}.
     */
    public Grid parseGrid(String text, int lineNumber) {
        String[] tokens = tokens(text);
        if (tokens.length != 2) {
            throw new InvalidInputException(lineNumber,
                    "expected the grid size as \"<x> <y>\" but was \"" + text.strip() + "\"");
        }
        return new Grid(parseCoordinateValue(tokens[0], lineNumber), parseCoordinateValue(tokens[1], lineNumber));
    }

    /**
     * Parses a robot's starting position, e.g. {@code "1 1 E"}, which must lie on {@code grid}.
     */
    public Position parseStartPosition(String text, Grid grid, int lineNumber) {
        String[] tokens = tokens(text);
        if (tokens.length != 3) {
            throw new InvalidInputException(lineNumber,
                    "expected a position as \"<x> <y> <N|E|S|W>\" but was \"" + text.strip() + "\"");
        }

        Coordinate coordinate = new Coordinate(
                parseCoordinateValue(tokens[0], lineNumber),
                parseCoordinateValue(tokens[1], lineNumber));
        if (!grid.contains(coordinate)) {
            throw new InvalidInputException(lineNumber,
                    "starting position " + coordinate + " is outside the grid");
        }
        return new Position(coordinate, parseOrientation(tokens[2], lineNumber));
    }

    /**
     * Parses a robot's instruction string, e.g. {@code "RFRFRFRF"}.
     */
    public List<Command> parseInstructions(String text, int lineNumber) {
        String instructions = text.strip();
        if (instructions.isEmpty()) {
            throw new InvalidInputException(lineNumber, "instructions are missing");
        }
        if (instructions.length() > MAX_INSTRUCTION_LENGTH) {
            throw new InvalidInputException(lineNumber,
                    "instructions must be fewer than " + (MAX_INSTRUCTION_LENGTH + 1)
                            + " characters but were " + instructions.length());
        }

        return IntStream.range(0, instructions.length())
                .mapToObj(index -> {
                    char code = instructions.charAt(index);
                    return commands.find(code).orElseThrow(() -> new InvalidInputException(lineNumber,
                            "unknown instruction '" + code + "' at column " + (index + 1)));
                })
                .toList();
    }

    private static int parseCoordinateValue(String token, int lineNumber) {
        int value;
        try {
            value = Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new InvalidInputException(lineNumber, "\"" + token + "\" is not a whole number");
        }
        if (value < 0 || value > Grid.MAX_COORDINATE) {
            throw new InvalidInputException(lineNumber,
                    "coordinate " + value + " must be between 0 and " + Grid.MAX_COORDINATE);
        }
        return value;
    }

    private static Orientation parseOrientation(String token, int lineNumber) {
        return Orientation.fromSymbol(token.length() == 1 ? token.charAt(0) : '\0')
                .orElseThrow(() -> new InvalidInputException(lineNumber,
                        "\"" + token + "\" is not an orientation; expected one of N, E, S or W"));
    }

    private static String[] tokens(String text) {
        return text.strip().split("\\s+");
    }

    private static List<Line> nonBlankLines(List<String> lines) {
        return IntStream.range(0, lines.size())
                .mapToObj(index -> new Line(index + 1, lines.get(index).strip()))
                .filter(line -> !line.text().isEmpty())
                .toList();
    }

    /** A stripped line of input, remembering where it came from for error reporting. */
    private record Line(int number, String text) {
    }
}
