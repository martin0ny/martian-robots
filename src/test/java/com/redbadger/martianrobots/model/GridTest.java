package com.redbadger.martianrobots.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridTest {

    private final Grid grid = new Grid(5, 3);

    @ParameterizedTest
    @CsvSource({"0, 0", "5, 3", "0, 3", "5, 0", "2, 2"})
    void containsPointsInsideAndOnTheEdges(int x, int y) {
        assertTrue(grid.contains(new Coordinate(x, y)));
    }

    @ParameterizedTest
    @CsvSource({"-1, 0", "0, -1", "6, 3", "5, 4"})
    void doesNotContainPointsBeyondTheEdges(int x, int y) {
        assertFalse(grid.contains(new Coordinate(x, y)));
    }

    @Test
    void acceptsTheLargestAllowedGrid() {
        assertDoesNotThrow(() -> new Grid(Grid.MAX_COORDINATE, Grid.MAX_COORDINATE));
    }

    @ParameterizedTest
    @CsvSource({"-1, 3", "5, -1", "51, 3", "5, 51"})
    void rejectsBoundsOutsideTheAllowedRange(int maxX, int maxY) {
        assertThrows(IllegalArgumentException.class, () -> new Grid(maxX, maxY));
    }
}
