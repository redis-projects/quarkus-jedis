package org.acme.redis.config;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvValidationException;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;
import org.acme.redis.model.StockInfo;
import org.acme.redis.utils.TimeseriesUtil;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.json.Path;
import redis.clients.jedis.timeseries.TSCreateParams;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
@Startup
@ApplicationScoped
public class JedisConfig {

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String STOCK_INFO = "data/nasdaq_top_250.csv";
    private static final String STOCK_FILE = "data/ibm_stock_prices.csv";
    private static final String STOCK_FILE_ADJUSTED = "data/ibm_stock_prices_adjusted.csv";
    private static final String COMMA_DELIMITER = ",";

    @ConfigProperty(name = "redis.bulk.load.data")
    boolean loadData;

    @ConfigProperty(name = "redis.stocks.ts.prefix")
    String tsPrefix;

    @ConfigProperty(name = "redis.stocks.json.prefix")
    String jsonPrefix;

    @Inject
    TimeseriesUtil tsUtils;

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


    private void loadData(@Observes StartupEvent ev, UnifiedJedis jedis) throws IOException, CsvValidationException {

        if (loadData) {

            // Serialise Company CSV data to Java Obj
            List<StockInfo> stockInfoList = new CsvToBeanBuilder(this.loadFile(STOCK_INFO))
                    .withType(StockInfo.class)
                    .build()
                    .parse();

            // Add Stock Ticker Company details to Redis as JSON
            stockInfoList.stream().forEach(stock -> {
                String key = jsonPrefix + stock.getSymbol();
                jedis.jsonSet(key, Path.ROOT_PATH, stock);

                // Clear down existing TS
                try {
                    jedis.del(tsPrefix + stock.getSymbol());
                    log.info("Successfully deleted timeseries : {} ", tsPrefix + stock.getSymbol());
                } catch (Exception e) {

                }

            });

            // Add original timeseries data set
            this.processTimeseriesData(jedis, STOCK_FILE, false);

            // Add adjusted timeseries data set
            this.processTimeseriesData(jedis, STOCK_FILE_ADJUSTED, true);
        }
    }

    private BufferedReader loadFile(String fileName) {
        // Read file from disk
        ClassLoader classLoader = this.getClass().getClassLoader();
        return new BufferedReader(new
                InputStreamReader(classLoader.getResourceAsStream(fileName)));
    }

    private void processTimeseriesData(UnifiedJedis jedis, String filename, boolean adjusted) throws IOException, CsvValidationException {

        // Read Timeseries CSV from file
        BufferedReader reader = this.loadFile(filename);
        CSVReader csvReader = new CSVReaderBuilder(reader)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                .withSkipLines(1) // Skip the header
                .build();

        Set<String> timeSeriesKeys = new HashSet<>();
        // Iterate over file and process timeseries into Redis
        String[] line;
        while ((line = csvReader.readNext()) != null) {
            String ticker = line[0];

            if (StringUtils.isNotEmpty(ticker)) {
                String key = tsPrefix + ticker;
                Double cobValue = Double.parseDouble(line[2]);
                String date = line[7];

                Map<String, String> labels = new HashMap<>();
                labels.put("STOCK", ticker);
                log.debug("Ticker: {}, Double: {}, Date: {} - {} ", ticker, cobValue, date, tsUtils.getUTCTimestamp(date, DATE_PATTERN));

                // Place Retrospective updates/adjustments into seperate TimeSeries
                if (adjusted) {
                    String adjustedDate = String.valueOf(tsUtils.getUTCTimestamp(line[8], DATE_PATTERN));
                    log.debug("Adding adjustable date: {}", adjustedDate);
                    labels.put("ADJUSTED", adjustedDate);
                    key = key + ":" + adjustedDate;
                }

                // Push TS to Redis
                labels.put("KEY", key);
                jedis.tsAdd(key, tsUtils.getUTCTimestamp(date, DATE_PATTERN), cobValue, new TSCreateParams().labels(labels));
                timeSeriesKeys.add(key);
            }
        }

        log.info("The following Timeseries Keys have been added to Redis: {}", timeSeriesKeys);
        reader.close();
        csvReader.close();
    }
}
