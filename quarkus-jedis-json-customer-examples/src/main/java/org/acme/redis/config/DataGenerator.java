package org.acme.redis.config;


import com.github.javafaker.Faker;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.clients.jedis.UnifiedJedis;

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
public class DataGenerator {

    @Inject
    UnifiedJedis jedis;

    @ConfigProperty(name = "redis.generate.stock")
    boolean loadData;

    private static final String LOCATIONS = "locations.csv";
    private static final String PRODUCTS = "products.csv";

    void onStart(@Observes StartupEvent ev) throws IOException, CsvException {
        log.info("Generate Stock : {}", loadData);
        if (loadData) {
            // this.generateProductSkus(5000);
            //this.generateStockQuantity();
        }
    }

    /**
     * Generates 676 sites by iterating through the
     *
     * @throws IOException
     */
    private void generateLocations() throws IOException {

        Set<String> locations = new HashSet<>();
        for (char a1 = 'a'; a1 <= 'z'; a1++) {
            for (char a2 = 'a'; a2 <= 'z'; a2++) {
                locations.add(String.valueOf(a1).toUpperCase() + String.valueOf(a2).toUpperCase());
            }
        }

        if (!locations.isEmpty()) {
            log.info("Writing {} locations to `locations.csv`", locations.size());
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("locations.csv"), "utf-8"))) {
                writer.write(String.join(",", locations));
            }
        }
    }

    /**
     * Generates X number of SKUs:
     * - product_id  , sku_id
     * - PWB00000010 , 00010
     *
     * @param rangeSize
     * @throws IOException
     */
    private void generateProductSkus(int rangeSize) throws IOException {

        StringBuilder sb = new StringBuilder();
        IntStream.range(1, rangeSize + 1).forEach(i -> {
            sb.append("PWB" + String.format("%08d", i));
            sb.append("," + String.format("%05d", i));
            sb.append(System.lineSeparator());
        });

        if (!sb.toString().isEmpty()) {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream("products.csv"), "utf-8"))) {
                writer.write(sb.toString());
            }
        }
    }

    /**
     * FT.CREATE tsIdx ON HASH PREFIX 1 org:ts:stock: SCHEMA productId TAG sku TAG sideId TAG quantity NUMERIC
     * @throws IOException
     * @throws CsvException
     */
    private void generateStockQuantity() throws IOException, CsvException {
        // Read file from disk
        ClassLoader classLoader = this.getClass().getClassLoader();
        BufferedReader locationsReader = new BufferedReader(new
                InputStreamReader(classLoader.getResourceAsStream(LOCATIONS)));

        List<String[]> locationReader;
        try (CSVReader reader = new CSVReader(locationsReader)) {
            locationReader = reader.readAll();
        }

        String[] productReader;
        String[] locations = locationReader.get(0);
        BufferedReader productsReader = new BufferedReader(new
                InputStreamReader(classLoader.getResourceAsStream(PRODUCTS)));
        AtomicInteger count = new AtomicInteger();
        int iterations = 5000;

        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "20");
        try (CSVReader reader = new CSVReader(productsReader)) {
            while ((productReader = reader.readNext()) != null) {

                // org.ts.stock.<LOC>.<SKU>
                String productId = productReader[0];
                String sku = productReader[1];
                Arrays.stream(locations).parallel().forEach(loc -> {
                    count.getAndIncrement();
                    jedis.hset("org:ts:stock:" + loc + ":" + sku,
                            Map.ofEntries(
                                    entry("productId", productId),
                                    entry("sku", sku),
                                    entry("siteId", loc.toString()),
                                    entry("quantity", String.valueOf(ThreadLocalRandom.current().nextInt(0, 10000 + 1)))
                            ));
                });

                log.info("Inserted {} records into Redis, {} iterations remaining", count.get(), --iterations);
            }
        }
    }
}
