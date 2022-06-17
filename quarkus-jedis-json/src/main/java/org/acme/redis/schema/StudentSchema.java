package org.acme.redis.schema;

import lombok.Getter;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.Schema;

import javax.inject.Singleton;

@Getter
@Singleton
public class StudentSchema {

    public final static String STUDENT_INDEX = "idx:student";
    private final Schema studentSchema;

    public final IndexDefinition indexDefinition = new IndexDefinition(IndexDefinition.Type.JSON)
            .setPrefixes(new String[]{"student:"});

    /**
     * Add Searchable fields to Schema by Type.
     */
    public StudentSchema() {
        studentSchema = new Schema();
        studentSchema.addSortableTextField("$.firstName", 1.0);
        studentSchema.addSortableTextField("$.lastName", 1.0);
        studentSchema.addTagField("$.gender");
        studentSchema.addTagField("$.city");
        studentSchema.addTagField("$.company");
        studentSchema.addTagField("$.preferredColour");
        studentSchema.addTextField("$.plannedCareer", 1.0);
        studentSchema.addTextField("$.university", 1.0);
    }
}
