package org.alige.data.md;

import lombok.extern.slf4j.Slf4j;
import org.alige.data.md.service.FutureOptionImportService;
import org.alige.data.md.service.IndexOptionImportService;
import org.alige.data.md.service.StockOptionImportService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@MapperScan("org.alige.data.md.dao")
@SpringBootApplication(scanBasePackages = {"org.alige.data.md", "org.alige.data.md.dao", "org.alige.data.md.service"})
@ComponentScan({"org.alige.data.md", "org.alige.data.md.dao", "org.alige.data.md.service"})
public class DataImport {

    @Autowired
    private StockOptionImportService stockOptionImportService;

    @Autowired
    private IndexOptionImportService indexOptionImportService;

    @Autowired
    private FutureOptionImportService futureOptionImportService;

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            stockOptionImportService.importAllStockOption();
            indexOptionImportService.importAllIndexOption();
            futureOptionImportService.importAllFutureOption();
        };
    }


    public static void main(String[] args) {
//        DataImport dataImport = new DataImport();
//        dataImport.zipImport();
        SpringApplication.run(DataImport.class, args);
    }
}
