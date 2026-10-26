package com.redbadger.martianrobots.model;

import java.util.Objects;

/**
 * Where a robot is and which way it is facing.
 */
public record Position(Coordinate coordinate, Orientation orientation) {

    public Position {
        Objects.requireNonNull(coordinate, "coordinate");
        Objects.requireNonNull(orientation, "orientation");
    }

    public Position turnLeft() {
        return new Position(coordinate, orientation.left());
    }

    public Position turnRight() {
        return new Position(coordinate, orientation.right());
    }

    public Position forward() {
        return new Position(coordinate.step(orientation), orientation);
    }

    @Override
    public String toString() {
        return coordinate + " " + orientation.symbol();
    }
}
