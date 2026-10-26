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
 * any robot is moved.
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

        Grid grid = parseGrid(content.next());
        List<RobotProgram> robots = new ArrayList<>();
        while (content.hasNext()) {
            Line positionLine = content.next();
            Position start = parseStartPosition(positionLine, grid);
            if (!content.hasNext()) {
                throw new InvalidInputException(positionLine.number(),
                        "robot has a starting position but no instructions");
            }
            robots.add(new RobotProgram(start, parseCommands(content.next())));
        }
        return new Mission(grid, robots);
    }

    private static Grid parseGrid(Line line) {
        String[] tokens = line.tokens();
        if (tokens.length != 2) {
            throw new InvalidInputException(line.number(),
                    "expected the grid size as \"<x> <y>\" but was \"" + line.text() + "\"");
        }
        return new Grid(parseCoordinateValue(line, tokens[0]), parseCoordinateValue(line, tokens[1]));
    }

    private static Position parseStartPosition(Line line, Grid grid) {
        String[] tokens = line.tokens();
        if (tokens.length != 3) {
            throw new InvalidInputException(line.number(),
                    "expected a position as \"<x> <y> <N|E|S|W>\" but was \"" + line.text() + "\"");
        }

        Coordinate coordinate = new Coordinate(
                parseCoordinateValue(line, tokens[0]),
                parseCoordinateValue(line, tokens[1]));
        if (!grid.contains(coordinate)) {
            throw new InvalidInputException(line.number(),
                    "starting position " + coordinate + " is outside the grid");
        }
        return new Position(coordinate, parseOrientation(line, tokens[2]));
    }

    private static int parseCoordinateValue(Line line, String token) {
        int value;
        try {
            value = Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new InvalidInputException(line.number(), "\"" + token + "\" is not a whole number");
        }
        if (value < 0 || value > Grid.MAX_COORDINATE) {
            throw new InvalidInputException(line.number(),
                    "coordinate " + value + " must be between 0 and " + Grid.MAX_COORDINATE);
        }
        return value;
    }

    private static Orientation parseOrientation(Line line, String token) {
        return Orientation.fromSymbol(token.length() == 1 ? token.charAt(0) : '\0')
                .orElseThrow(() -> new InvalidInputException(line.number(),
                        "\"" + token + "\" is not an orientation; expected one of N, E, S or W"));
    }

    private List<Command> parseCommands(Line line) {
        String instructions = line.text();
        if (instructions.length() > MAX_INSTRUCTION_LENGTH) {
            throw new InvalidInputException(line.number(),
                    "instructions must be fewer than " + (MAX_INSTRUCTION_LENGTH + 1)
                            + " characters but were " + instructions.length());
        }

        return IntStream.range(0, instructions.length())
                .mapToObj(index -> {
                    char code = instructions.charAt(index);
                    return commands.find(code).orElseThrow(() -> new InvalidInputException(line.number(),
                            "unknown instruction '" + code + "' at column " + (index + 1)));
                })
                .toList();
    }

    private static List<Line> nonBlankLines(List<String> lines) {
        return IntStream.range(0, lines.size())
                .mapToObj(index -> new Line(index + 1, lines.get(index).strip()))
                .filter(line -> !line.text().isEmpty())
                .toList();
    }

    /** A stripped line of input, remembering where it came from for error reporting. */
    private record Line(int number, String text) {

        String[] tokens() {
            return text.split("\\s+");
        }
    }
}
