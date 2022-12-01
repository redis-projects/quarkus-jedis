package org.acme.redis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private long id;
    private String name;
    private String sku;
    private String supplierId;
    private String categoryId;
    private int quantityPerUnit;
    private double unitPrice;
    private boolean discontinued;
}
