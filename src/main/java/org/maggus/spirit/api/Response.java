package org.maggus.spirit.api;

import lombok.Data;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
@Data
public class Response {
    private int status;
    private String type;
    private String message;
    private Object data;
    private Object metaData;

    public Response(Object data) {
        this.status = javax.ws.rs.core.Response.Status.OK.getStatusCode();
        this.data = data;
    }

    public Response(javax.ws.rs.core.Response.Status status, String type, String message) {
        this.status = status.getStatusCode();
        this.type = type;
        this.message = message;
    }

    public static Response fail(Exception ex) {
        StackTraceElement ste = ex.getStackTrace()[0];
        log.log(Level.WARNING, "Responding with error from: " + ste.getClassName() + "." + ste.getMethodName(), ex);
        return new Response(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, ex.getClass().getSimpleName(), ex.getMessage());
    }

    public static Response ok(Object data) {
        return new Response(data);
    }

    public static Response ok() {
        return new Response(null);
    }
}
