package org.acme.redis.api;

import lombok.extern.slf4j.Slf4j;
import org.acme.redis.model.Movie;
import org.acme.redis.model.Person;
import org.acme.redis.model.response.Page;
import org.acme.redis.services.MovieService;
import org.acme.redis.services.PersonService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Slf4j
@Path("/people")
public class PersonResource {

    private static String ACTOR_REF = "Actor";
    private static String DIRECTOR_REF = "Director";

    @Inject
    PersonService personService;

    // http://localhost:8080/people/actors/all?limit=20&page=1
    @GET
    @Path("/actors/all")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get All Actors", description = "Returns all Actors in a paginated response")
    public Page getAllActors(@RestQuery int page, @RestQuery int limit) {
        page = page <= 0 ? 1 : page;
        limit = limit <= 0 ? 20 : limit;
        return personService.getAllPersons(page, limit, ACTOR_REF);
    }

    // http://localhost:8080/people/directors/all?limit=20&page=1
    @GET
    @Path("/directors/all")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get All Directors", description = "Returns all Directors in a paginated response")
    public Page getAllDirectors(@RestQuery int page, @RestQuery int limit) {
        page = page <= 0 ? 1 : page;
        limit = limit <= 0 ? 20 : limit;
        return personService.getAllPersons(page, limit, DIRECTOR_REF);
    }

    // http://localhost:8080/people/actors/name/Tom Hanks
    @GET
    @Path("/actors/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Actor By Name", description = "Returns actor by name")
    public Person getActorByName(@RestPath String name) {
        return personService.getPersonByName(name, ACTOR_REF);
    }

    // http://localhost:8080/people/directors/name/Quentin Tarantino
    @GET
    @Path("/directors/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Director By Name", description = "Returns Director by name")
    public Person getDirectorByName(@RestPath String name) {
        return personService.getPersonByName(name, DIRECTOR_REF);
    }

}

