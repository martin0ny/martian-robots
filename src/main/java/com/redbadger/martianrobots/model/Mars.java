package com.redbadger.martianrobots.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The shared world that robots explore one after another. Alongside the fixed grid bounds it
 * remembers the "scents" left behind by lost robots, which is the only state that carries over
 * from one robot to the next.
 */
public final class Mars {

    private final Grid grid;
    private final Set<Coordinate> scents = new HashSet<>();

    public Mars(Grid grid) {
        this.grid = Objects.requireNonNull(grid, "grid");
    }

    public boolean isOnGrid(Coordinate coordinate) {
        return grid.contains(coordinate);
    }

    public boolean hasScentAt(Coordinate coordinate) {
        return scents.contains(coordinate);
    }

    public void leaveScentAt(Coordinate coordinate) {
        scents.add(coordinate);
    }
}
