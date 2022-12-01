package org.acme.redis.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private long id;
    private String productKey;
    private String customerKey;
    private String loyaltyId;
    private String inferredCustomerId;
    private int quantity;
    private long householdId;
    private int gender;
    private int ageRangeFrom;
    private int ageRangeTo;
    private int householdSeg;
    private String householdSegDesc;
    private String healthSegDesc;
    private int shoppingGroup;
    private boolean longTermControl;
    private String shoppingSeg;
    private String statusSeg;
    private boolean dapFlag;
}
