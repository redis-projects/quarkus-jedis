package org.acme.redis.services;

import lombok.extern.slf4j.Slf4j;
import org.acme.redis.model.TSResultSet;
import org.acme.redis.utils.TimeseriesUtil;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.timeseries.TSInfo;
import redis.clients.jedis.timeseries.TSKeyedElements;
import redis.clients.jedis.timeseries.TSMRangeParams;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.acme.redis.config.JedisConfig.DATE_PATTERN;

@Slf4j
@ApplicationScoped
public class StockService {

    @ConfigProperty(name = "redis.stocks.ts.prefix")
    String tsPrefix;

    @Inject
    UnifiedJedis jedis;

    @Inject
    TimeseriesUtil tsUtils;

    /**
     * Return all TimeSeries (Keys) with labels
     *
     * @param labels
     * @return
     */
    public List<String> queryIndex(List<String> labels) {
        log.info("Query Labels: {}", labels);
        return jedis.tsQueryIndex(labels.stream().toArray(String[]::new));
    }

    public List<TSInfo> getAllTSIndexInfoByTickerId(String key) {
        List<String> tsKeys = jedis.tsQueryIndex(new String[]{String.format("STOCK=%1$s", key)});
        return tsKeys.stream().map(k -> this.getTSIndexInfo(k)).collect(Collectors.toList());
    }

    public TSInfo getTSIndexInfoByTickerId(String id) {
        return this.getTSIndexInfo(tsPrefix + id);
    }

    public TSInfo getTSIndexInfo(String key) {
        return jedis.tsInfo(key);
    }

    /**
     * Returns a list of TS Keys that fall within the Adjusted 'AS_OF' date
     *
     * @param labels
     * @param date
     * @return
     */
    public Set<String> getTSAdjusted(List<String> labels, long date) {
        List<String> timeSeries = this.queryIndex(labels);
        log.info("Queried Keys: {}", timeSeries);
        return timeSeries.stream().filter(ts -> isTSWithinRange(date, ts)).collect(Collectors.toSet());
    }

    public TSResultSet getTSValuesAdjusted(String ticker, List<String> labels, String asOfDate, String fromDate, String toDate) throws Exception {

        long asOfDateUTC = tsUtils.getUTCTimestamp(asOfDate, DATE_PATTERN);
        if (asOfDateUTC < 0l) {
            asOfDateUTC = 0l;
        }

        // Add Keys to Labels
        Set<String> keys = this.getTSAdjusted(labels, asOfDateUTC);
        log.info("Keys: {}", keys);
        String keysLabel = String.format("KEY=(%1$s)", keys.stream().collect(Collectors.joining(",")));
        log.info("Labels: {}", keysLabel);
        long fromTimestamp = tsUtils.getUTCTimestamp(fromDate, DATE_PATTERN);
        long toTimestamp = tsUtils.getUTCTimestamp(toDate, DATE_PATTERN);

        // Build TSMRangeParams - Find by Labels
        TSMRangeParams tsmRangeParams = new TSMRangeParams(fromTimestamp, toTimestamp);
        tsmRangeParams.withLabels(true);
        tsmRangeParams.filter(new String[]{keysLabel});

        // Query for Multiple TS with Labels
        log.info("Retrieving {} TS from date: '{}' to date: '{}' within an as of date: {}", ticker, fromDate, toDate, asOfDate);
        List<TSKeyedElements> tsResults = this.getTSKeyedElements(tsmRangeParams, ticker);

        if (tsResults == null || tsResults.isEmpty()) {
            log.error("No timeseries found for this query");
            // handle empty resultset
        }

        // Build complete responses
        TSResultSet tsResultSet = tsUtils.flattenTS(tsResults);
        tsResultSet.setAsOfDate(asOfDate);
        tsResultSet.setAsOfDateUTC(tsUtils.getUTCTimestamp(asOfDate, DATE_PATTERN));
        tsResultSet.setFromDate(fromDate);
        tsResultSet.setFromDateUTC(fromTimestamp);
        tsResultSet.setToDate(toDate);
        tsResultSet.setToDateUTC(toTimestamp);
        return tsResultSet;
    }

    private List<TSKeyedElements> getTSKeyedElements(TSMRangeParams tsmRangeParams, String ticker) {
        long start = System.currentTimeMillis();
        List<TSKeyedElements> tsResults = jedis.tsMRange(tsmRangeParams);
        long end = System.currentTimeMillis();
        log.info("{} TS retrieval from Redis took: {}ms", ticker, end - start);
        return tsResults;
    }

    /**
     * Checks whether the TS Keys falls within the 'AS_OF' query date
     *
     * @param asOfDateUTC
     * @param tsKey
     * @return
     */
    private boolean isTSWithinRange(long asOfDateUTC, String tsKey) {
        long timestamp = this.getTimeStampFromKey(tsKey);
        log.info("Timestamp: {} vs {}", timestamp, asOfDateUTC);
        return timestamp <= asOfDateUTC;
    }

    /**
     * Get Timestamp from TS Key
     * NOTE: Adjusted TS will be appended with adjusted date UTC
     *
     * @param key
     * @return
     */
    private long getTimeStampFromKey(String key) {
        try {
            return Long.parseLong(StringUtils.substringAfterLast(key, ":"));
        } catch (NumberFormatException nfe) {
            return 0l;
        }
    }
}
