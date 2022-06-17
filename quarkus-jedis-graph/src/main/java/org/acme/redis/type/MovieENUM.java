package org.acme.redis.type;

import java.util.Arrays;

public enum MovieENUM {

    ID("id"),
    MOVIE_ID("movieId"),
    TMDB_ID("tmdbId"),
    IMDB_ID("imdbId"),
    TITLE("title"),
    YEAR("year"),
    RUNTIME("runtime"),
    DURATION("duration"),
    IMDB_RATING("imdbRating"),
    LANGUAGES("languages"),
    REVENUE("revenue"),
    BUDGET("budget"),
    IMDB_VOTES("imdbVotes"),
    RELEASED("released"),
    PLOT("plot"),
    TAGLINE("tagline"),
    URL("url"),
    POSTER_IMAGE("poster_image"),
    POSTER("poster");

    private String fieldId;

    MovieENUM(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldId() {
        return this.fieldId;
    }

    public static MovieENUM fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(MovieENUM.values())
                .filter(v -> v.fieldId.equals(s))
                .findFirst()
                .orElse(null);
    }

}
