package com.redbadger.martianrobots.input;

import com.redbadger.martianrobots.command.CommandRegistry;
import com.redbadger.martianrobots.command.MoveForward;
import com.redbadger.martianrobots.command.TurnLeft;
import com.redbadger.martianrobots.command.TurnRight;
import com.redbadger.martianrobots.mission.Mission;
import com.redbadger.martianrobots.mission.RobotProgram;
import com.redbadger.martianrobots.model.Coordinate;
import com.redbadger.martianrobots.model.Grid;
import com.redbadger.martianrobots.model.Orientation;
import com.redbadger.martianrobots.model.Position;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MissionParserTest {

    private final MissionParser parser = new MissionParser(CommandRegistry.standard());

    @Nested
    class ValidInput {

        @Test
        void parsesGridAndRobots() {
            Mission mission = parser.parse(List.of(
                    "5 3",
                    "1 1 E",
                    "RFL",
                    "3 2 N",
                    "F"));

            assertEquals(new Grid(5, 3), mission.grid());
            assertEquals(2, mission.robots().size());

            RobotProgram first = mission.robots().get(0);
            assertEquals(new Position(new Coordinate(1, 1), Orientation.EAST), first.start());
            assertEquals(3, first.commands().size());
            assertInstanceOf(TurnRight.class, first.commands().get(0));
            assertInstanceOf(MoveForward.class, first.commands().get(1));
            assertInstanceOf(TurnLeft.class, first.commands().get(2));

            RobotProgram second = mission.robots().get(1);
            assertEquals(new Position(new Coordinate(3, 2), Orientation.NORTH), second.start());
        }

        @Test
        void gridWithNoRobotsIsAValidMission() {
            Mission mission = parser.parse(List.of("5 3"));

            assertTrue(mission.robots().isEmpty());
        }

        @Test
        void toleratesBlankLinesAndSurroundingWhitespace() {
            Mission mission = parser.parse(List.of(
                    "  5   3 ",
                    "",
                    "1\t1  E",
                    "  RFRF  ",
                    "",
                    ""));

            assertEquals(new Grid(5, 3), mission.grid());
            assertEquals(1, mission.robots().size());
            assertEquals(4, mission.robots().get(0).commands().size());
        }

        @Test
        void acceptsTheLongestAllowedInstructionString() {
            String instructions = "L".repeat(MissionParser.MAX_INSTRUCTION_LENGTH);

            Mission mission = parser.parse(List.of("5 3", "1 1 E", instructions));

            assertEquals(MissionParser.MAX_INSTRUCTION_LENGTH, mission.robots().get(0).commands().size());
        }

        @Test
        void understandsCommandsAddedToTheRegistry() {
            MissionParser extended = new MissionParser(
                    CommandRegistry.standard().with('B', (robot, mars) -> robot));

            Mission mission = extended.parse(List.of("5 3", "1 1 E", "FB"));

            assertEquals(2, mission.robots().get(0).commands().size());
        }
    }

    @Nested
    class InvalidInput {

        @Test
        void rejectsEmptyInput() {
            assertRejected(List.of("", "  "), 1, "input is empty");
        }

        @ParameterizedTest
        @ValueSource(strings = {"5", "5 3 1", "five 3", "5 -1", "51 3"})
        void rejectsMalformedOrOutOfRangeGrid(String gridLine) {
            InvalidInputException error = assertThrows(InvalidInputException.class,
                    () -> parser.parse(List.of(gridLine)));

            assertEquals(1, error.lineNumber());
        }

        @ParameterizedTest
        @ValueSource(strings = {"1 1", "1 1 E E", "a 1 E", "1 1 X", "1 1 north", "1 1 n"})
        void rejectsMalformedPosition(String positionLine) {
            InvalidInputException error = assertThrows(InvalidInputException.class,
                    () -> parser.parse(List.of("5 3", positionLine, "F")));

            assertEquals(2, error.lineNumber());
        }

        @Test
        void rejectsStartingPositionOutsideTheGrid() {
            assertRejected(List.of("5 3", "6 1 N", "F"), 2, "outside the grid");
        }

        @Test
        void rejectsRobotWithoutInstructions() {
            assertRejected(List.of("5 3", "1 1 E", "RF", "2 2 N"), 4, "no instructions");
        }

        @Test
        void rejectsUnknownInstruction() {
            assertRejected(List.of("5 3", "1 1 E", "FFXF"), 3, "unknown instruction 'X' at column 3");
        }

        @Test
        void rejectsLowercaseInstructions() {
            assertRejected(List.of("5 3", "1 1 E", "f"), 3, "unknown instruction 'f'");
        }

        @Test
        void rejectsInstructionsThatAreTooLong() {
            String instructions = "F".repeat(MissionParser.MAX_INSTRUCTION_LENGTH + 1);

            assertRejected(List.of("5 3", "1 1 E", instructions), 3, "fewer than 100 characters");
        }

        @Test
        void reportsTheOriginalLineNumberWhenBlankLinesAreSkipped() {
            assertRejected(List.of("5 3", "", "", "1 1 Q", "F"), 4, "not an orientation");
        }

        private void assertRejected(List<String> lines, int expectedLine, String expectedReason) {
            InvalidInputException error = assertThrows(InvalidInputException.class, () -> parser.parse(lines));

            assertEquals(expectedLine, error.lineNumber());
            assertTrue(error.getMessage().contains(expectedReason),
                    () -> "Expected message to contain \"" + expectedReason + "\" but was: " + error.getMessage());
        }
    }
}
