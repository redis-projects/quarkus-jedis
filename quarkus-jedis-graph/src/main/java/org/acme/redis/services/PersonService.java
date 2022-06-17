package org.acme.redis.services;

import lombok.extern.slf4j.Slf4j;
import org.acme.redis.model.Person;
import org.acme.redis.model.response.Page;
import org.acme.redis.utils.QueryUtils;
import redis.clients.jedis.graph.Record;
import redis.clients.jedis.graph.ResultSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Iterator;

import static org.acme.redis.type.TypeENUM.PERSON;
import static org.acme.redis.utils.QueryUtils.COUNT_REF;

@Slf4j
@ApplicationScoped
public class PersonService {

    private static final String OBJECT_ID = "a";
    private static final String ALL_PERSONS = "MATCH (%1$s:%2$s)";

    @Inject
    QueryUtils queryUtils;

    /**
     * MATCH (a:Actor) WHERE a.name = "Tom Hanks" RETURN a
     *
     * @param name
     * @return
     */
    public Person getPersonByName(String name, String ref) {

        // Build Query
        String personQuery = String.format(ALL_PERSONS, OBJECT_ID, ref);
        String query = String.format("%1$s WHERE a.name = \"%2$s\" RETURN %3$s", personQuery, name, OBJECT_ID);

        log.info("Search for {} by name with query : '{}'", ref, query);

        // Get Records
        ResultSet rs = queryUtils.executeQuery(query);
        Iterator<Record> iterator = rs.iterator();
        Person p = queryUtils.mapNodeToPerson(rs.iterator().next().getValue(OBJECT_ID));

        // Return aggregated results
        return p;
    }

    /**
     * MATCH (total:Actor)
     * MATCH (a:Actor) RETURN a, count(total) as total SKIP 0 LIMIT 20
     *
     * @return
     */
    public Page getAllPersons(int page, int limit, String ref) {

        // Build Query
        String actorCount = String.format(ALL_PERSONS, COUNT_REF, ref);
        String actorQuery = String.format(ALL_PERSONS, OBJECT_ID, ref);
        String returnQuery = String.format("RETURN %1$s, count(%2$s) as %2$s SKIP %3$s LIMIT %4$s",
                OBJECT_ID, COUNT_REF, queryUtils.getSkipRecords(page, limit), limit);
        String query = String.format("%1$s %2$s %3$s", actorCount, actorQuery, returnQuery);

        log.info("Search for all {} with query : '{}'", ref, query);

        // Get Records
        ResultSet rs = queryUtils.executeQuery(query);
        Iterator<Record> iterator = rs.iterator();

        // Return aggregated results
        return queryUtils.mapResultsToArray(iterator, page, limit, OBJECT_ID, PERSON);
    }

    /**
     * MATCH (a:Actor)-[:ACTED_IN]->(m:Movie) WHERE m.title = "Toy Story" RETURN a
     *
     * @return
     */
    public Page getPeopleByMovieId(int page, int limit, String ref) {

        // Build Query
        String actorCount = String.format(ALL_PERSONS, COUNT_REF, ref);
        String actorQuery = String.format(ALL_PERSONS, OBJECT_ID, ref);
        String returnQuery = String.format("RETURN %1$s, count(%2$s) as %2$s SKIP %3$s LIMIT %4$s",
                OBJECT_ID, COUNT_REF, queryUtils.getSkipRecords(page, limit), limit);
        String query = String.format("%1$s %2$s %3$s", actorCount, actorQuery, returnQuery);

        log.info("Search for all {} with query : '{}'", ref, query);

        // Get Records
        ResultSet rs = queryUtils.executeQuery(query);
        Iterator<Record> iterator = rs.iterator();

        // Return aggregated results
        return queryUtils.mapResultsToArray(iterator, page, limit, OBJECT_ID, PERSON);
    }


}
