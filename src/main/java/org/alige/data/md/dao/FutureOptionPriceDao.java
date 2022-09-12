package org.alige.data.md.dao;

import org.alige.data.md.model.FutureOptionPrice;
import org.alige.data.md.model.FutureOptionPriceMin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface FutureOptionPriceDao {

    String checkTableExist(@Param("dataBaseName") String dataBaseName, @Param("tableName") String tableName);

    void createOptionPriceTable(@Param("tableName") String tableName);

    void insertOptionPriceBatch(@Param("tableName") String tableName, List<FutureOptionPrice> priceList);

    void createOptionPriceMinTable(@Param("tableName") String tableName);

    void insertOptionPriceMinBatch(@Param("tableName") String tableName, List<FutureOptionPriceMin> priceList);
}
