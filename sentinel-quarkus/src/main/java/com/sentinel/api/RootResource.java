package com.sentinel.api;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.Response;
import java.net.URI;

// src/main/java/com/sentinel/api/RootResource.java
@Path("/")
public class RootResource {
    @GET
    public Response index() {
        return Response.seeOther(URI.create("/dashboard")).build();
    }
}