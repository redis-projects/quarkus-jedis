package org.acme.redis.services;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.graph.Record;
import redis.clients.jedis.graph.ResultSet;
import redis.clients.jedis.graph.entities.Node;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@ApplicationScoped
public class GenreService {

    @ConfigProperty(name = "redis.movies.graph.name")
    String graphName;

    private static final String OBJECT_ID = "g";
    private static final String OBJECT_REF = "genre";
    private static final String ALL_GENRES = "MATCH (g:Genre) RETURN g";

    @Inject
    UnifiedJedis jedis;

    public List<String> getAllGenres() {

        log.info("Retrieving all genres!");

        // Get Records
        ResultSet rs = jedis.graphQuery(graphName, ALL_GENRES);
        Iterator<Record> iterator = rs.iterator();

        log.info("{} genres found", rs.size());
        List<String> genres = new ArrayList<>();
        while (iterator.hasNext()) {
            Record r = iterator.next();
            Node n = r.getValue(OBJECT_ID);
            genres.add(n.getProperty(OBJECT_REF).getValue().toString());
        }

        // Marshall response
        return genres;
    }
}
