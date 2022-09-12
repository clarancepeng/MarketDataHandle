package org.alige.data.md.dao;

import org.alige.data.md.model.OptionPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface OptionPriceDao {

    String checkTableExist(@Param("dataBaseName") String dataBaseName, @Param("tableName") String tableName);

    void createOptionPriceTable(@Param("tableName") String tableName);

    void insertOptionPrice(@Param("tableName") String tableName, OptionPrice price);

    void insertOptionPriceBatch(@Param("tableName") String tableName, List<OptionPrice> priceList);
}
