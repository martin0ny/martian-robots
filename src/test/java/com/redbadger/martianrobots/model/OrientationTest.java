package com.redbadger.martianrobots.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrientationTest {

    @ParameterizedTest
    @CsvSource({
            "NORTH, WEST",
            "WEST, SOUTH",
            "SOUTH, EAST",
            "EAST, NORTH"
    })
    void turningLeftRotatesAntiClockwise(Orientation start, Orientation expected) {
        assertEquals(expected, start.left());
    }

    @ParameterizedTest
    @CsvSource({
            "NORTH, EAST",
            "EAST, SOUTH",
            "SOUTH, WEST",
            "WEST, NORTH"
    })
    void turningRightRotatesClockwise(Orientation start, Orientation expected) {
        assertEquals(expected, start.right());
    }

    @ParameterizedTest
    @EnumSource(Orientation.class)
    void fourTurnsInEitherDirectionReturnToTheStart(Orientation start) {
        assertEquals(start, start.left().left().left().left());
        assertEquals(start, start.right().right().right().right());
    }

    @ParameterizedTest
    @EnumSource(Orientation.class)
    void symbolRoundTrips(Orientation orientation) {
        assertEquals(orientation, Orientation.fromSymbol(orientation.symbol()).orElseThrow());
    }

    @Test
    void unknownSymbolIsNotAnOrientation() {
        assertTrue(Orientation.fromSymbol('X').isEmpty());
        assertTrue(Orientation.fromSymbol('n').isEmpty());
    }
}
