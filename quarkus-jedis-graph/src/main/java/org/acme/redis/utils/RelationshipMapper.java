package org.acme.redis.utils;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class RelationshipMapper {

    private String fromObjectName;
    private String fromObjectReference;
    private Map<String, Object> fromMap;
    private String toObjectName;
    private String toObjectReference;
    private Map<String, Object> toMap;

    public void setFromObjectName(String fromObjectName) {
        this.fromObjectName = fromObjectName;
        this.setFromObjectReference(fromObjectName.substring(0, 1).toLowerCase());
    }

    public void setToObjectReference(String toObjectReference) {
        this.toObjectReference = toObjectReference;
        this.setToObjectReference(toObjectReference.substring(0, 1).toLowerCase());
    }

    public List<String> toMapValueList() {
        return this.mapValueList(this.toMap);
    }

    public List<String> fromMapValueList() {
        return this.mapValueList(this.fromMap);
    }

    /**
     * Parameterisation of Map for Cypher Query
     * @param mapValues
     * @return
     */
    private List<String> mapValueList(Map<String, Object> mapValues) {
        return mapValues.keySet().stream()
                .map(k -> k + ": $" + k).collect(Collectors.toList());
    }

}
