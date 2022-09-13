package org.alige.data.md.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class IndexOptionPriceMin {
    private int tdate;
    private String instrumentId;
    private String market;
    private String barTime;
    private double openPrice;
    private double highPrice;
    private double lowPrice;
    private double closePrice;
    private double totalVolume;
    private double totalValue;
    private double vwap;
    private long openInterest;
}
