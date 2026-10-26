package com.redbadger.martianrobots.input;

/**
 * Thrown when the input cannot be turned into a valid mission. The message always names the
 * offending line so that the problem can be fixed without guesswork.
 */
public class InvalidInputException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int lineNumber;
    private final String reason;

    public InvalidInputException(int lineNumber, String reason) {
        super("Line " + lineNumber + ": " + reason);
        this.lineNumber = lineNumber;
        this.reason = reason;
    }

    public int lineNumber() {
        return lineNumber;
    }

    /**
     * The problem on its own, without the line number. Useful when the input is being typed in
     * one answer at a time and line numbers would mean nothing to the user.
     */
    public String reason() {
        return reason;
    }
}
