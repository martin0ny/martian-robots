package com.redbadger.martianrobots.model;

/**
 * The bounds of the explorable surface. The lower-left corner is always {@code (0, 0)}; the
 * upper-right corner is supplied by mission control.
 */
public record Grid(int maxX, int maxY) {

    public static final int MAX_COORDINATE = 50;

    public Grid {
        requireWithinLimits("maxX", maxX);
        requireWithinLimits("maxY", maxY);
    }

    public boolean contains(Coordinate coordinate) {
        return coordinate.x() >= 0 && coordinate.x() <= maxX
                && coordinate.y() >= 0 && coordinate.y() <= maxY;
    }

    private static void requireWithinLimits(String name, int value) {
        if (value < 0 || value > MAX_COORDINATE) {
            throw new IllegalArgumentException(
                    "%s must be between 0 and %d but was %d".formatted(name, MAX_COORDINATE, value));
        }
    }
}
