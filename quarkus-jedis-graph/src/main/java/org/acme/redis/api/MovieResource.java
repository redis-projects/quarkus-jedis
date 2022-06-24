package org.acme.redis.api;

import lombok.extern.slf4j.Slf4j;
import org.acme.redis.model.Movie;
import org.acme.redis.model.Person;
import org.acme.redis.model.response.Page;
import org.acme.redis.services.MovieService;
import org.acme.redis.type.RelationshipENUM;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import redis.clients.jedis.graph.ResultSet;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.acme.redis.type.RelationshipENUM.ACTOR;
import static org.acme.redis.type.RelationshipENUM.DIRECTOR;

@Slf4j
@Path("/movie")
public class MovieResource {

    @Inject
    MovieService movieService;

    // http://localhost:8080/movie/id/862
    @GET
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Movie By ID", description = "Returns Movie By ID")
    public Movie getMovieById(@RestPath long id) {
        return movieService.getMovieById(id);
    }

    // http://localhost:8080/movie/imdb/0114709
    @GET
    @Path("/imdb/{imdbId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Movie By IMDB ID", description = "Returns Movie By IMDB ID")
    public Movie getMovieByImdbId(@RestPath String imdbId) {
        return movieService.getMovieByImdbId(imdbId);
    }

    // http://localhost:8080/movie/title/Toy Story
    @GET
    @Path("/title/{title}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Movie By Title", description = "Returns Movie By Title")
    public Movie getMovieByTitle(@RestPath String title) {
        return movieService.getMovieByTitle(title);
    }

    // http://localhost:8080/movie/year/1995?page=1&limit=30
    @GET
    @Path("/year/{year}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get All Movies By Year", description = "Returns all Movie by Year in a paginated response ")
    public Page getMovieByYear(@RestPath int year, @RestQuery int page, @RestQuery int limit) {
        page = page <= 0 ? 1 : page;
        limit = limit <= 0 ? 20 : limit;
        return movieService.getMovieByYear(year, page, limit);
    }

    // http://localhost:8080/movie/genre?page=1&limit=30&genres=Drama,Animation
    @GET
    @Path("/genre")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get All Movies By Genre", description = "Returns all Movie by Genre in a paginated response. " +
            "NOTE: Genres should be a comma separated list")
    public Page getMovieByGenre(@RestQuery String genres, @RestQuery int page, @RestQuery int limit) {
        page = page <= 0 ? 1 : page;
        limit = limit <= 0 ? 20 : limit;
        return movieService.getMovieByGenre(genres, page, limit);
    }

    // http://localhost:8080/movie/actor/name/Tom Hanks?page=1&limit=10
    @GET
    @Path("/actor/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Movies By Actor Name", description = "Returns Movies that have an Actor by Name")
    public Page getMoviesByActorName(@RestPath String name, @RestQuery int page, @RestQuery int limit) {
        page = page <= 0 ? 1 : page;
        limit = limit <= 0 ? 20 : limit;
        return movieService.getMovieByPersonName(name, page, limit, ACTOR);
    }

    // http://localhost:8080/movie/director/name/David Fincher?page=1&limit=10
    @GET
    @Path("/director/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Movies By Director Name", description = "Returns Movies that have an Director by Name")
    public Page getMoviesByDirectorName(@RestPath String name, @RestQuery int page, @RestQuery int limit) {
        page = page <= 0 ? 1 : page;
        limit = limit <= 0 ? 20 : limit;
        return movieService.getMovieByPersonName(name, page, limit, DIRECTOR);
    }
}

// Get Actors by Movie
