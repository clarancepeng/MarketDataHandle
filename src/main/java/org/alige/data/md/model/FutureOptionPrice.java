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
    private int clearingDate;
    private int ttime;
    private String market;
    private double lastPx;
    private double preSettle;
    private double preClose;
    private double initOpenInts;
    private double openPx;
    private double highPx;
    private double lowPx;
    private long volume;
    private double value;
    private long openInts;
    private double closePx;
    private double settlePx;
    private double upperLimit;
    private double lowerLimit;
    private double preDelta;
    private double currDelta;
    private double avgPx;
    private double b1;
    private int bv1;
    private double s1;
    private int sv1;
    private double b2;
    private int bv2;
    private double b3;
    private int bv3;
    private double b4;
    private int bv4;
    private double b5;
    private int bv5;
    private double s2;
    private int sv2;
    private double s3;
    private int sv3;
    private double s4;
    private int sv4;
    private double s5;
    private int sv5;
    private String varieties;
    private String tflag;
    private String localtime;
    private int seqNo;
}
