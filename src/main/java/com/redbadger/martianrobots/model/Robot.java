package com.redbadger.martianrobots.model;

import java.util.Objects;

/**
 * An immutable snapshot of a robot. A lost robot keeps the last position it held on the grid,
 * because that is what gets reported back to Earth.
 */
public record Robot(Position position, boolean isLost) {

    private static final String LOST_SUFFIX = " LOST";

    public Robot {
        Objects.requireNonNull(position, "position");
    }

    public static Robot deployedAt(Position position) {
        return new Robot(position, false);
    }

    public Robot moveTo(Position newPosition) {
        requireOperational();
        return new Robot(newPosition, false);
    }

    public Robot markLost() {
        requireOperational();
        return new Robot(position, true);
    }

    /**
     * Formats the robot's final state as expected in the program output, e.g. {@code 3 3 N LOST}.
     */
    public String report() {
        return isLost ? position + LOST_SUFFIX : position.toString();
    }

    private void requireOperational() {
        if (isLost) {
            throw new IllegalStateException("Robot at " + position + " is already lost");
        }
    }
}
