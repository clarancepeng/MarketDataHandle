package org.alige.data.md.service;

import lombok.extern.slf4j.Slf4j;
import org.alige.data.md.model.OptionPrice;
import org.alige.data.md.model.OptionPriceMin;
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
    private OptionPriceService optionPriceService;

    @Value("${option_price.data.dir}")
    private String optionPriceDataDir;

    @Value("${option_pricemin.data.dir}")
    private String optionPriceMinDataDir;

    @Value("${option_price03min.data.dir}")
    private String optionPrice03MinDataDir;

    @Value("${option_price05min.data.dir}")
    private String optionPrice05MinDataDir;

    @Value("${option_price15min.data.dir}")
    private String optionPrice15MinDataDir;

    @Value("${option_price30min.data.dir}")
    private String optionPrice30MinDataDir;

    @Value("${option_price60min.data.dir}")
    private String optionPrice60MinDataDir;

    @Value("${option_price.data.batch_size: 300}")
    private int optionPriceDataBatchSize = 300;

    @Value("${option_pricemin.data.batch_size: 300}")
    private int optionPriceMinDataBatchSize = 300;

    public void importAllStockOption() {
        try {
            log.info("Begin to import Stock Option Tick Data");
            zipImportOptionTickData();
            log.info("End to import Stock Option Tick Data");
            log.info("Begin to import Stock Option One Minute Data");
            zipImportOptionMinData("", optionPriceMinDataDir);
            log.info("End to import Stock Option One Minute Data");
            log.info("Begin to import Stock Option Three Minutes Data");
            zipImportOptionMinData("03", optionPrice03MinDataDir);
            log.info("End to import Stock Option Three Minutes Data");
            log.info("Begin to import Stock Option Five Minutes Data");
            zipImportOptionMinData("05", optionPrice05MinDataDir);
            log.info("End to import Stock Option Five Minutes Data");
            log.info("Begin to import Stock Option Fifteen Minutes Data");
            zipImportOptionMinData("15", optionPrice15MinDataDir);
            log.info("End to import Stock Option Fifteen Minutes Data");
            log.info("Begin to import Stock Option Thirty Minutes Data");
            zipImportOptionMinData("30", optionPrice30MinDataDir);
            log.info("End to import Stock Option One Minute Data");
            log.info("Begin to import Stock Option Sixty Minutes Data");
            zipImportOptionMinData("60", optionPrice60MinDataDir);
            log.info("End to import Stock Option Sixty Minutes Data");
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
                                                String tableName = new StringBuilder().append("option_price_").append(recordDate).toString();
                                                String checkTable = optionPriceService.checkTableExist("public", tableName);
                                                log.info("Check the tableName[{} - {}] from tableSchema[public]", tableName, checkTable);
                                                try {
                                                    if (checkTable == null) {
                                                        String tablePK = new StringBuilder().append("option_price_").append(recordDate).append("_pk").toString();
                                                        log.info("Start to create Table[{}], Primary Key={}", tableName, tablePK);
                                                        optionPriceService.createOptionPriceTable(tableName, tablePK);
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
                                                        List<OptionPrice> priceList = new ArrayList<>(optionPriceDataBatchSize);
                                                        while ((line = br.readLine()) != null) {
                                                            //                                            log.info(line);
                                                            try {
                                                                String[] d = line.split("\t");
                                                                OptionPrice optionPrice = new OptionPrice();
                                                                optionPrice.setTdate(toInt(d[0]));
                                                                optionPrice.setUptime(toInt(d[1]));
                                                                optionPrice.setOrigintime(toInt(d[2]));
                                                                optionPrice.setOrigintimems(toInt(d[3]));
                                                                optionPrice.setOptid(d[4]);
                                                                optionPrice.setPresettle(toDouble(d[5]));
                                                                optionPrice.setSettlepx(toDouble(d[6]));
                                                                optionPrice.setOpenpx(toDouble(d[7]));
                                                                optionPrice.setHighpx(toDouble(d[8]));
                                                                optionPrice.setLowpx(toDouble(d[9]));
                                                                optionPrice.setLastpx(toDouble(d[10]));
                                                                optionPrice.setClosepx(toDouble(d[11]));
                                                                optionPrice.setAuctionpx(toDouble(d[12]));
                                                                optionPrice.setAuctionqty(Long.parseLong(d[13]));
                                                                optionPrice.setOpenints(Long.parseLong(d[14]));
                                                                optionPrice.setVolume(Long.parseLong(d[15]));
                                                                optionPrice.setValue(toDouble(d[16]));
                                                                optionPrice.setB1(toDouble(d[17]));
                                                                optionPrice.setBv1(toInt(d[18]));
                                                                optionPrice.setB2(toDouble(d[19]));
                                                                optionPrice.setBv2(toInt(d[20]));
                                                                optionPrice.setB3(toDouble(d[21]));
                                                                optionPrice.setBv3(toInt(d[22]));
                                                                optionPrice.setB4(toDouble(d[23]));
                                                                optionPrice.setBv4(toInt(d[24]));
                                                                optionPrice.setB5(toDouble(d[25]));
                                                                optionPrice.setBv5(toInt(d[26]));

                                                                optionPrice.setS1(toDouble(d[27]));
                                                                optionPrice.setSv1(toInt(d[28]));
                                                                optionPrice.setS2(toDouble(d[29]));
                                                                optionPrice.setSv2(toInt(d[30]));
                                                                optionPrice.setS3(toDouble(d[31]));
                                                                optionPrice.setSv3(toInt(d[32]));
                                                                optionPrice.setS4(toDouble(d[33]));
                                                                optionPrice.setSv4(toInt(d[34]));
                                                                optionPrice.setS5(toDouble(d[35]));
                                                                optionPrice.setSv5(toInt(d[36]));

                                                                optionPrice.setLocaltime(d[37]);
                                                                optionPrice.setMarket(d[38]);
                                                                optionPrice.setUnix(Long.parseLong(d[39]));
                                                                optionPrice.setSource(d[40]);
                                                                optionPrice.setDatastatus(toInt(d[41]));
                                                                optionPrice.setTradingphase(d[42]);
                                                                optionPrice.setImagestatus(toInt(d[43]));
                                                                optionPrice.setSeqno(toInt(d[44]));
                                                                optionPrice.setPreclosepx(toDouble(d[45]));
                                                                optionPrice.setNumtrade(Long.parseLong(d[46]));

                                                                //optionPriceService.insertOptionPrice(tableName, optionPrice);
                                                                priceList.add(optionPrice);
                                                                ++dataCount;
                                                                if (dataCount % optionPriceDataBatchSize == 0) {
                                                                    optionPriceService.insertOptionPriceBatch(tableName, priceList);
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

                                                            }

                                                            //                                            System.out.println(Arrays.asList(d));
                                                        }
                                                        if (priceList.size() > 0) {
                                                            optionPriceService.insertOptionPriceBatch(tableName, priceList);
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
                                                String checkTable = optionPriceService.checkTableExist("public", tableName);
                                                log.info("Check the tableName[{} - {}] from tableSchema[public]", tableName, checkTable);
                                                try {
                                                    if (checkTable == null) {
                                                        String tablePK = new StringBuilder().append("option_price").append(minType).append("min_").append(recordDate).append("_pk").toString();
                                                        log.info("Start to create Minute Table[{}], Primary Key={}", tableName, tablePK);
                                                        optionPriceService.createOptionPriceMinTable(tableName, tablePK);
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
                                                        List<OptionPriceMin> priceList = new ArrayList<>(optionPriceMinDataBatchSize);
                                                        while ((line = br.readLine()) != null) {
                                                            //                                            log.info(line);
                                                            try {
                                                                String[] d = line.split("\t");
                                                                OptionPriceMin optionPrice = new OptionPriceMin();
                                                                optionPrice.setDatadate(toInt(d[0]));
                                                                optionPrice.setOptid(d[1]);
                                                                optionPrice.setExchangecd(d[2]);
                                                                optionPrice.setUtcoffect(toInt(d[3]));
                                                                optionPrice.setBartime(d[4]);
                                                                optionPrice.setCloseprice(toDouble(d[5]));
                                                                optionPrice.setOpenprice(toDouble(d[6]));
                                                                optionPrice.setHighprice(toDouble(d[7]));
                                                                optionPrice.setLowprice(toDouble(d[8]));
                                                                optionPrice.setVolume(toLong(d[9]));
                                                                optionPrice.setValue(toDouble(d[10]));
                                                                optionPrice.setVwap(toDouble(d[11]));
                                                                optionPrice.setOpenints(toLong(d[12]));

                                                                //optionPriceService.insertOptionPrice(tableName, optionPrice);
                                                                priceList.add(optionPrice);
                                                                ++dataCount;
                                                                if (dataCount % optionPriceDataBatchSize == 0) {
                                                                    optionPriceService.insertOptionPriceMinBatch(tableName, priceList);
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

                                                            }

                                                            //                                            System.out.println(Arrays.asList(d));
                                                        }
                                                        if (priceList.size() > 0) {
                                                            optionPriceService.insertOptionPriceMinBatch(tableName, priceList);
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
