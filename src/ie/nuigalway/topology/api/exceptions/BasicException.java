package ie.nuigalway.topology.api.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import ie.nuigalway.topology.api.model.ErrorModel;

public class BasicException extends WebApplicationException {

    private static final long serialVersionUID = -7718258421253104846L;

    public BasicException(Status status, String shortError, String fullError) {
        super(Response.status(status).entity(
                new ErrorModel(status.getStatusCode(), shortError, fullError)).build());
    }
    
}