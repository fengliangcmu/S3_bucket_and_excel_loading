package com.feng.hackathon.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class CheckedException extends WebApplicationException {

    private static final long serialVersionUID = -7161834422688964657L;

    public CheckedException(final Throwable cause, final Response response) {
        super(cause, response);
    }
}
