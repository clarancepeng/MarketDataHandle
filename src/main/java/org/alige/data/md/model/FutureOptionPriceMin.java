package org.alige.data.md.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class FutureOptionPriceMin {
    private int clearingDay;
    private int tdate;
    private String contractId;
    private int offset;
    private String market;
    private String barTime;
    private double closePx;
    private double openPx;
    private double highPx;
    private double lowPx;
    private int volume;
    private double value;
    private double vwap;
    private int openInts;
}
