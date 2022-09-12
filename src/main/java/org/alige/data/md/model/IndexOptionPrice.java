package org.alige.data.md.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class IndexOptionPrice {
    private String instrumentId;
    private String market;
    private int tdate;
    private String ttime;
    private double lastPx;
    private double openPx;
    private double highPx;
    private double lowPx;
    private double volume;
    private double value;
    private double openInt;
    private double preSettlePx;
    private double preClosePx;
    private double preOpenInt;
    private double closePx;
    private double settlePx;
    private double upperLimit;
    private double lowerLimit;
    private double preDelta;
    private double currDelta;
    private double bidPrice1;
    private double bidVolume1;
    private double askPrice1;
    private double askVolume1;
    private double bidPrice2;
    private double bidVolume2;
    private double askPrice2;
    private double askVolume2;
    private double bidPrice3;
    private double bidVolume3;
    private double askPrice3;
    private double askVolume3;
    private double bidPrice4;
    private double bidVolume4;
    private double askPrice4;
    private double askVolume4;
    private double bidPrice5;
    private double bidVolume5;
    private double askPrice5;
    private double askVolume5;
}
