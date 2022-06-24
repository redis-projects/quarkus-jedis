package org.acme.redis.model.response;

import lombok.Data;

import java.util.List;

/**
 * Basic Pagination Wrapper
 */
@Data
public class Page {

    private int total;
    private int page;
    private long totalPage;
    private int limit;
    private List<Object> items;

    public Page(int total, int page, int limit, List<Object> items) {
        this.total = total;
        this.page = page;
        this.limit = limit;
        this.items = items;
        if (total > 0 && limit > 0){
            this.totalPage = Math.round((double) total / (double) limit);
        }
    }
}
