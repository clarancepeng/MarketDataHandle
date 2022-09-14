package org.alige.data.md.service;

import lombok.extern.slf4j.Slf4j;
import org.alige.data.md.model.FutureOptionPrice;
import org.alige.data.md.model.FutureOptionPriceMin;
import org.alige.data.md.model.IndexOptionPrice;
import org.alige.data.md.model.IndexOptionPriceMin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.alige.data.md.utils.Utils.*;

@Service
@Slf4j
public class FutureOptionImportService {
    @Autowired
    private FutureOptionPriceService indexOptionPriceService;

    @Value("${future_opt_price.data.dir}")
    private String optionPriceDataDir;

    @Value("${future_opt_pricemin.data.dir}")
    private String optionPriceMinDataDir;

    @Value("${future_opt_price03min.data.dir}")
    private String optionPrice03MinDataDir;

    @Value("${future_opt_price05min.data.dir}")
    private String optionPrice05MinDataDir;

    @Value("${future_opt_price15min.data.dir}")
    private String optionPrice15MinDataDir;

    @Value("${future_opt_price30min.data.dir}")
    private String optionPrice30MinDataDir;

    @Value("${future_opt_price60min.data.dir}")
    private String optionPrice60MinDataDir;

    @Value("${future_opt_price.data.batch_size: 300}")
    private int optionPriceDataBatchSize = 300;

    @Value("${future_opt_pricemin.data.batch_size: 300}")
    private int optionPriceMinDataBatchSize = 300;

