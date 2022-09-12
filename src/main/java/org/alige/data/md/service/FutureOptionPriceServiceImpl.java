package org.alige.data.md.service;

import org.alige.data.md.dao.FutureOptionPriceDao;
import org.alige.data.md.model.FutureOptionPrice;
import org.alige.data.md.model.FutureOptionPriceMin;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FutureOptionPriceServiceImpl implements FutureOptionPriceService {
    @Resource
    private FutureOptionPriceDao futureOptionPriceDao;

    @Override
    public String checkTableExist(String dataBaseName, String tableName) {
        return futureOptionPriceDao.checkTableExist(dataBaseName, tableName);
    }

    @Override
    public void createOptionPriceTable(String tableName, String tablePK) {
        futureOptionPriceDao.createOptionPriceTable(tableName);
    }


    @Override
    public void insertOptionPriceBatch(String tableName, List<FutureOptionPrice> priceList) {
        futureOptionPriceDao.insertOptionPriceBatch(tableName, priceList);
    }

    @Override
    public void createOptionPriceMinTable(String tableName, String tablePK) {
        futureOptionPriceDao.createOptionPriceMinTable(tableName);
    }

    @Override
    public void insertOptionPriceMinBatch(String tableName, List<FutureOptionPriceMin> priceList) {
        futureOptionPriceDao.insertOptionPriceMinBatch(tableName, priceList);
    }
}
