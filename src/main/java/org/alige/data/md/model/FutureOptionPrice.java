package org.alige.data.md.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class FutureOptionPrice {
    private String contractId;
    private int tdate;
    private int clearingDay;
    private int ttime;
    private String market;
    private double lastPx;
    private double preSettle;
    private double preClose;
    private int initOpenInts;
    private double openPx;
    private double highPx;
    private double lowPx;
    private double volume;
    private double value;
    private double openInts;
    private double closePx;
    private double settlePx;
    private double upperLimit;
    private double lowerLimit;
    private double preDelta;
    private double currDelta;
    private double avgPx;
    private double b1;
    private double bv1;
    private double s1;
    private double sv1;
    private double b2;
    private double bv2;
    private double b3;
    private double bv3;
    private double b4;
    private double bv4;
    private double b5;
    private double bv5;
    private double s2;
    private double sv2;
    private double s3;
    private double sv3;
    private double s4;
    private double sv4;
    private double s5;
    private double sv5;
    private String commodityNo;
    private String tflag;
    private int localTm;
    private int seqNo;
}
