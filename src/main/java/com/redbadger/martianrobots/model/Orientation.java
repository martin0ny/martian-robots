package com.redbadger.martianrobots.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * The four compass points a robot can face. Constants are declared in clockwise order, which
 * lets turning be expressed as stepping through {@link #values()} rather than a hand-written
 * lookup table.
 */
public enum Orientation {
    NORTH('N', 0, 1),
    EAST('E', 1, 0),
    SOUTH('S', 0, -1),
    WEST('W', -1, 0);

    private static final Orientation[] CLOCKWISE = values();

    private final char symbol;
    private final int dx;
    private final int dy;

    Orientation(char symbol, int dx, int dy) {
        this.symbol = symbol;
        this.dx = dx;
        this.dy = dy;
    }

    public static Optional<Orientation> fromSymbol(char symbol) {
        return Arrays.stream(CLOCKWISE)
                .filter(orientation -> orientation.symbol == symbol)
                .findFirst();
    }

    public Orientation left() {
        return CLOCKWISE[(ordinal() + CLOCKWISE.length - 1) % CLOCKWISE.length];
    }

    public Orientation right() {
        return CLOCKWISE[(ordinal() + 1) % CLOCKWISE.length];
    }

    public char symbol() {
        return symbol;
    }

    int dx() {
        return dx;
    }

    int dy() {
        return dy;
    }
}
