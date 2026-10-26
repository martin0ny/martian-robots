package com.redbadger.martianrobots.command;

import com.redbadger.martianrobots.model.Coordinate;
import com.redbadger.martianrobots.model.Mars;
import com.redbadger.martianrobots.model.Position;
import com.redbadger.martianrobots.model.Robot;

/**
 * Moves the robot one grid point in the direction it is facing. Stepping off the grid loses the
 * robot and leaves a scent at its last position. If a previous robot has already been lost from
 * that position, the move is ignored instead.
 */
public final class MoveForward implements Command {

    @Override
    public Robot execute(Robot robot, Mars mars) {
        Position next = robot.position().forward();
        if (mars.isOnGrid(next.coordinate())) {
            return robot.moveTo(next);
        }

        Coordinate current = robot.position().coordinate();
        if (mars.hasScentAt(current)) {
            return robot;
        }

        mars.leaveScentAt(current);
        return robot.markLost();
    }
}
