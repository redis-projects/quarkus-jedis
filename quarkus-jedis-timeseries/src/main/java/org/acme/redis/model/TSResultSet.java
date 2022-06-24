package org.acme.redis.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.timeseries.TSElement;
import redis.clients.jedis.timeseries.TSKeyedElements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
public class TSResultSet {

    // Marshall TSKeyedElement object to Response Object
    public TSResultSet(TSKeyedElements tsKeyedElements) {
        this.key = tsKeyedElements.getKey();
        this.labels = tsKeyedElements.getLabels();
        this.values = tsKeyedElements.getValue().stream().collect(
                Collectors.toMap(TSElement::getTimestamp, TSElement::getValue));
        this.adjustedKeys = new ArrayList<>();
    }

    private String key;
    private long tsSize;
    private String fromDate;
    private long fromDateUTC;
    private String toDate;
    private long toDateUTC;
    private String asOfDate;
    private long asOfDateUTC;
    private List<String> adjustedKeys;
    private Map<String, String> labels;
    private Map<Long, Double> values;

}
