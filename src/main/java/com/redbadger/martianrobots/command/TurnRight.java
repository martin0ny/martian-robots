package com.redbadger.martianrobots.command;

import com.redbadger.martianrobots.model.Mars;
import com.redbadger.martianrobots.model.Robot;

/**
 * Rotates the robot 90 degrees clockwise without moving it.
 */
public final class TurnRight implements Command {

    @Override
    public Robot execute(Robot robot, Mars mars) {
        return robot.moveTo(robot.position().turnRight());
    }
}
