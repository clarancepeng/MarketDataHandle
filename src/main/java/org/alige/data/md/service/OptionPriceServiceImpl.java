package org.alige.data.md.service;

import org.alige.data.md.dao.OptionPriceDao;
import org.alige.data.md.model.OptionPrice;
import org.alige.data.md.model.OptionPriceMin;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class OptionPriceServiceImpl implements OptionPriceService {
    @Resource
    private OptionPriceDao optionPriceDao;

    @Override
    public String checkTableExist(String dataBaseName, String tableName) {
        return optionPriceDao.checkTableExist(dataBaseName, tableName);
    }

    @Override
    public void createOptionPriceTable(String tableName, String tablePK) {
        optionPriceDao.createOptionPriceTable(tableName);
    }

    @Override
    public void insertOptionPrice(String tableName, OptionPrice price) {
        optionPriceDao.insertOptionPrice(tableName, price);
    }

    @Override
    public void insertOptionPriceBatch(String tableName, List<OptionPrice> priceList) {
        optionPriceDao.insertOptionPriceBatch(tableName, priceList);
    }

    @Override
    public void createOptionPriceMinTable(String tableName, String tablePK) {
        optionPriceDao.createOptionPriceMinTable(tableName);
    }

    @Override
    public void insertOptionPriceMinBatch(String tableName, List<OptionPriceMin> priceList) {
        optionPriceDao.insertOptionPriceMinBatch(tableName, priceList);
    }
}
