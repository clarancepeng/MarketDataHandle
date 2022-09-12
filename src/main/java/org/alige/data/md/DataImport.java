package org.alige.data.md;

import lombok.extern.slf4j.Slf4j;
import org.alige.data.md.model.OptionPrice;
import org.alige.data.md.service.OptionPriceService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

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

@Slf4j
@MapperScan("org.alige.data.md.dao")
@SpringBootApplication(scanBasePackages = {"org.alige.data.md", "org.alige.data.md.dao", "org.alige.data.md.service"})
@ComponentScan({"org.alige.data.md", "org.alige.data.md.dao", "org.alige.data.md.service"})
public class DataImport {
    @Autowired
    private OptionPriceService optionPriceService;

    @Value("${option_price.data.dir}")
    private String optionPriceDataDir;

    @Value("${option_price.data.batch_size: 300}")
    private int optionPriceDataBatchSize = 300;

    private int guessDate(String fileName) {
        int place = fileName.indexOf('.');
        if (place > 8) {
            String dateStr = fileName.substring(place - 8, place);
            return Integer.parseInt(dateStr);
        }
        return 0;
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            zipImport();
        };
    }

    public int toInt(String data) {
        try {
            return Integer.parseInt(data);
        } catch (Exception e) {
            return 0;
        }
    }

    public double toDouble(String data) {
        try {
            return Double.parseDouble(data);
        } catch (Exception e) {
            return 0;
        }
    }

    public void zipImport() {
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
                                                        //                                        ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
                                                        //                                        strategy.setType(OptionPrice.class);
                                                        //
                                                        //                                        CsvToBean csvToBean = new CsvToBeanBuilder(br)
                                                        //                                                .withType(OptionPrice.class)
                                                        //                                                .withSeparator('\t')
                                                        ////                                                .withMappingStrategy(strategy)
                                                        //                                                .withIgnoreLeadingWhiteSpace(true)
                                                        //                                                .build();
                                                        //
                                                        //                                        List<OptionPrice> optionPriceList = csvToBean.parse();
                                                        //                                        for(OptionPrice op : optionPriceList) {
                                                        //                                            log.info("{}", op);
                                                        //                                        }
                                                        //                                        System.out.println();
                                                        //                                        br.close();
                                                        //                                    zipInput = new ZipInputStream(new FileInputStream(fileName));
                                                        //                                    final RandomAccessFile rf = new RandomAccessFile(fileName, "r");
                                                        //                                    String line;
                                                        //                                    while ((line = rf.readLine()) != null) {
                                                        //                                        System.out.println(line);
                                                        //                                    }
                                                        //                                    rf.close();
                                                        //                                    zipInput.closeEntry();
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


    public static void main(String[] args) {
//        DataImport dataImport = new DataImport();
//        dataImport.zipImport();
        SpringApplication.run(DataImport.class, args);
    }
}
