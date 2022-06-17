package org.acme.redis.services;

import lombok.extern.slf4j.Slf4j;
import org.acme.redis.model.Movie;
import org.acme.redis.model.response.Page;
import org.acme.redis.type.RelationshipENUM;
import org.acme.redis.utils.QueryUtils;
import redis.clients.jedis.graph.Record;
import redis.clients.jedis.graph.ResultSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Iterator;

import static org.acme.redis.type.MovieENUM.*;
import static org.acme.redis.type.RelationshipENUM.ACTOR;
import static org.acme.redis.type.RelationshipENUM.DIRECTOR;
import static org.acme.redis.type.TypeENUM.MOVIE;

@Slf4j
@ApplicationScoped
public class MovieService {

    private static final String OBJECT_ID = "m";
    private static final String TOTAL_COUNT = "WITH count(m) as total";
    private static final String GENRE_QUERY = "MATCH (m:Movie)-[:IN_GENRE]->(g:Genre) WHERE g.genre IN [%1$s]";
    private static final String ACTOR_QUERY = "MATCH (%1$s:%2$s)-[:%3$s]->(m:Movie) WHERE %1$s.name = \"%4$s\"";

    @Inject
    QueryUtils queryUtils;

    public Movie getMovieById(long id) {

        // Build Query
        String query = String.format("MATCH (m:Movie) WHERE m.%1$s = %2$s RETURN m", ID.getFieldId(), id);
        log.info("Search for movie with {} : '{}' with query : '{}'", ID.getFieldId(), id, query);

        // Get Records
        Record r = queryUtils.executeQuery(query).iterator().next();

        // Marshall response
        return queryUtils.mapNodeToMovie(r.getValue(OBJECT_ID));
    }

    public Movie getMovieByImdbId(String imdbId) {

        // Build Query
        String query = String.format("MATCH (m:Movie) WHERE m.%1$s = \"%2$s\" RETURN m", IMDB_ID.getFieldId(), imdbId);
        log.info("Search for movie with {} : '{}' with query : '{}'", IMDB_ID.getFieldId(), imdbId, query);

        // Get Records
        Record r = queryUtils.executeQuery(query).iterator().next();

        // Marshall response
        return queryUtils.mapNodeToMovie(r.getValue(OBJECT_ID));
    }

    /**
     * MATCH (m:Movie) WHERE m.title = "Toy Story" RETURN m
     */
    public Movie getMovieByTitle(String title) {

        // Build Query
        String query = String.format("MATCH (m:Movie) WHERE m.%1$s = \"%2$s\" RETURN m", TITLE.getFieldId(), title);
        log.info("Search for movie with {} : '{}' with query : '{}'", TITLE.getFieldId(), title, query);

        // Get Records
        Record r = queryUtils.executeQuery(query).iterator().next();

        // Marshall response
        return queryUtils.mapNodeToMovie(r.getValue(OBJECT_ID));
    }

    /**
     * Paginated Response: wraps response in Page object
     * MATCH (m:Movie) WHERE m.year = 1995 WITH count(m) as total
     * MATCH (m:Movie) WHERE m.year = 1995 RETURN m, total SKIP 10 LIMIT 20
     */
    public Page getMovieByYear(int year, int page, int limit) {

        // Build Query
        String queryBase = String.format("MATCH (m:Movie) WHERE m.%1$s = %2$s", YEAR.getFieldId(), year);
        String query = String.format("%1$s %2$s %1$s RETURN m, total SKIP %3$s LIMIT %4$s",
                queryBase, TOTAL_COUNT, queryUtils.getSkipRecords(page, limit), limit);
        log.info("Search for movie with {} : '{}' with query : '{}'", YEAR.getFieldId(), year, query);

        // Get Records
        ResultSet rs = queryUtils.executeQuery(query);

        // Marshall response
        return queryUtils.mapResultsToArray(rs.iterator(), page, limit, OBJECT_ID, MOVIE);
    }

    /**
     * Paginated Response: wraps response in Page object
     * MATCH (m:Movie)-[:IN_GENRE]->(g:Genre) WHERE g.genre IN ["Animation", "Sci-Fi"]
     * WITH count(m) as total
     * MATCH (m:Movie)-[:IN_GENRE]->(g:Genre) WHERE g.genre IN ["Animation", "Sci-Fi"]
     * RETURN m, total SKIP 0 LIMIT 30
     */
    public Page getMovieByGenre(String genres, int page, int limit) {

        // Add quotes to comma seperate list
        String genresRegEx = String.join(",", Arrays.asList(genres.split(",")))
                .replaceAll("([^,]+)", "\"$1\"");

        // Build Query
        String genreQuery = String.format(GENRE_QUERY, genresRegEx);
        String query = String.format("%1$s %2$s %1$s RETURN m, total SKIP %3$s LIMIT %4$s",
                genreQuery, TOTAL_COUNT, queryUtils.getSkipRecords(page, limit), limit);

        log.info("Search for movie with Genre : '{}' with query : '{}'", genres, query);

        // Get Records
        ResultSet rs = queryUtils.executeQuery(query);

        // Marshall response
        return queryUtils.mapResultsToArray(rs.iterator(), page, limit, OBJECT_ID, MOVIE);
    }

    /**
     * Paginated Response: wraps response in Page object
     * MATCH (a:Actor)-[:ACTED_IN]->(m:Movie) WHERE a.name = \"Tom Hanks\"
     * WITH count(m) as total
     * MATCH (a:Actor)-[:ACTED_IN]->(m:Movie) WHERE a.name = \"Tom Hanks\"
     * RETURN m, total SKIP 0 LIMIT 30
     */
    public Page getMovieByPersonName(String name, int page, int limit, RelationshipENUM personType) throws IllegalStateException {

        // Build Query
        String personQuery;
        switch (personType) {
            case ACTOR:
                personQuery = String.format(ACTOR_QUERY, ACTOR.getObjectId(), ACTOR.getObjectRef(), ACTOR.getRelationship(), name);
                break;
            case DIRECTOR:
                personQuery = String.format(ACTOR_QUERY, DIRECTOR.getObjectId(), DIRECTOR.getObjectRef(), DIRECTOR.getRelationship(), name);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + personType);
        }

        String query = String.format("%1$s %2$s %1$s RETURN m, total SKIP %3$s LIMIT %4$s",
                personQuery, TOTAL_COUNT, queryUtils.getSkipRecords(page, limit), limit);

        log.info("Search for movie with {} : '{}' with query : '{}'", personType.name(), name, query);

        // Get Records
        ResultSet rs = queryUtils.executeQuery(query);
        Iterator<Record> iterator = rs.iterator();

        // Return aggregated results
        return queryUtils.mapResultsToArray(iterator, page, limit, OBJECT_ID, MOVIE);
    }
}
