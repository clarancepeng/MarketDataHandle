package org.alige.data.md.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class OptionPrice {
    private int tdate;
    private int uptime;
    private int origintime;
    private int origintimems;
    private String optid;
    private double presettle;
    private double settlepx;
    private double openpx;
    private double highpx;
    private double lowpx;
    private double lastpx;
    private double closepx;
    private double auctionpx;
    private long auctionqty;
    private long openints;
    private long volume;
    private double value;
    private double b1;
    private int bv1;
    private double b2;
    private int bv2;
    private double b3;
    private int bv3;
    private double b4;
    private int bv4;
    private double b5;
    private int bv5;

    private double s1;
    private int sv1;
    private double s2;
    private int sv2;
    private double s3;
    private int sv3;
    private double s4;
    private int sv4;
    private double s5;
    private int sv5;

    private String localtime;
    private String market;
    private long unix;
    private String source;
    private int datastatus;
    private String tradingphase;
    private int imagestatus;
    private int seqno;
    private double preclosepx;
    private long numtrade;
}
