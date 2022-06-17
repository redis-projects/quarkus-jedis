package org.acme.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.acme.redis.model.Student;
import org.acme.redis.schema.StudentSchema;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.SearchProtocol;

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
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.acme.redis.schema.StudentSchema.STUDENT_INDEX;

@Slf4j
@Startup
@ApplicationScoped
public class JedisConfig {

    private static final String STUDENTS_FILES = "students.json";
    public AtomicInteger studentCount = new AtomicInteger(1);

    // value from application.properties
    @ConfigProperty(name = "redis.bulk.load.data")
    boolean loadData;

    @Inject
    StudentSchema schema;

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

    private void buildIndex(@Observes StartupEvent ev, UnifiedJedis jedis) {
        log.info("Checking to see if Students Index exists...");

        // Check if Student Index exists
        boolean createIndex = false;
        try {
            // Attempt to get Index details, will throw exception if it doesnt exist
            Object response = jedis.sendCommand(SearchProtocol.SearchCommand.INFO, STUDENT_INDEX);
        } catch (JedisDataException e) {
            if (e.getMessage().contains("Unknown Index name")) {
                log.info("No student index exists, will attempt to create");
                createIndex = true;
            }
        }

        // Create Student Index
        if (createIndex) {
            log.info("Creating {} Index in Redis", STUDENT_INDEX);
            try {
                // Create index
                jedis.ftCreate(STUDENT_INDEX,
                        IndexOptions.defaultOptions().setDefinition(schema.getIndexDefinition()),
                        schema.getStudentSchema());
            } catch (JedisDataException jde) {
                jde.printStackTrace();
            }
        }

        // Bulk load data into Redis?
        if (loadData) {
            log.info("loading data from json file...");
            Object obj = jedis.jsonGet("student:600");

            if (obj == null) {
                try {
                    this.loadData(jedis);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                log.info("Skipping data load, looks like there is already 600+ keys in the DB");
            }
        }
    }

    private void loadData(UnifiedJedis jedis) throws IOException {

        // Read file from disk
        ClassLoader classLoader = this.getClass().getClassLoader();
        BufferedReader bufferedReader = new BufferedReader(new
                InputStreamReader(classLoader.getResourceAsStream(STUDENTS_FILES)));

        // Serialise to Student Array
        List<Student> students = Arrays.asList(mapper.readValue(bufferedReader, Student[].class));
        log.info("Serialised {} json objects", students.size());

        // Insert into Redis
        students.stream().forEach(student -> {
            student.setAge(determineAge(student.getBirth()));
            jedis.jsonSet(String.format("student:%s", studentCount.getAndIncrement()),
                    Path.ROOT_PATH, student);
        });

        log.info("Successfully inserted '{}' into Redis", students.size());
        log.info("Redis DBSize : '{}' values", jedis.dbSize());
    }

    /**
     * Add age field to json...
     *
     * @param date
     * @return
     */
    private int determineAge(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);
        return Period.between(localDate, LocalDate.now()).getYears();
    }
}
