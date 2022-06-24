package org.acme.redis.utils;

import lombok.extern.slf4j.Slf4j;
import org.acme.redis.model.TSResultSet;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.timeseries.TSElement;
import redis.clients.jedis.timeseries.TSKeyedElements;

import javax.inject.Singleton;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class TimeseriesUtil {

    /**
     * Add escape symbols for 'Special Characters' in Strings but does not include brackets
     *
     * @param inputString
     * @return
     */
    public String escapeMetaCharacters(String inputString) {
        final String[] metaCharacters = {"\\", "^", "$", ".", "*", "+", "?", "|", "<", ">", "-", "&", "%"};

        for (int i = 0; i < metaCharacters.length; i++) {
            if (inputString.contains(metaCharacters[i])) {
                inputString = inputString.replace(metaCharacters[i], "\\" + metaCharacters[i]);
            }
        }
        return inputString;
    }

    public String wrapInParentheses(String query) {
        return "(" + query + ")";
    }

    public String wrapInCurlyBraces(String query) {
        return "{" + query + "}";
    }

    /**
     * Super basic pagination handle...
     *
     * @param query
     * @param page
     * @param offset
     * @return
     */
    public Query handlePagination(Query query, Integer page, Integer offset) {
        // Offset with no page number
        if (intNotNullOrEmpty(offset) && !intNotNullOrEmpty(page)) {
            query.limit(0, offset);
        }

        // Offset with desired page number
        if (intNotNullOrEmpty(offset) && intNotNullOrEmpty(page)) {
            int base = offset * page;
            query.limit(base, base + offset);
        }
        return query;
    }

    /**
     * Nullchecker
     *
     * @param value
     * @return
     */
    private boolean intNotNullOrEmpty(Integer value) {
        if (value != null && value > 0) {
            return true;
        }
        return false;
    }

    /**
     * Get UTC date from String date pattern
     *
     * @param date
     * @return
     */
    public Long getUTCTimestamp(String date, String DATE_PATTERN) {
        if (date != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            LocalDate localDate = LocalDate.parse(date, formatter);
            return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else {
            return null;
        }
    }

    /**
     * Test the provided String is a valid date for a given format
     *
     * @param date
     * @param DATE_PATTERN
     * @return
     */
    public boolean isValidDate(String date, String DATE_PATTERN) {
        DateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        sdf.setLenient(false);
        try {
            sdf.parse(date);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    /**
     * Iterate over TS and merge together
     * 1. Parse List<TSKeyedElements> into TreeMap<TS_ADJUSTED_DATE, TS>
     * 2. Creates a TSResultsSets with the original TS
     * 3. Merges in Ascending order the Adjusted TS into the Original TS
     * 3 a. Latest (adjusted) values are always provided
     *
     * @param timeseriesResults
     * @return
     */
    public TSResultSet flattenTS(List<TSKeyedElements> timeseriesResults) throws Exception {

        // Move results into a Sorted (ascending) TreeMap
        TreeMap<Long, TSKeyedElements> tsKeyedElementsMap = timeseriesResults.stream().collect(Collectors.toMap(
                tsk -> this.getTimeStampFromKey(tsk.getKey()),
                Function.identity(),
                (tsk1, tsk2) -> tsk1,
                TreeMap::new));

        if (tsKeyedElementsMap.containsKey(0l)) {

            // Create ResultsSet from original TS
            TSResultSet tsResultSet = new TSResultSet(tsKeyedElementsMap.get(0l));
            tsResultSet.setKey(tsKeyedElementsMap.get(0l).getKey());

            // Flatten the TS into a single TS
            // NOTE: TreeMap is
            for (var entry : tsKeyedElementsMap.entrySet()) {

                // Original TS has already been merged
                if (entry.getKey().equals(0l)) {
                    continue;
                } else {
                    log.info("Merging TS {} into {}", entry.getValue().getKey(),
                            entry.getValue().getKey().substring(0, entry.getValue().getKey().lastIndexOf(":")));

                    // Merge all adjusted TS into ResultsSet
                    tsResultSet.getAdjustedKeys().add(entry.getValue().getKey());
                    tsResultSet.getValues().putAll(
                            entry.getValue().getValue().stream().collect(Collectors.toMap(
                                    TSElement::getTimestamp,
                                    TSElement::getValue))
                    );
                }
            }

            tsResultSet.setTsSize(tsResultSet.getValues().size());
            return tsResultSet;
        } else {
            throw new Exception("Original TS not found... something went wrong");
        }
    }

    /**
     * Pulls timestamp from TS KEY redis:key:space:<TIMESTAMP>
     *
     * @param key
     * @return
     */
    public long getTimeStampFromKey(String key) {
        try {
            return Long.parseLong(StringUtils.substringAfterLast(key, ":"));
        } catch (NumberFormatException nfe) {
            return 0l;
        }
    }
}
