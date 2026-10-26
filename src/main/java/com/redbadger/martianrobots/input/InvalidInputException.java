package com.redbadger.martianrobots.input;

/**
 * Thrown when the input cannot be turned into a valid mission. The message always names the
 * offending line so that the problem can be fixed without guesswork.
 */
public class InvalidInputException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int lineNumber;

    public InvalidInputException(int lineNumber, String reason) {
        super("Line " + lineNumber + ": " + reason);
        this.lineNumber = lineNumber;
    }

    public int lineNumber() {
        return lineNumber;
    }
}
