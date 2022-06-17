package org.acme.redis.api;

import lombok.extern.slf4j.Slf4j;
import org.acme.redis.services.GenreService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestPath;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Slf4j
@Path("/genre")
public class GenreResource {

    @Inject
    GenreService genreService;

    // http://localhost:8080/genre
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get All Genres", description = "Returns all Genre as a list.")
    public List<String> getAllGenres(@RestPath long id) {
        return genreService.getAllGenres();
    }
}