package org.acme.redis.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
public class Movie {

    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private long id;
    private String title;
    private int year;
    private String imdbId;
    private int runtime;
    private int duration;
    private double imdbRating;
    private long movieId;
    private List<String> languages;
    private List<String> countries;
    private long revenue;
    private long budget;
    private long tmdbId;
    private long imdbVotes;
    private String released;
    private String plot;
    private String tagline;
    private String url;
    private String poster_image;
    private String poster;

    public LocalDate getReleaseDate() {
        LocalDate ld = null;
        if (this.released != null || !this.released.isEmpty()) {
            ld = LocalDate.parse(this.released, dtf);
        }
        return ld;
    }
}
