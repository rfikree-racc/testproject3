package org.ozsoft.secs;

/**
 * Generic, top-level SECS exception.
 * 
 * @author Oscar Stigter
 */
public class SecsException extends Exception {

    private static final long serialVersionUID = -6343582345850766217L;

    /**
     * Constructor with a message only.
     * 
     * @param message
     *            Message describing the problem.
     */
    public SecsException(String message) {
        super(message);
    }

    /**
     * Constructor with a message and an inner exception as cause.
     * 
     * @param message
     *            Message describing the problem.
     * @param cause
     *            Inner exception with the actual cause of the problem.
     */
    public SecsException(String message, Throwable cause) {
        super(message, cause);
    }

}
