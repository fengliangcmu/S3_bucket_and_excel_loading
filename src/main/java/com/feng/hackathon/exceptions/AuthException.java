package com.feng.hackathon.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class AuthException extends CheckedException {
    private static final long serialVersionUID = 7825682239884625945L;

    /**
     * Default constructor.
     */
    public AuthException() {
        this("Generic exception from user authentication");
    }

    /**
     * Creates a new AuthException with an already raised Throwable.
     *
     * @param e - Instance of Throwable with exception or error details.
     */
    public AuthException(Throwable e) {
        this(null, e);
    }

    /**
     * This constructor creates a new AuthException with a custom message, and a
     * Throwable instance.
     *
     * @param message - Custom error or exception message.
     * @param e - Instance of Throwable with exception of error details
     */
    public AuthException(String message, Throwable e) {
        super(e, Response.status(Status.UNAUTHORIZED)
                .entity(message == null ? e.getMessage() : message)
                .type(MediaType.TEXT_PLAIN_TYPE).build());
    }

    /**
     * Throw a DBException with a custom message. 
     *
     * @param message - Custom error message.
     */
    public AuthException(String message) {
        this(message, null);
    }
}
