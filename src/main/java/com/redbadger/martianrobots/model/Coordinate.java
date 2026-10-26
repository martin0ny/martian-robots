package com.redbadger.martianrobots.model;

/**
 * A point on the Martian grid. Coordinates are deliberately unbounded so that a robot can
 * "look" one step past the edge before deciding whether the move is safe.
 */
public record Coordinate(int x, int y) {

    public Coordinate step(Orientation orientation) {
        return new Coordinate(x + orientation.dx(), y + orientation.dy());
    }

    @Override
    public String toString() {
        return x + " " + y;
    }
}
