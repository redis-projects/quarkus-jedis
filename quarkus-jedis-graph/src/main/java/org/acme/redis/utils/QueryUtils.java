package org.acme.redis.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.acme.redis.model.Movie;
import org.acme.redis.model.Person;
import org.acme.redis.model.response.Page;
import org.acme.redis.type.TypeENUM;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.graph.Record;
import redis.clients.jedis.graph.ResultSet;
import redis.clients.jedis.graph.entities.Node;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple helper class for Query Generation
 */
@Slf4j
@Singleton
public class QueryUtils {

    public static final String COUNT_REF = "total";

    @ConfigProperty(name = "redis.movies.graph.name")
    String graphName;

    @Inject
    ObjectMapper mapper;

    @Inject
    UnifiedJedis jedis;

    public ResultSet executeQuery(String query) {
        ResultSet rs = jedis.graphQuery(graphName, query);
        log.info("Query search found {} entities", rs.size());
        return rs;
    }

    public Movie mapNodeToMovie(Node node) {
        return mapper.convertValue(this.getMapFromNode(node), Movie.class);
    }

    public Person mapNodeToPerson(Node node) {
        return mapper.convertValue(this.getMapFromNode(node), Person.class);
    }

    private Map<String, Object> getMapFromNode(Node node) {
        return node.getEntityPropertyNames().stream()
                .collect(Collectors.toMap(str -> str, str -> node.getProperty(str).getValue()));
    }

    public Page mapResultsToArray(Iterator<Record> iterator, int page, int limit, String objectRef, TypeENUM typeENUM) {
        List<Object> entities = new ArrayList<>();
        int totalRecords = 0;
        while (iterator.hasNext()) {
            Record r = iterator.next();

            switch (typeENUM) {
                case PERSON:
                    entities.add(this.mapNodeToPerson(r.getValue(objectRef)));
                    break;
                case MOVIE:
                    entities.add(this.mapNodeToMovie(r.getValue(objectRef)));
                    break;
            }

            if (totalRecords == 0) {
                totalRecords = Integer.parseInt(r.getValue(COUNT_REF).toString());
            }
        }

        return new Page(totalRecords, page, limit, entities);
    }

    public int getSkipRecords(int page, int limit) {
        return (page - 1) * limit;
    }

}
