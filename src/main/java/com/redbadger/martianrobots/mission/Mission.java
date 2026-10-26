package com.redbadger.martianrobots.mission;

import com.redbadger.martianrobots.model.Grid;

import java.util.List;
import java.util.Objects;

/**
 * Everything sent from Earth in a single transmission: the grid and the robots to run on it,
 * in the order they should be run.
 */
public record Mission(Grid grid, List<RobotProgram> robots) {

    public Mission {
        Objects.requireNonNull(grid, "grid");
        robots = List.copyOf(robots);
    }
}
