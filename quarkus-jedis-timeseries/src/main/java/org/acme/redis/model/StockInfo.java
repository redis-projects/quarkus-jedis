package org.acme.redis.model;

import com.opencsv.bean.CsvBindByName;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@RegisterForReflection
public class StockInfo {

    @CsvBindByName(column = "Symbol")
    String symbol;

    @CsvBindByName(column = "Name")
    String name;

    @CsvBindByName(column = "Last Sale")
    String lastSale;

    @CsvBindByName(column = "Net Change")
    String netChange;

    @CsvBindByName(column = "% Change")
    String percentChange;

    @CsvBindByName(column = "Market Cap")
    Double marketCap;

    @CsvBindByName(column = "Country")
    String country;

    @CsvBindByName(column = "IPO Year")
    Long IPOYear;

    @CsvBindByName(column = "Volume")
    Long volume;

    @CsvBindByName(column = "Sector")
    String sector;

    @CsvBindByName(column = "Industry")
    String industry;

}
