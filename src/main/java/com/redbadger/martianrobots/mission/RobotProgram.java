package com.redbadger.martianrobots.mission;

import com.redbadger.martianrobots.command.Command;
import com.redbadger.martianrobots.model.Position;

import java.util.List;
import java.util.Objects;

/**
 * Where a robot is dropped and the commands it should carry out once it lands.
 */
public record RobotProgram(Position start, List<Command> commands) {

    public RobotProgram {
        Objects.requireNonNull(start, "start");
        commands = List.copyOf(commands);
    }
}
