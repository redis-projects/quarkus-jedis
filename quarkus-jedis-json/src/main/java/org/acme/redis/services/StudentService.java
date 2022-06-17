package org.acme.redis.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.ext.web.handler.sockjs.impl.StringEscapeUtils;
import lombok.extern.slf4j.Slf4j;
import org.acme.redis.config.JedisConfig;
import org.acme.redis.model.Student;
import org.acme.redis.utils.SearchUtil;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.acme.redis.schema.StudentSchema.STUDENT_INDEX;

@Slf4j
@ApplicationScoped
public class StudentService {

    // TODO - Query Builder Factory
    private static final String QUERY_PREFIX = "@$.";
    private static final String QUERY_NAME = "firstName:";
    private static final String QUERY_LASTNAME = "lastName:";
    private static final String QUERY_CITY = "city:";

    @Inject
    JedisConfig config;

    @Inject
    SearchUtil searchUtil;

    @Inject
    UnifiedJedis jedis;

    @Inject
    ObjectMapper mapper;

    public String insertStudent(Student student) {
        String key = "student:" + config.studentCount.incrementAndGet();
        log.info("Inserting new student into redis with key : '{}'", key);
        jedis.jsonSet(key, Path.ROOT_PATH, student);
        return key;
    }

    public Student getStudent(String key) {
        log.info("Obtaining Student with ID : {}", key);
        return jedis.jsonGet(key, Student.class);
    }

    /**
     * Search by 'firstName' & 'lastName' ::
     * FT.SEARCH "idx:student" "(@\\$\\.firstName:(Oliwia) | @\\$\\.lastName:(Maya))"
     *
     * @param name
     * @return
     */
    public SearchResult searchByName(String name, Integer page, Integer offset) throws Exception {
        log.info("Searching Student with Name : {}", name);
        String firstName = searchUtil.escapeMetaCharacters(
                QUERY_PREFIX + QUERY_NAME + searchUtil.wrapInParentheses(name));
        String lastName = searchUtil.escapeMetaCharacters(
                QUERY_PREFIX + QUERY_LASTNAME + searchUtil.wrapInParentheses(name));
        String queryString = searchUtil.wrapInParentheses(firstName + " | " + lastName);

        log.info("Using queryByName string : '{}'", StringEscapeUtils.escapeJava(queryString));
        Query query = searchUtil.handlePagination(new Query(queryString), page, offset);
        return jedis.ftSearch(STUDENT_INDEX, new Query(queryString));
    }

    /**
     * Search by 'City' TAG :: FT.SEARCH "idx:student" "@\\$\\.city:{Paris}"
     *
     * @param city
     * @return
     */
    public SearchResult searchByCity(String city, Integer page, Integer offset) throws Exception {
        log.info("Searching for City: '{}' ", city);
        String cityQueryString = searchUtil.escapeMetaCharacters(
                QUERY_PREFIX + QUERY_CITY + searchUtil.wrapInCurlyBraces(city));

        log.info("Using queryByCity string : '{}'", StringEscapeUtils.escapeJava(cityQueryString));
        Query query = searchUtil.handlePagination(
                new Query(searchUtil.escapeMetaCharacters(cityQueryString)), page, offset);
        return jedis.ftSearch(STUDENT_INDEX, query);
    }

    /**
     * Search by 'Generic Search Term' :: FT.SEARCH "idx:student" "Hello World"
     *
     * @param queryString
     * @param page
     * @param offset
     * @return
     */
    public SearchResult genericSearch(String queryString, Integer page, Integer offset) {
        log.info("Generic search for Students: '{}' ", queryString);
        Query query = searchUtil.handlePagination(
                new Query(searchUtil.escapeMetaCharacters(queryString)), page, offset);
        return jedis.ftSearch(STUDENT_INDEX, query);
    }
}
