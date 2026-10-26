package com.redbadger.martianrobots.command;

import com.redbadger.martianrobots.model.Mars;
import com.redbadger.martianrobots.model.Robot;

/**
 * A single instruction sent from Earth. New instruction types are supported by implementing this
 * interface and registering the implementation with a {@link CommandRegistry}; the parser and
 * simulation need no changes.
 */
@FunctionalInterface
public interface Command {

    /**
     * Applies this command to an operational robot.
     *
     * @return the robot's state after the command, which may be lost
     */
    Robot execute(Robot robot, Mars mars);
}
