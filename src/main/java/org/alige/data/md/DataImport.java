package org.alige.data.md;

import lombok.extern.slf4j.Slf4j;
import org.alige.data.md.model.OptionPrice;
import org.alige.data.md.model.OptionPriceMin;
import org.alige.data.md.service.OptionPriceService;
import org.alige.data.md.service.StockOptionImportService;
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
    private StockOptionImportService stockOptionImportService;

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            stockOptionImportService.importAllStockOption();
        };
    }


    public static void main(String[] args) {
//        DataImport dataImport = new DataImport();
//        dataImport.zipImport();
        SpringApplication.run(DataImport.class, args);
    }
}
