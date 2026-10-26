package com.redbadger.martianrobots.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarsTest {

    @Test
    void remembersScentsLeftByLostRobots() {
        Mars mars = new Mars(new Grid(5, 3));
        Coordinate edge = new Coordinate(3, 3);

        assertFalse(mars.hasScentAt(edge));

        mars.leaveScentAt(edge);

        assertTrue(mars.hasScentAt(edge));
        assertFalse(mars.hasScentAt(new Coordinate(2, 3)));
    }
}
