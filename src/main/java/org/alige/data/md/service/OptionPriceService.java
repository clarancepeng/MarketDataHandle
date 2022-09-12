package org.alige.data.md.service;

import org.alige.data.md.model.OptionPrice;

import java.util.List;

public interface OptionPriceService {
    String checkTableExist(String dataBaseName, String tableName);

    void createOptionPriceTable(String tableName, String tablePK);

    void insertOptionPrice(String tableName, OptionPrice price);

    void insertOptionPriceBatch(String tableName, List<OptionPrice> priceList);
}
