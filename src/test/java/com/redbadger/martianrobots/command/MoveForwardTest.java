package com.redbadger.martianrobots.command;

import com.redbadger.martianrobots.model.Coordinate;
import com.redbadger.martianrobots.model.Grid;
import com.redbadger.martianrobots.model.Mars;
import com.redbadger.martianrobots.model.Orientation;
import com.redbadger.martianrobots.model.Position;
import com.redbadger.martianrobots.model.Robot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoveForwardTest {

    private final MoveForward moveForward = new MoveForward();
    private Mars mars;

    @BeforeEach
    void setUp() {
        mars = new Mars(new Grid(5, 3));
    }

    @Test
    void movesWithinTheGrid() {
        Robot robot = robotAt(1, 1, Orientation.EAST);

        Robot moved = moveForward.execute(robot, mars);

        assertEquals(position(2, 1, Orientation.EAST), moved.position());
        assertFalse(moved.isLost());
    }

    @Test
    void movesOntoTheEdgeWithoutBeingLost() {
        Robot robot = robotAt(3, 2, Orientation.NORTH);

        Robot moved = moveForward.execute(robot, mars);

        assertEquals(position(3, 3, Orientation.NORTH), moved.position());
        assertFalse(moved.isLost());
    }

    @Test
    void steppingOffTheGridLosesTheRobotAndLeavesAScent() {
        Robot robot = robotAt(3, 3, Orientation.NORTH);

        Robot moved = moveForward.execute(robot, mars);

        assertTrue(moved.isLost());
        assertEquals(position(3, 3, Orientation.NORTH), moved.position());
        assertTrue(mars.hasScentAt(new Coordinate(3, 3)));
    }

    @Test
    void ignoresAMoveOffTheGridFromAScentedPosition() {
        mars.leaveScentAt(new Coordinate(3, 3));
        Robot robot = robotAt(3, 3, Orientation.NORTH);

        Robot moved = moveForward.execute(robot, mars);

        assertFalse(moved.isLost());
        assertEquals(robot, moved);
    }

    @Test
    void scentOnlyProtectsTheExactGridPoint() {
        mars.leaveScentAt(new Coordinate(3, 3));
        Robot robot = robotAt(2, 3, Orientation.NORTH);

        Robot moved = moveForward.execute(robot, mars);

        assertTrue(moved.isLost());
    }

    @Test
    void canLeaveTheGridFromBelowTheOrigin() {
        Robot robot = robotAt(0, 0, Orientation.SOUTH);

        Robot moved = moveForward.execute(robot, mars);

        assertTrue(moved.isLost());
        assertEquals(position(0, 0, Orientation.SOUTH), moved.position());
    }

    private static Robot robotAt(int x, int y, Orientation orientation) {
        return Robot.deployedAt(position(x, y, orientation));
    }

    private static Position position(int x, int y, Orientation orientation) {
        return new Position(new Coordinate(x, y), orientation);
    }
}
