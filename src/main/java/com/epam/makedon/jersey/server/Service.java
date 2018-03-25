package com.epam.makedon.jersey.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/service")
public class Service {

    @GET
    @Path("/helloworld")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMessage() {
        return "Hello world!";
    }
}
