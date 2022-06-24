package org.acme.redis.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.acme.redis.type.RelationshipENUM.*;

/**
 * Simple helper class for Query Generation
 */
@Slf4j
@Singleton
public class GraphUtils {

    private static String CREATE_QUERY = "CREATE ( %1$s:%2$s { %3$s })";
    private static String RELATIONSHIP_QUERY = "MATCH %1$s CREATE %2$s";
    private static String MOVIE_GENRE_QUERY = "MATCH (m:Movie { title: \"%1$s\" }), ( g:Genre { genre: \"%2$s\" }) CREATE (m)-[:%3$s]->(g)";
    private static String ACTOR_MOVIE_QUERY = "MATCH (a:Actor { name: \"%1$s\" }), ( m:Movie { title: \"%2$s\" }) CREATE (m)-[:%3$s]->(m)";
    private static String DIRECTOR_MOVIE_QUERY = "MATCH (d:Director { name: \"%1$s\" }), ( m:Movie { title: \"%2$s\" }) CREATE (m)-[:%3$s]->(m)";

    private static String PERSON_MOVIE_QUERY = "MATCH (%1$s:%6$s), (%2$s:Movie) " +
            "WHERE %1$s.name = \"%3$s\" AND %2$s.title = \"%4$s\" " +
            "CREATE (%1$s)-[:%5$s]->(%2$s)";

    /**
     * Util should generate a string like so:
     * "CREATE ( p:Person { name: $name, age: $age, gender: $gender, status: $status }) RETURN p;
     */
    public String createNewObject(String objectID, Object object, Map<String, Object> params, boolean returnNode) {

        // Use ClassName as Type if not provided
        if (objectID == null || objectID.isEmpty()) {
            object.getClass().getSimpleName();
        }

        // Parameterise all Map (Object) values
        List<String> queryParams = new ArrayList<>();
        params.keySet().stream().forEach(key -> {
            queryParams.add(key + ": $" + key);
        });

        // Build the query
        String query = String.format(CREATE_QUERY, objectID.toLowerCase(), objectID,
                String.join(", ", queryParams));

        // Append return statement if required
        if (returnNode) {
            query = query + "Return " + objectID.toLowerCase();
        }

        log.debug("Creating new Entity : {}", query);
        return query;
    }


    /**
     * Util should generate a string like so:
     * "MATCH (m:Movie{movieId:1}), (g:Genre{genre:"Animation"}) CREATE (m)-[:IN_GENRE]->(g);
     */
    public String createGenreMovieRelationship(String movieTitle, String genre) {
        log.info("Generating new Relationship for Movie :'{}' and Genre: '{}'", movieTitle, genre);
        String query = String.format(MOVIE_GENRE_QUERY, movieTitle, genre, GENRE.getRelationship());
        log.info("Query String: {}", query);
        return query;
    }

    public String createPersonMovieRelationship(String movieTitle, String personName, String roleID) {
        log.info("Generating new Relationship for Movie :'{}' and {}: '{}'", movieTitle, roleID, personName);

        String personRef = "p";
        String movieRef = "m";

        String query;
        if (roleID.toUpperCase().equals(ACTOR.name())) {
            //query = String.format(ACTOR_MOVIE_QUERY, personName, movieTitle, ACTOR.getRelationship());
            query = String.format(PERSON_MOVIE_QUERY,
                    personRef, movieRef, personName, movieTitle, ACTOR.getRelationship(), ACTOR.getObjectRef());
        } else {
            //query = String.format(DIRECTOR_MOVIE_QUERY, personName, movieTitle, DIRECTOR.getRelationship());
            query = String.format(PERSON_MOVIE_QUERY,
                    personRef, movieRef, personName, movieTitle, DIRECTOR.getRelationship(), DIRECTOR.getObjectRef());
        }

        log.info("Query String: {}", query);
        return query;
    }

    // TODO: Create Generic relationship builder
//    public String createRelationship(RelationshipENUM relationship, RelationshipMapper relationshipMapper ) {
//        log.info("Generating new Relationship for Movie :'{}' and {}: '{}'", movieTitle, roleID, personName);
//
//        switch (relationship) {
//            case GENRE:
//                break;
//            case ACTOR:
//                break;
//            case DIRECTOR:
//                break;
//        }
//
//    }

}
