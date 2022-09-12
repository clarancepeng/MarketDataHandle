package org.alige.data.md.service;

import org.alige.data.md.model.IndexOptionPrice;
import org.alige.data.md.model.IndexOptionPriceMin;

import java.util.List;

public interface IndexOptionPriceService {
    String checkTableExist(String dataBaseName, String tableName);

    void createOptionPriceTable(String tableName, String tablePK);

    void insertOptionPriceBatch(String tableName, List<IndexOptionPrice> priceList);

    void createOptionPriceMinTable(String tableName, String tablePK);

    void insertOptionPriceMinBatch(String tableName, List<IndexOptionPriceMin> priceList);
}
