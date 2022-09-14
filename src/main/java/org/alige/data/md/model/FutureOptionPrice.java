package org.alige.data.md.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class FutureOptionPrice {
    private int tdate;
    private int ttime;
    private int updateMilliSec;
    private String contractId;
    private String exchangeInstId;
    private String contractName;
    private double lastPx;
    private double highPx;
    private double lowPx;
    private long cq;
    private double cm;
    private String qc;
    private int tq;
    private double tm;
    private int lastQty;
    private int initOpenInts;
    private int openInts;
    private int intsChg;
    private double upperLimit;
    private double lowerLimit;
    private double preSettle;
    private double preClose;
    private double s1;
    private double b1;
    private int sv1;
    private int bv1;
    private double openPx;
    private double closePx;
    private double settlePx;
    private double lifeLow;
    private double lifeHigh;
    private double avgPx;
    private int bidImplyQty;
    private int askImplyQty;
    private double s5;
    private double s4;
    private double s3;
    private double s2;
    private double b2;
    private double b3;
    private double b4;
    private double b5;
    private int sv5;
    private int sv4;
    private int sv3;
    private int sv2;
    private int bv2;
    private int bv3;
    private int bv4;
    private int bv5;
    private double preDelta;
    private double currDelta;
    private String localTm;
    private String market;
    private double chg;
    private double chgPct;
    private String settleGroupId;
    private int settleId;
    private int clearingDay;
    private String mflag;
    private String source;
    private String commodityNo;
    private int offset;
    private String tflag;
}
