package org.acme.redis.api;

import io.smallrye.common.constraint.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.acme.redis.model.TSResultSet;
import org.acme.redis.services.StockService;
import org.acme.redis.utils.TimeseriesUtil;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import redis.clients.jedis.timeseries.TSInfo;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

import static org.acme.redis.config.JedisConfig.DATE_PATTERN;

@Slf4j
@Path("/ts/stock")
public class TimeseriesResource {

    @Inject
    StockService stockService;

    @Inject
    TimeseriesUtil timeseriesUtil;

    // http://localhost:8080/ts/stock/info/ticker/IBM
    @GET
    @Path("/info/ticker/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Stock info By Stock ID",
            description = "Returns TSInfo of Stock by ticker ID e.g. 'IBM' ")
    public TSInfo getTSInfoByTickerId(@RestPath String id) {
        return stockService.getTSIndexInfoByTickerId(id);
    }

    // http://localhost:8080/ts/stock/info/all/IBM
    @GET
    @Path("/info/all/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get All Stock TS info By Stock ID",
            description = "Returns TSInfo for all TS associated to Stock by ticker ID e.g. 'IBM' ")
    public List<TSInfo> getAllTSInfoByTickerId(@RestPath String id) {
        return stockService.getAllTSIndexInfoByTickerId(id);
    }

    // http://localhost:8080/ts/stock/info/key/redis:stock:ts:IBM
    // http://localhost:8080/ts/stock/info/key/redis:stock:ts:IBM:1524524400000
    @GET
    @Path("/info/key/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Stock info By TS Key", description = "Returns TSInfo of Stock by TS Key e.g. 'redis:stock:ts:IBM' ")
    public TSInfo getTSInfoByKey(@RestPath String key) {
        return stockService.getTSIndexInfo(key);
    }

    // http://localhost:8080/ts/stock/?date=2012-05-01
    // BODY: ["STOCK=IBM"]
    @POST
    @Path("/keys")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Stock info By ID", description = "Returns Movie By ID")
    public Set<String> getTSAdjusted(@RestQuery String date, List<String> labels) {
        log.info("Searching for TS with Labels: {} on date: {}", labels, date);
        if (labels != null || !labels.isEmpty()) {
            return stockService.getTSAdjusted(labels, this.validateDate(date));
        } else {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("At least 1 TimeSeries Lable is required")
                            .build()
            );
        }
    }

    // http://localhost:8080/ts/stock/values/IBM?asOfDate=2018-05-01&fromDate=2010-01-01&toDate=2018-01-01
    // BODY: ["STOCK=IBM"]
    @POST
    @Path("/values/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get Stock info By ID", description = "Returns Movie By ID")
    public TSResultSet getTSAdjusted(@RestPath String id,
                                     @Nullable @RestQuery String asofDate,
                                     @RestQuery String fromDate,
                                     @RestQuery String toDate,
                                     List<String> labels) throws Exception {
        log.info("Searching for TS with Labels: {} on date: {}", labels, asofDate);
        if (StringUtils.isEmpty(asofDate)) {
            // return original timeseries
            asofDate = "1970-01-01";
        }
        return stockService.getTSValuesAdjusted(id.toUpperCase(), labels, asofDate, fromDate, toDate);
    }

    private long validateDate(String date) {

        long dateUTC = 0;
        if (timeseriesUtil.isValidDate(date, DATE_PATTERN)) {
            dateUTC = timeseriesUtil.getUTCTimestamp(date, DATE_PATTERN);
        }

        if (dateUTC < 0l) {
            dateUTC = 0l;
        }
        return dateUTC;
    }

}

