package com.feng.hackathon.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.feng.hackathon.exceptions.CheckedException;
import com.feng.hackathon.exceptions.ServiceException;
import com.feng.hackathon.utils.Log;

public class BaseService {

    protected void handleException(Exception e) {
        Log.error(e);
        if (e instanceof CheckedException) {
            throw (CheckedException) e;
        } else {
            throw new ServiceException(e);
        }
    }

    protected Response ok() {
        return Response.status(Status.OK).build();
    }

    protected Response ok(Object obj) {
        return Response.status(Status.OK).entity(obj).build();
    }

    protected Response created(Object obj) {
        return Response.status(Status.CREATED).entity(obj).build();
    }
}
