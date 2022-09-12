package org.alige.data.md.service;

import org.alige.data.md.dao.IndexOptionPriceDao;
import org.alige.data.md.model.IndexOptionPrice;
import org.alige.data.md.model.IndexOptionPriceMin;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class IndexOptionPriceServiceImpl implements IndexOptionPriceService {
    @Resource
    private IndexOptionPriceDao indexOptionPriceDao;

    @Override
    public String checkTableExist(String dataBaseName, String tableName) {
        return indexOptionPriceDao.checkTableExist(dataBaseName, tableName);
    }

    @Override
    public void createOptionPriceTable(String tableName, String tablePK) {
        indexOptionPriceDao.createOptionPriceTable(tableName);
    }


    @Override
    public void insertOptionPriceBatch(String tableName, List<IndexOptionPrice> priceList) {
        indexOptionPriceDao.insertOptionPriceBatch(tableName, priceList);
    }

    @Override
    public void createOptionPriceMinTable(String tableName, String tablePK) {
        indexOptionPriceDao.createOptionPriceMinTable(tableName);
    }

    @Override
    public void insertOptionPriceMinBatch(String tableName, List<IndexOptionPriceMin> priceList) {
        indexOptionPriceDao.insertOptionPriceMinBatch(tableName, priceList);
    }
}
