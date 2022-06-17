package org.acme.redis.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.acme.redis.model.Genre;
import org.acme.redis.model.Movie;
import org.acme.redis.model.Person;
import org.acme.redis.utils.GraphUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.UnifiedJedis;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.acme.redis.type.RelationshipENUM.ACTOR;
import static org.acme.redis.type.RelationshipENUM.DIRECTOR;

@Slf4j
@Startup
@ApplicationScoped
public class JedisConfig {

    private static final String DATE_PATTERN = "yyyy-M-d";

    @Inject
    GraphUtils graphUtils;

    // Raw Data Records
    private static final String MOVIES_FILE = "data/movies.json";
    private static final String GENRES_FILE = "data/genres.json";
    private static final String DIRECTORS_FILE = "data/directors.json";
    private static final String ACTORS_FILE = "data/actors.json";

    // Relationship between Movies + Genres + People
    private static final String MOVIES_GENRE_RL_FILE = "data/movie_genre_relationship.json";
    private static final String MOVIES_ACTOR_RL_FILE = "data/actor_movie_relationships.json";
    private static final String MOVIES_DIRECTOR_RL_FILE = "data/director_movie_relationships.json";

    // value from application.properties
    @ConfigProperty(name = "redis.bulk.load.data")
    boolean loadData;

    @ConfigProperty(name = "redis.movies.graph.name")
    String graphName;

    @Inject
    ObjectMapper mapper;

    private volatile JedisPool jedisPool;

    @Dependent
    @Produces
    public UnifiedJedis jedis() {
        UnifiedJedis jedis = new JedisPooled(Protocol.DEFAULT_HOST, 6379);
        return jedis;
    }

    public void destroy(@Disposes UnifiedJedis jedis) {
        jedis.close();
    }


    private void loadData(@Observes StartupEvent ev, UnifiedJedis jedis) throws IOException {

        // Delete current graph implementation if exists and start from fresh
        // Command: GRAPH.DELETE <graph_name>
        log.info("Clearing down graph: {}", graphName);
        try {
            jedis.graphDelete(graphName);
            log.info("Successfully deleted {}", graphName);
        } catch (Exception e) {
            // do nothing, graph doesnt exist
        }

        // Load Entity Dataset
        log.info("Hydrating graph with fresh values: {}", graphName);
        this.loadMovies(jedis);
//        this.loadEntities(jedis, Movie.class, MOVIES_FILE);
//        this.loadEntities(jedis, Genre.class, GENRES_FILE);
        this.loadGenres(jedis);
        this.loadPerson(jedis, ACTORS_FILE, "Actor");
        this.loadPerson(jedis, DIRECTORS_FILE, "Director");

        // Create Relationships
        this.buildGenreRelationships(jedis);
        this.buildPersonRelationships(jedis, ACTOR.name());
        this.buildPersonRelationships(jedis, DIRECTOR.name());
    }