    public void importAllFutureOption() {
        try {
            log.info("Begin to import Future Option Tick Data");
            zipImportOptionTickData();
            log.info("End to import Future Option Tick Data");
            log.info("Begin to import Future Option One Minute Data");
            zipImportOptionMinData("", optionPriceMinDataDir);
            log.info("End to import Future Option One Minute Data");
            log.info("Begin to import Future Option Three Minutes Data");
            zipImportOptionMinData("03", optionPrice03MinDataDir);
            log.info("End to import Future Option Three Minutes Data");
            log.info("Begin to import Future Option Five Minutes Data");
            zipImportOptionMinData("05", optionPrice05MinDataDir);
            log.info("End to import Future Option Five Minutes Data");
            log.info("Begin to import Future Option Fifteen Minutes Data");
            zipImportOptionMinData("15", optionPrice15MinDataDir);
            log.info("End to import Future Option Fifteen Minutes Data");
            log.info("Begin to import Future Option Thirty Minutes Data");
            zipImportOptionMinData("30", optionPrice30MinDataDir);
            log.info("End to import Future Option One Minute Data");
            log.info("Begin to import Future Option Sixty Minutes Data");
            zipImportOptionMinData("60", optionPrice60MinDataDir);
            log.info("End to import Future Option Sixty Minutes Data");
        } catch (Exception e) {
            log.error("", e);
        }
    }
    public void zipImportOptionTickData() {
        try {
            Path path = Paths.get(optionPriceDataDir);
//            Set<String> fileList = new HashSet<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path mp : stream) {
                    String monthPath = mp.getFileName().toString();
                    log.info("Prepare to handle Monthly Data[{}]", monthPath);
                    if (!Files.isDirectory(mp)) {
                        log.info("Directory [{}] is a file", mp.getFileName());
                        continue;
                    }
                    try (Stream<Path> sp = Files.list(mp)) {
                        sp.sorted().forEach(p -> {
                            String zipFileName = p.getFileName().toString();
                            if (zipFileName.endsWith("zip")) {
                                try {
                                    log.info("Zip File: {}", p.getFileName());
                                    final ZipFile zipFile = new ZipFile(p.toFile());
                                    final Enumeration<? extends ZipEntry> entries = zipFile.entries();
                                    ZipInputStream zipInput = null;

                                    while (entries.hasMoreElements()) {
                                        try {
                                            final ZipEntry zipEntry = entries.nextElement();
                                            if (!zipEntry.isDirectory()) {
                                                final String fileName = zipEntry.getName();
                                                //                                System.out.println("Handle the csv files: " + fileName);
                                                log.info("Handle the csv files: {}", fileName);
                                                int recordDate = guessDate(fileName);
                                                String tableName = new StringBuilder().append("future_opt_price_").append(recordDate).toString();
                                                String checkTable = indexOptionPriceService.checkTableExist("public", tableName);
                                                log.info("Check the tableName[{} - {}] from tableSchema[public]", tableName, checkTable);
                                                try {
                                                    if (checkTable == null) {
                                                        String tablePK = new StringBuilder().append("future_opt_price_").append(recordDate).append("_pk").toString();
                                                        log.info("Start to create Table[{}], Primary Key={}", tableName, tablePK);
                                                        indexOptionPriceService.createOptionPriceTable(tableName, tablePK);
                                                        log.info("End to create Table[{}]", tableName);
                                                    } else {
                                                        log.info("Table[{}] was exists! Skip to create and load data", tableName);
                                                        continue;
                                                    }
                                                } catch (Exception e) {
                                                    log.warn("Table[{}] is exist", tableName, e);
                                                }
                                                if (fileName.endsWith(".txt")) {
                                                    try (BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)))) {
                                                        String line;
                                                        String colName = br.readLine();
                                                        log.info("Content Header = {}", colName);
                                                        int dataCount = 0;
                                                        long start = System.currentTimeMillis();
                                                        long start2 = start;
                                                        List<FutureOptionPrice> priceList = new ArrayList<>(optionPriceDataBatchSize);
                                                        while ((line = br.readLine()) != null) {
                                                            //                                            log.info(line);
                                                            try {
                                                                String[] d = line.split("\t");
                                                                FutureOptionPrice optionPrice = new FutureOptionPrice();
                                                                optionPrice.setContractId(d[0]);
                                                                optionPrice.setTdate(toInt(d[1]));
                                                                optionPrice.setClearingDay(toInt(d[2]));
                                                                optionPrice.setTtime(toInt(d[3]));
                                                                optionPrice.setMarket(d[4]);
                                                                optionPrice.setLastPx(toDouble(d[5]));
                                                                optionPrice.setPreSettle(toDouble(d[6]));
                                                                optionPrice.setPreClose(toDouble(d[7]));
                                                                optionPrice.setInitOpenInts(toInt(d[8]));
                                                                optionPrice.setOpenPx(toDouble(d[9]));
                                                                optionPrice.setHighPx(toDouble(d[10]));
                                                                optionPrice.setLowPx(toDouble(d[11]));
                                                                optionPrice.setVolume(toInt(d[12]));
                                                                optionPrice.setValue(toDouble(d[13]));
                                                                optionPrice.setOpenInts(toInt(d[14]));
                                                                optionPrice.setClosePx(toDouble(d[15]));
                                                                optionPrice.setSettlePx(toDouble(d[16]));
                                                                optionPrice.setUpperLimit(toDouble(d[17]));
                                                                optionPrice.setLowerLimit(toDouble(d[18]));
                                                                optionPrice.setPreDelta(toDouble(d[19]));
                                                                optionPrice.setCurrDelta(toDouble(d[20]));
                                                                optionPrice.setAvgPx(toDouble(d[21]));

                                                                optionPrice.setB1(toDouble(d[22]));
                                                                optionPrice.setBv1(toInt(d[23]));
                                                                optionPrice.setS1(toDouble(d[24]));
                                                                optionPrice.setSv1(toInt(d[25]));

                                                                optionPrice.setB2(toDouble(d[26]));
                                                                optionPrice.setBv2(toInt(d[27]));
                                                                optionPrice.setB3(toDouble(d[28]));
                                                                optionPrice.setBv3(toInt(d[29]));
                                                                optionPrice.setB4(toDouble(d[30]));
                                                                optionPrice.setBv4(toInt(d[31]));
                                                                optionPrice.setB5(toDouble(d[32]));
                                                                optionPrice.setBv5(toInt(d[33]));
                                                                optionPrice.setS2(toDouble(d[34]));
                                                                optionPrice.setSv2(toInt(d[35]));
                                                                optionPrice.setS3(toDouble(d[36]));
                                                                optionPrice.setSv3(toInt(d[37]));
                                                                optionPrice.setS4(toDouble(d[38]));
                                                                optionPrice.setSv4(toInt(d[39]));
                                                                optionPrice.setS5(toDouble(d[40]));
                                                                optionPrice.setSv5(toInt(d[41]));
                                                                optionPrice.setCommodityNo(d[42]);
                                                                optionPrice.setTflag(d[43]);
                                                                optionPrice.setLocalTm(toInt(d[44]));
                                                                optionPrice.setSeqNo(toInt(d[45]));

                                                                //optionPriceService.insertOptionPrice(tableName, optionPrice);
                                                                priceList.add(optionPrice);
                                                                ++dataCount;
                                                                if (dataCount % optionPriceDataBatchSize == 0) {
                                                                    indexOptionPriceService.insertOptionPriceBatch(tableName, priceList);
                                                                    priceList.clear();
                                                                    long end = System.currentTimeMillis();
                                                                    log.info("It costs {}s per {} records", (end - start) / 1000.0, optionPriceDataBatchSize);
                                                                    start = end;
                                                                }
                                                                if (dataCount % 100_000 == 0) {
                                                                    long end2 = System.currentTimeMillis();
                                                                    log.info("*** Persist {} records on {}, it costs {}s per {} records ***", dataCount, tableName, (end2 - start2) / 1000.0, 100_000, 100_000);
                                                                    start2 = end2;
                                                                }
                                                            } catch (Exception e) {
                                                                log.error("", e);
                                                            }

                                                            //                                            System.out.println(Arrays.asList(d));
                                                        }
                                                        if (priceList.size() > 0) {
                                                            indexOptionPriceService.insertOptionPriceBatch(tableName, priceList);
                                                            priceList.clear();
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            log.error("", e);
                                        }
                                    }
                                    zipFile.close();
                                } catch (Exception e) {
                                    log.error("", e);
                                }

                            }
                        });
                    }
                }
            }

        } catch (final Exception ioe) {
            System.err.println("Unhandled exception:");
            ioe.printStackTrace();
            return;
        }
    }

    public void zipImportOptionMinData(String minType, String dataDir) {
        try {
            Path path = Paths.get(dataDir);
//            Set<String> fileList = new HashSet<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path mp : stream) {
                    String monthPath = mp.getFileName().toString();
                    log.info("Prepare to handle Monthly Data[{}]", monthPath);
                    if (!Files.isDirectory(mp)) {
                        log.info("Directory [{}] is a file", mp.getFileName());
                        continue;
                    }
                    try (Stream<Path> sp = Files.list(mp)) {
                        sp.sorted().forEach(p -> {
                            String zipFileName = p.getFileName().toString();
                            if (zipFileName.endsWith("zip")) {
                                try {
                                    log.info("Zip File: {}", p.getFileName());
                                    final ZipFile zipFile = new ZipFile(p.toFile());
                                    final Enumeration<? extends ZipEntry> entries = zipFile.entries();
                                    ZipInputStream zipInput = null;

                                    while (entries.hasMoreElements()) {
                                        try {
                                            final ZipEntry zipEntry = entries.nextElement();
                                            if (!zipEntry.isDirectory()) {
                                                final String fileName = zipEntry.getName();
                                                //                                System.out.println("Handle the csv files: " + fileName);
                                                log.info("Handle the csv files: {}", fileName);
                                                int recordDate = guessDate(fileName);
                                                String tableName = new StringBuilder().append("option_price").append(minType).append("min_").append(recordDate).toString();
                                                String checkTable = indexOptionPriceService.checkTableExist("public", tableName);
                                                log.info("Check the tableName[{} - {}] from tableSchema[public]", tableName, checkTable);
                                                try {
                                                    if (checkTable == null) {
                                                        String tablePK = new StringBuilder().append("option_price").append(minType).append("min_").append(recordDate).append("_pk").toString();
                                                        log.info("Start to create Minute Table[{}], Primary Key={}", tableName, tablePK);
                                                        indexOptionPriceService.createOptionPriceMinTable(tableName, tablePK);
                                                        log.info("End to create Minute Table[{}]", tableName);
                                                    } else {
                                                        log.info("Table[{}] was exists! Skip to create and load data", tableName);
                                                        continue;
                                                    }
                                                } catch (Exception e) {
                                                    log.warn("Table[{}] is exist", tableName, e);
                                                }
                                                if (fileName.endsWith(".txt")) {
                                                    try (BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)))) {
                                                        String line;
                                                        String colName = br.readLine();
                                                        log.info("Content Header = {}", colName);
                                                        int dataCount = 0;
                                                        long start = System.currentTimeMillis();
                                                        long start2 = start;
                                                        List<FutureOptionPriceMin> priceList = new ArrayList<>(optionPriceMinDataBatchSize);
                                                        while ((line = br.readLine()) != null) {
                                                            //                                            log.info(line);
                                                            try {
                                                                String[] d = line.split("\t");
                                                                FutureOptionPriceMin optionPrice = new FutureOptionPriceMin();
                                                                optionPrice.setClearingDay(toInt(d[0]));
                                                                optionPrice.setTdate(toInt(d[1]));
                                                                optionPrice.setContractId(d[2]);
                                                                optionPrice.setMarket(d[3]);
                                                                optionPrice.setOffset(toInt(d[4]));
                                                                optionPrice.setBarTime(d[5]);
                                                                optionPrice.setClosePx(toDouble(d[6]));
                                                                optionPrice.setOpenPx(toDouble(d[7]));
                                                                optionPrice.setHighPx(toDouble(d[8]));
                                                                optionPrice.setLowPx(toDouble(d[9]));
                                                                optionPrice.setVolume(toInt(d[10]));
                                                                optionPrice.setValue(toDouble(d[11]));
                                                                optionPrice.setVwap(toDouble(d[12]));
                                                                optionPrice.setOpenInts(toInt(d[13]));
                                                                //optionPriceService.insertOptionPrice(tableName, optionPrice);
                                                                priceList.add(optionPrice);
                                                                ++dataCount;
                                                                if (dataCount % optionPriceDataBatchSize == 0) {
                                                                    indexOptionPriceService.insertOptionPriceMinBatch(tableName, priceList);
                                                                    priceList.clear();
                                                                    long end = System.currentTimeMillis();
                                                                    log.info("It costs {}s per {} records", (end - start) / 1000.0, optionPriceMinDataBatchSize);
                                                                    start = end;
                                                                }
                                                                if (dataCount % 100_000 == 0) {
                                                                    long end2 = System.currentTimeMillis();
                                                                    log.info("*** Persist {} records on {}, it costs {}s per {} records ***", dataCount, tableName, (end2 - start2) / 1000.0, 100_000, 100_000);
                                                                    start2 = end2;
                                                                }
                                                            } catch (Exception e) {
                                                                log.error("", e);
                                                            }

                                                            //                                            System.out.println(Arrays.asList(d));
                                                        }
                                                        if (priceList.size() > 0) {
                                                            indexOptionPriceService.insertOptionPriceMinBatch(tableName, priceList);
                                                            priceList.clear();
                                                        }
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            log.error("", e);
                                        }
                                    }
                                    zipFile.close();
                                } catch (IOException e) {
                                    log.error("", e);
                                }

                            }
                        });
                    }
                }
            }

        } catch (final Exception ioe) {
            log.error("Unhandled exception:", ioe);
            return;
        }
    }

}
