package org.acme.redis.config;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.json.Path;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.acme.redis.model.Transaction;

@Slf4j
@Startup
@ApplicationScoped
public class TransactionGenerator {

    @Inject
    UnifiedJedis jedis;

    private static final String PRODUCT_KEYS = "product_keys.csv";

    @ConfigProperty(name = "redis.generate.transactions")
    boolean loadTx;

    @ConfigProperty(name = "redis.generate.transactions.amount")
    int txCount;

    private static String[] houseHoldSegs = {"MIDDLE AGED SINGLE:3", "MIDDLE AGED COUPLE:6", "SINGLE PARENT:2", "RETIRED COUPLE WITH CHILDREN:10", "YOUNG SINGLE (NO CHILDREN):1"};
    private static String[] healthSegs = {"NATURALLY HEALTHY:5", "HEALTH ENTHUSIASTS:6", "CASUAL LIFESTYLE:4", "NATURALLY HEALTHY:6"};
    private static String[] shoppingSegs = {"FOODIES", "ON TREND CHEFS", "VALUE FOR MONEY"};
    private static String[] statusSegs = {"GOLD", "SILVER", "BRONZE", "NONE"};

    private List<String> productKeys;

    void onStart(@Observes StartupEvent ev) throws IOException, CsvException {
        if (loadTx) {
            productKeys = this.loadAllProductKeys();
            log.info("Generating {} tx's : using {} product keys", txCount, productKeys.size());
            this.generateTransactions(txCount);
        }
    }

    private void generateTransactions(int transactionCount) throws IOException {
        log.info("Generating {} products", transactionCount);
        IntStream.range(1, transactionCount + 1).parallel().forEach(i -> {
            log.info("Inserting product id {} of {}", i, transactionCount);
            jedis.jsonSet("org:sainsburys:tx:" + i, Path.ROOT_PATH, this.generateTransaction(i));
        });
        log.info("Finished inserting products");
    }

    /**
     * Transaction(id=1, productKey=0,
     * customerKey=2d113457-533b-4407-a944-b74e510d2bd3,
     * loyaltyId=07b17e4b-21c6-45d9-9acb-9aad934df3a9,
     * inferredCustomerId=61ed9e60-9028-426c-82ba-f110f0e1e979,
     * quantity=2, householdId=70000036254603, gender=1,
     * ageRangeFrom=25, ageRangeTo=34,
     * householdSeg=10,
     * householdSegDesc=RETIRED COUPLE WITH CHILDREN,
     * healthSegDesc=CASUAL LIFESTYLE,
     * shoppingGroup=4,
     * longTermControl=false,
     * shoppingSeg=ON TREND CHEFS,
     * statusSeg=BRONZE,
     * dapFlag=true)
     *
     * @param id
     * @return
     */
    private Transaction generateTransaction(long id) {

        int from = new Random().nextBoolean() ? 25 : 55;
        int to = from == 25 ? 34 : 64;
        String hhs = this.RandomHouseholdSeg();
        String health = this.RandomHealthSeg();
        String status = this.RandomStatus();

        return new Transaction(
                id,
                this.RandomProductKey(), // productId
                UUID.randomUUID().toString(), // customerKey
                UUID.randomUUID().toString(), // loyaltyId
                UUID.randomUUID().toString(), // inferredCustomerId
                ThreadLocalRandom.current().nextInt(1, 3 + 1), // Quantity per Unit
                70000036254603l, // householdId
                Math.toIntExact(Math.round(Math.random())), // gender
                from, to, // ageRange
                Integer.valueOf(hhs.substring(hhs.lastIndexOf(":") + 1, hhs.length())), // householdSeg
                hhs.substring(0, hhs.lastIndexOf(":")), // householdSegDesc
                health.substring(0, health.lastIndexOf(":")), // healthSegDesc
                Integer.valueOf(health.substring(health.lastIndexOf(":") + 1, health.length())), // shoppingGroup
                false,
                this.RandomShoppingSeg(),
                status,
                status == "NONE" ? false : true
        );
    }

    private String RandomHouseholdSeg() {
        Random random = new Random();
        return houseHoldSegs[random.nextInt(houseHoldSegs.length)];
    }

    private String RandomHealthSeg() {
        Random random = new Random();
        return healthSegs[random.nextInt(healthSegs.length)];
    }

    private String RandomShoppingSeg() {
        Random random = new Random();
        return shoppingSegs[random.nextInt(shoppingSegs.length)];
    }

    private String RandomStatus() {
        Random random = new Random();
        return statusSegs[random.nextInt(statusSegs.length)];
    }

    private String RandomProductKey() {
        Random random = new Random();
        return productKeys.get(random.nextInt(productKeys.size()));
    }

    private List<String> loadAllProductKeys() throws IOException, CsvException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        BufferedReader productKeysReader = new BufferedReader(new
                InputStreamReader(classLoader.getResourceAsStream(PRODUCT_KEYS)));

        List<String[]> productKeyReader;
        try (CSVReader reader = new CSVReader(productKeysReader)) {
            productKeyReader = reader.readAll();
        }

        return Arrays.asList(productKeyReader.get(0));
    }
}