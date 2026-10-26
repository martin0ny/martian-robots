package com.redbadger.martianrobots.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Maps the single-letter instruction codes used in the input to their {@link Command}s.
 */
public final class CommandRegistry {

    private final Map<Character, Command> commands;

    private CommandRegistry(Map<Character, Command> commands) {
        this.commands = Map.copyOf(commands);
    }

    /**
     * The instruction set described in the original specification: {@code L}, {@code R} and
     * {@code F}.
     */
    public static CommandRegistry standard() {
        return new CommandRegistry(Map.of(
                'L', new TurnLeft(),
                'R', new TurnRight(),
                'F', new MoveForward()));
    }

    /**
     * Returns a new registry that additionally understands {@code code}. Existing codes may be
     * overridden, which is handy for testing.
     */
    public CommandRegistry with(char code, Command command) {
        Map<Character, Command> extended = new HashMap<>(commands);
        extended.put(code, Objects.requireNonNull(command, "command"));
        return new CommandRegistry(extended);
    }

    public Optional<Command> find(char code) {
        return Optional.ofNullable(commands.get(code));
    }
}