    /**
     * Get UTC date from String date pattern
     * @param date
     * @return
     */
    private Long getUTCTimestamp(String date) {
        if (date != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            LocalDate localDate = LocalDate.parse(date, formatter);
            return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }

    private void loadEntities(UnifiedJedis jedis, Class entity, String fileId) throws IOException {

        String objectId = entity.getClass().getSimpleName();

        // Serialise to Movies Array
        List<Object> entities = Arrays.asList(mapper.readValue(this.loadFile(fileId), List.class));
        log.info("Serialised {} {} from resources", entities.size(), objectId);

        entities.stream().forEach(e -> {
            Map<String, Object> map = mapper.convertValue(e, Map.class);
            String query = graphUtils.createNewObject(objectId, e, map, false);
            jedis.graphQuery(graphName, query, map);
        });
    }

    private void loadMovies(UnifiedJedis jedis) throws IOException {
        // Serialise to Movies Array
        List<Movie> movies = Arrays.asList(mapper.readValue(this.loadFile(MOVIES_FILE), Movie[].class));
        log.info("Serialised {} movies from resources", movies.size());

        movies.stream().forEach(movie -> {
            Map<String, Object> map = mapper.convertValue(movie, Map.class);
            String query = graphUtils.createNewObject("Movie", movie, map, false);

            log.debug("Inserting Movie : {}", movie.getTitle());
            jedis.graphQuery(graphName, query, map);
        });
    }

    private void loadGenres(UnifiedJedis jedis) throws IOException {
        // Serialise to Genres Array
        List<Genre> genres = Arrays.asList(mapper.readValue(this.loadFile(GENRES_FILE), Genre[].class));
        log.info("Serialised {} genres from resources", genres.size());

        genres.stream().forEach(genre -> {
            Map<String, Object> map = mapper.convertValue(genre, Map.class);
            String query = graphUtils.createNewObject("Genre", genre, map, false);

            log.debug("Inserting Genre : {}", genre.getGenre());
            jedis.graphQuery(graphName, query, map);
        });
    }

    private void loadPerson(UnifiedJedis jedis, String fileName, String personType) throws IOException {
        // Serialise to Genres Array
        List<Person> personList = Arrays.asList(mapper.readValue(this.loadFile(fileName), Person[].class));
        log.info("Serialised {} {} from resources", personList.size(), personType);

        personList.stream().forEach(person -> {
            person.setBornUTC(this.getUTCTimestamp(person.getBorn()));
            person.setDiedUTC(this.getUTCTimestamp(person.getDied()));

            Map<String, Object> map = mapper.convertValue(person, Map.class);
            String query = graphUtils.createNewObject(personType, person, map, false);

            log.debug("Inserting {} : {}", personType, person.getName());
            jedis.graphQuery(graphName, query, map);
        });
    }

    private void buildGenreRelationships(UnifiedJedis jedis) throws IOException {

        List<JsonNode> jsonNodeList = Arrays.asList(mapper.readValue(
                this.loadFile(MOVIES_GENRE_RL_FILE), JsonNode[].class));

        AtomicInteger grel = new AtomicInteger(0);
        jsonNodeList.stream().forEach(jn -> {
            String movieTitle = jn.get("movieTitle").asText();
            String genre = jn.get("genre").asText();
            jedis.graphQuery(graphName, graphUtils.createGenreMovieRelationship(movieTitle, genre));
            grel.incrementAndGet();
        });
        log.info("Movie to Genre relationships created: {}", grel.get());
    }

    private void buildPersonRelationships(UnifiedJedis jedis, String roleID) throws IOException {

        List<JsonNode> jsonNodeList;
        boolean applyRole;
        String personNameId;

        if (roleID.toUpperCase().equals("ACTOR")) {
            applyRole = true;
            personNameId = "actorName";
            jsonNodeList = Arrays.asList(mapper.readValue(
                    this.loadFile(MOVIES_ACTOR_RL_FILE), JsonNode[].class));
        } else {
            personNameId = "directorName";
            jsonNodeList = Arrays.asList(mapper.readValue(
                    this.loadFile(MOVIES_DIRECTOR_RL_FILE), JsonNode[].class));
        }

        AtomicInteger grel = new AtomicInteger(0);
        jsonNodeList.stream().forEach(jn -> {
            String movieTitle = jn.get("movieTitle").asText();
            String personName = jn.get(personNameId).asText();
            jedis.graphQuery(graphName, graphUtils.createPersonMovieRelationship(movieTitle, personName, roleID));
            grel.incrementAndGet();
        });
        log.info("Movie to {} relationships created: {}", roleID, grel.get());
    }

    private BufferedReader loadFile(String fileName) {
        // Read file from disk
        ClassLoader classLoader = this.getClass().getClassLoader();
        return new BufferedReader(new
                InputStreamReader(classLoader.getResourceAsStream(fileName)));
    }
}
