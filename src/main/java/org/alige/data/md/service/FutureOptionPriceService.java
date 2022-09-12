package org.alige.data.md.service;

import org.alige.data.md.model.FutureOptionPrice;
import org.alige.data.md.model.FutureOptionPriceMin;

import java.util.List;

public interface FutureOptionPriceService {
    String checkTableExist(String dataBaseName, String tableName);

    void createOptionPriceTable(String tableName, String tablePK);

    void insertOptionPriceBatch(String tableName, List<FutureOptionPrice> priceList);

    void createOptionPriceMinTable(String tableName, String tablePK);

    void insertOptionPriceMinBatch(String tableName, List<FutureOptionPriceMin> priceList);
}
