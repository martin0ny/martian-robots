package com.redbadger.martianrobots.mission;

import com.redbadger.martianrobots.command.Command;
import com.redbadger.martianrobots.command.MoveForward;
import com.redbadger.martianrobots.command.TurnLeft;
import com.redbadger.martianrobots.command.TurnRight;
import com.redbadger.martianrobots.model.Coordinate;
import com.redbadger.martianrobots.model.Grid;
import com.redbadger.martianrobots.model.Orientation;
import com.redbadger.martianrobots.model.Position;
import com.redbadger.martianrobots.model.Robot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MissionControlTest {

    private static final Command F = new MoveForward();
    private static final Command L = new TurnLeft();
    private static final Command R = new TurnRight();

    private final MissionControl missionControl = new MissionControl();

    @Test
    void robotWithNoCommandsStaysWhereItLanded() {
        Mission mission = new Mission(new Grid(5, 3), List.of(program(1, 1, Orientation.EAST)));

        List<Robot> robots = missionControl.run(mission);

        assertEquals(List.of("1 1 E"), reports(robots));
    }

    @Test
    void lostRobotIgnoresRemainingCommands() {
        // Without the early exit the trailing turns would be applied to a lost robot.
        Mission mission = new Mission(new Grid(1, 1), List.of(
                program(1, 1, Orientation.NORTH, F, L, L)));

        List<Robot> robots = missionControl.run(mission);

        assertEquals(List.of("1 1 N LOST"), reports(robots));
    }

    @Test
    void laterRobotIsSavedByTheScentOfAnEarlierOne() {
        Mission mission = new Mission(new Grid(1, 1), List.of(
                program(1, 1, Orientation.NORTH, F),
                program(1, 1, Orientation.NORTH, F, L, F)));

        List<Robot> robots = missionControl.run(mission);

        assertEquals(List.of("1 1 N LOST", "0 1 W"), reports(robots));
    }

    @Test
    void scentProtectsAGridPointWhicheverWayTheRobotFaces() {
        // The spec ties a scent to a grid point rather than a direction, so a robot lost to the
        // north of a corner also stops later robots leaving that corner to the east.
        Mission mission = new Mission(new Grid(1, 1), List.of(
                program(1, 1, Orientation.NORTH, F),
                program(1, 1, Orientation.EAST, F)));

        List<Robot> robots = missionControl.run(mission);

        assertEquals(List.of("1 1 N LOST", "1 1 E"), reports(robots));
    }

    @Test
    void scentsDoNotCarryOverBetweenMissions() {
        Mission mission = new Mission(new Grid(1, 1), List.of(program(1, 1, Orientation.NORTH, F)));

        missionControl.run(mission);
        List<Robot> secondRun = missionControl.run(mission);

        assertTrue(secondRun.get(0).isLost());
    }

    private static RobotProgram program(int x, int y, Orientation orientation, Command... commands) {
        return new RobotProgram(new Position(new Coordinate(x, y), orientation), List.of(commands));
    }

    private static List<String> reports(List<Robot> robots) {
        return robots.stream().map(Robot::report).toList();
    }
}
