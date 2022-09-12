package org.alige.data.md.dao;

import org.alige.data.md.model.IndexOptionPrice;
import org.alige.data.md.model.IndexOptionPriceMin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface IndexOptionPriceDao {

    String checkTableExist(@Param("dataBaseName") String dataBaseName, @Param("tableName") String tableName);

    void createOptionPriceTable(@Param("tableName") String tableName);

//    void insertOptionPrice(@Param("tableName") String tableName, IndexOptionPrice price);

    void insertOptionPriceBatch(@Param("tableName") String tableName, List<IndexOptionPrice> priceList);

    void createOptionPriceMinTable(@Param("tableName") String tableName);

    void insertOptionPriceMinBatch(@Param("tableName") String tableName, List<IndexOptionPriceMin> priceList);
}
