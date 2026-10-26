package com.redbadger.martianrobots.mission;

import com.redbadger.martianrobots.command.Command;
import com.redbadger.martianrobots.model.Mars;
import com.redbadger.martianrobots.model.Robot;

import java.util.List;

/**
 * Runs a mission, one robot at a time, against a single shared {@link Mars}.
 */
public final class MissionControl {

    /**
     * Runs every robot in order and returns their final states in the same order. Each run
     * starts with a fresh planet, so scents never leak between missions; within a mission they
     * accumulate, which is what lets later robots avoid earlier robots' fate.
     */
    public List<Robot> run(Mission mission) {
        Mars mars = new Mars(mission.grid());
        return mission.robots().stream()
                .map(program -> runRobot(program, mars))
                .toList();
    }

    private static Robot runRobot(RobotProgram program, Mars mars) {
        Robot robot = Robot.deployedAt(program.start());
        for (Command command : program.commands()) {
            if (robot.isLost()) {
                break;
            }
            robot = command.execute(robot, mars);
        }
        return robot;
    }
}
