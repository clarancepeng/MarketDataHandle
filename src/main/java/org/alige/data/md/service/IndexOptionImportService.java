package org.alige.data.md.service;

import lombok.extern.slf4j.Slf4j;
import org.alige.data.md.model.IndexOptionPrice;
import org.alige.data.md.model.IndexOptionPriceMin;
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
public class IndexOptionImportService {
    @Autowired
    private IndexOptionPriceService indexOptionPriceService;

    @Value("${idx_opt_price.data.dir}")
    private String optionPriceDataDir;

    @Value("${idx_opt_pricemin.data.dir}")
    private String optionPriceMinDataDir;

    @Value("${idx_opt_price03min.data.dir}")
    private String optionPrice03MinDataDir;

    @Value("${idx_opt_price05min.data.dir}")
    private String optionPrice05MinDataDir;

    @Value("${idx_opt_price15min.data.dir}")
    private String optionPrice15MinDataDir;

    @Value("${idx_opt_price30min.data.dir}")
    private String optionPrice30MinDataDir;

    @Value("${idx_opt_price60min.data.dir}")
    private String optionPrice60MinDataDir;

    @Value("${idx_opt_price.data.batch_size: 300}")
    private int optionPriceDataBatchSize = 300;

    @Value("${idx_opt_pricemin.data.batch_size: 300}")
    private int optionPriceMinDataBatchSize = 300;

    public void importAllIndexOption() {
        try {
            log.info("Begin to import Index Option Tick Data");
            zipImportOptionTickData();
            log.info("End to import Index Option Tick Data");
            log.info("Begin to import Index Option One Minute Data");
            zipImportOptionMinData("", optionPriceMinDataDir);
            log.info("End to import Index Option One Minute Data");
            log.info("Begin to import Index Option Three Minutes Data");
            zipImportOptionMinData("03", optionPrice03MinDataDir);
            log.info("End to import Index Option Three Minutes Data");
            log.info("Begin to import Index Option Five Minutes Data");
            zipImportOptionMinData("05", optionPrice05MinDataDir);
            log.info("End to import Index Option Five Minutes Data");
            log.info("Begin to import Index Option Fifteen Minutes Data");
            zipImportOptionMinData("15", optionPrice15MinDataDir);
            log.info("End to import Index Option Fifteen Minutes Data");
            log.info("Begin to import Index Option Thirty Minutes Data");
            zipImportOptionMinData("30", optionPrice30MinDataDir);
            log.info("End to import Index Option One Minute Data");
            log.info("Begin to import Index Option Sixty Minutes Data");
            zipImportOptionMinData("60", optionPrice60MinDataDir);
            log.info("End to import Index Option Sixty Minutes Data");
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
                                                String tableName = new StringBuilder().append("idx_opt_price_").append(recordDate).toString();
                                                String checkTable = indexOptionPriceService.checkTableExist("public", tableName);
                                                log.info("Check the tableName[{} - {}] from tableSchema[public]", tableName, checkTable);
                                                try {
                                                    if (checkTable == null) {
                                                        String tablePK = new StringBuilder().append("idx_opt_price_").append(recordDate).append("_pk").toString();
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
                                                        List<IndexOptionPrice> priceList = new ArrayList<>(optionPriceDataBatchSize);
                                                        while ((line = br.readLine()) != null) {
                                                            //                                            log.info(line);
                                                            try {
                                                                String[] d = line.split("\t");
                                                                IndexOptionPrice optionPrice = new IndexOptionPrice();
                                                                optionPrice.setInstrumentId(d[0]);
                                                                optionPrice.setMarket(d[1]);
                                                                optionPrice.setTdate(toInt(d[2]));
                                                                optionPrice.setTtime(d[3]);
                                                                optionPrice.setLastPx(toDouble(d[4]));
                                                                optionPrice.setOpenPx(toDouble(d[5]));
                                                                optionPrice.setHighPx(toDouble(d[6]));
                                                                optionPrice.setLowPx(toDouble(d[7]));
                                                                optionPrice.setVolume(Long.parseLong(d[8]));
                                                                optionPrice.setValue(toDouble(d[9]));
                                                                optionPrice.setOpenInt(Long.parseLong(d[10]));
                                                                optionPrice.setPreSettlePx(toDouble(d[11]));
                                                                optionPrice.setPreClosePx(toDouble(d[12]));
                                                                optionPrice.setPreOpenInt(toDouble(d[13]));
                                                                optionPrice.setClosePx(toDouble(d[14]));
                                                                optionPrice.setSettlePx(toDouble(d[15]));
                                                                optionPrice.setUpperLimit(toDouble(d[16]));
                                                                optionPrice.setLowerLimit(toDouble(d[17]));
                                                                optionPrice.setPreDelta(toDouble(d[18]));
                                                                optionPrice.setCurrDelta(toDouble(d[19]));

                                                                optionPrice.setBidPrice1(toDouble(d[20]));
                                                                optionPrice.setBidVolume1(toInt(d[21]));
                                                                optionPrice.setAskPrice1(toDouble(d[22]));
                                                                optionPrice.setAskVolume1(toInt(d[23]));
                                                                optionPrice.setBidPrice2(toDouble(d[24]));
                                                                optionPrice.setBidVolume2(toInt(d[25]));
                                                                optionPrice.setAskPrice2(toDouble(d[26]));
                                                                optionPrice.setAskVolume2(toInt(d[27]));
                                                                optionPrice.setBidPrice3(toDouble(d[28]));
                                                                optionPrice.setBidVolume3(toInt(d[29]));
                                                                optionPrice.setAskPrice3(toDouble(d[30]));
                                                                optionPrice.setAskVolume3(toInt(d[31]));
                                                                optionPrice.setBidPrice4(toDouble(d[32]));
                                                                optionPrice.setBidVolume4(toInt(d[33]));
                                                                optionPrice.setAskPrice4(toDouble(d[34]));
                                                                optionPrice.setAskVolume4(toInt(d[35]));
                                                                optionPrice.setBidPrice5(toDouble(d[36]));
                                                                optionPrice.setBidVolume5(toInt(d[37]));
                                                                optionPrice.setAskPrice5(toDouble(d[38]));
                                                                optionPrice.setAskVolume5(toInt(d[39]));

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
                                                        List<IndexOptionPriceMin> priceList = new ArrayList<>(optionPriceMinDataBatchSize);
                                                        while ((line = br.readLine()) != null) {
                                                            //                                            log.info(line);
                                                            try {
                                                                String[] d = line.split("\t");
                                                                IndexOptionPriceMin optionPrice = new IndexOptionPriceMin();
                                                                optionPrice.setTdate(toInt(d[0]));
                                                                optionPrice.setInstrumentId(d[1]);
                                                                optionPrice.setMarket(d[2]);
//                                                                optionPrice.setUtcoffect(toInt(d[3]));
                                                                optionPrice.setBarTime(d[3]);
                                                                optionPrice.setClosePrice(toDouble(d[5]));
                                                                optionPrice.setOpenPrice(toDouble(d[6]));
                                                                optionPrice.setHighPrice(toDouble(d[7]));
                                                                optionPrice.setLowPrice(toDouble(d[8]));
                                                                optionPrice.setTotalVolume(toLong(d[9]));
                                                                optionPrice.setTotalValue(toDouble(d[10]));
                                                                optionPrice.setVwap(toDouble(d[11]));
                                                                optionPrice.setOpenInterest(toLong(d[12]));
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
