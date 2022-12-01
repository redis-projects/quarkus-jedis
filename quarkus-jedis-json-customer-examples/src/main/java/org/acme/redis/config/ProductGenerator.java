package org.acme.redis.config;


import com.github.javafaker.Faker;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.acme.redis.model.Product;
import org.acme.redis.model.Transaction;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.json.Path;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.util.Map.entry;

@Slf4j
@Startup
@ApplicationScoped
public class ProductGenerator {

    @Inject
    UnifiedJedis jedis;

    private Faker faker;

    @ConfigProperty(name = "redis.generate.products")
    boolean loadData;

    @ConfigProperty(name = "redis.generate.products.amount")
    int productCount;

    void onStart(@Observes StartupEvent ev) throws IOException, CsvException {
        log.info("Generating products : {}", loadData);
        if (loadData) {
            log.info("Generating {} product's : {}", productCount);
            faker = new Faker();
            this.generateProducts(productCount);
        }
    }

    /**
     * @throws IOException
     */
    private void generateProducts(int productCount) throws IOException {
        log.info("Generating {} products", productCount);
        IntStream.range(1, productCount + 1).parallel().forEach(i -> {
            log.info("Inserting product id {} of {}", i, productCount);
            jedis.jsonSet("org:ts:product:" + i, Path.ROOT_PATH, this.generateProduct(i));
        });
        log.info("Finished inserting products");
    }

    private Product generateProduct(long id) {
        return new Product(
                id,
                faker.commerce().productName(),
                RandomStringUtils.randomAlphanumeric(6).toUpperCase(),
                UUID.randomUUID().toString(), // Supplier ID
                UUID.randomUUID().toString(), // Category ID
                ThreadLocalRandom.current().nextInt(1, 6 + 1), // Quantity per Unit
                Double.valueOf(faker.commerce().price()), // Unit Price
                false // Discontinued
        );
    }
}