package com.redbadger.martianrobots.command;

import com.redbadger.martianrobots.model.Coordinate;
import com.redbadger.martianrobots.model.Grid;
import com.redbadger.martianrobots.model.Mars;
import com.redbadger.martianrobots.model.Orientation;
import com.redbadger.martianrobots.model.Position;
import com.redbadger.martianrobots.model.Robot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TurnCommandsTest {

    private final Mars mars = new Mars(new Grid(5, 3));
    private final Robot robot = Robot.deployedAt(new Position(new Coordinate(1, 1), Orientation.NORTH));

    @Test
    void turnLeftFacesWestFromNorth() {
        Robot turned = new TurnLeft().execute(robot, mars);

        assertEquals(new Position(new Coordinate(1, 1), Orientation.WEST), turned.position());
    }

    @Test
    void turnRightFacesEastFromNorth() {
        Robot turned = new TurnRight().execute(robot, mars);

        assertEquals(new Position(new Coordinate(1, 1), Orientation.EAST), turned.position());
    }
}
