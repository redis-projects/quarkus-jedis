package org.acme.redis.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@RegisterForReflection
public class Person {

    private long identity;
    private List<String> labels;
    private long tmdbId;
    private String imdbId;
    private String name;
    private String bio;
    private String bornIn;
    private String born;
    private String died;
    private String poster;
    private String url;
    private Long bornUTC;
    private Long diedUTC;
}
