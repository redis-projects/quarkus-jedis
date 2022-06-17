package org.acme.redis.utils;

import redis.clients.jedis.search.Query;

import javax.inject.Singleton;

@Singleton
public class SearchUtil {

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

    private boolean intNotNullOrEmpty(Integer value) {
        if (value != null && value > 0) {
            return true;
        }
        return false;
    }
}
