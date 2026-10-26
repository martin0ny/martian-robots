package com.redbadger.martianrobots.command;

import com.redbadger.martianrobots.model.Coordinate;
import com.redbadger.martianrobots.model.Grid;
import com.redbadger.martianrobots.model.Mars;
import com.redbadger.martianrobots.model.Orientation;
import com.redbadger.martianrobots.model.Position;
import com.redbadger.martianrobots.model.Robot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandRegistryTest {

    @Test
    void standardRegistryUnderstandsTheOriginalInstructionSet() {
        CommandRegistry registry = CommandRegistry.standard();

        assertInstanceOf(TurnLeft.class, registry.find('L').orElseThrow());
        assertInstanceOf(TurnRight.class, registry.find('R').orElseThrow());
        assertInstanceOf(MoveForward.class, registry.find('F').orElseThrow());
    }

    @Test
    void unknownCodesAreNotFound() {
        CommandRegistry registry = CommandRegistry.standard();

        assertTrue(registry.find('B').isEmpty());
        assertTrue(registry.find('f').isEmpty());
    }

    @Test
    void canBeExtendedWithNewCommandsWithoutAffectingTheOriginal() {
        Command uTurn = (robot, mars) -> robot.moveTo(robot.position().turnRight().turnRight());
        CommandRegistry standard = CommandRegistry.standard();

        CommandRegistry extended = standard.with('U', uTurn);

        Robot robot = Robot.deployedAt(new Position(new Coordinate(1, 1), Orientation.NORTH));
        Robot turned = extended.find('U').orElseThrow().execute(robot, new Mars(new Grid(5, 3)));
        assertEquals(Orientation.SOUTH, turned.position().orientation());
        assertTrue(standard.find('U').isEmpty());
    }
}
