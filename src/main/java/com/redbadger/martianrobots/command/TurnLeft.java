package com.redbadger.martianrobots.command;

import com.redbadger.martianrobots.model.Mars;
import com.redbadger.martianrobots.model.Robot;

/**
 * Rotates the robot 90 degrees anti-clockwise without moving it.
 */
public final class TurnLeft implements Command {

    @Override
    public Robot execute(Robot robot, Mars mars) {
        return robot.moveTo(robot.position().turnLeft());
    }
}
