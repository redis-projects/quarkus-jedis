package org.acme.redis.type;

import java.util.Arrays;

public enum RelationshipENUM {

    ACTOR("ACTED_IN", "Actor", "a"),
    GENRE("IN_GENRE", "Genre", "g"),
    DIRECTOR("DIRECTED", "Director", "d");

    private String relationship;
    private String objectRef;
    private String objectId;

    RelationshipENUM(String relationship, String objectRef, String objectId) {
        this.relationship = relationship;
        this.objectRef = objectRef;
        this.objectId = objectId;
    }

    public String getRelationship() {
        return this.relationship;
    }

    public String getObjectRef() {
        return this.objectRef;
    }

    public String getObjectId() {
        return this.objectId;
    }

    public static RelationshipENUM fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(RelationshipENUM.values())
                .filter(v -> v.relationship.equals(s))
                .findFirst()
                .orElse(null);
    }
}
