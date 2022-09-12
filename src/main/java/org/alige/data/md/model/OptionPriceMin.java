package org.alige.data.md.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class OptionPriceMin {
    private int datadate;
    private String optid;
    private String exchangecd;
    private int utcoffect;
    private String bartime;
    private double closeprice;
    private double openprice;
    private double highprice;
    private double lowprice;
    private long volume;
    private double value;
    private double vwap;
    private long openints;
}
