package com.redbadger.martianrobots.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RobotTest {

    private static final Position START = new Position(new Coordinate(3, 3), Orientation.NORTH);

    @Test
    void newlyDeployedRobotIsNotLost() {
        Robot robot = Robot.deployedAt(START);

        assertFalse(robot.isLost());
        assertEquals("3 3 N", robot.report());
    }

    @Test
    void lostRobotReportsItsLastKnownPosition() {
        Robot robot = Robot.deployedAt(START).markLost();

        assertTrue(robot.isLost());
        assertEquals("3 3 N LOST", robot.report());
    }

    @Test
    void lostRobotCannotBeMoved() {
        Robot lost = Robot.deployedAt(START).markLost();

        assertThrows(IllegalStateException.class, () -> lost.moveTo(START.forward()));
        assertThrows(IllegalStateException.class, lost::markLost);
    }
}
