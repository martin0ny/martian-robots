package com.redbadger.martianrobots.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PositionTest {

    @ParameterizedTest
    @CsvSource({
            "NORTH, 2, 3",
            "EAST,  3, 2",
            "SOUTH, 2, 1",
            "WEST,  1, 2"
    })
    void forwardMovesOneStepInTheDirectionFaced(Orientation facing, int expectedX, int expectedY) {
        Position start = new Position(new Coordinate(2, 2), facing);

        Position moved = start.forward();

        assertEquals(new Position(new Coordinate(expectedX, expectedY), facing), moved);
    }

    @Test
    void turningChangesOrientationButNotCoordinate() {
        Position start = new Position(new Coordinate(1, 1), Orientation.NORTH);

        assertEquals(new Position(new Coordinate(1, 1), Orientation.WEST), start.turnLeft());
        assertEquals(new Position(new Coordinate(1, 1), Orientation.EAST), start.turnRight());
    }

    @Test
    void rendersInTheOutputFormat() {
        Position position = new Position(new Coordinate(3, 3), Orientation.NORTH);

        assertEquals("3 3 N", position.toString());
    }
}
