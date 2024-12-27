/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.sliew.carp.framework.biz.ext.fastexcel.converters.number.money;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import cn.sliew.milky.common.unit.MoneyUnit;
import cn.sliew.milky.common.unit.MoneyValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Objects;

@Slf4j
public class MoneyFen2YuanConverter implements Converter<Long> {

    @Override
    public Class supportJavaTypeKey() {
        return Long.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER;
    }

    @Override
    public Long convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        String stringValue = cellData.getStringValue();
        if (cellData.getType().equals(CellDataTypeEnum.NUMBER)) {
            stringValue = cellData.getNumberValue() + "";
        }
        if (Objects.isNull(stringValue) || StringUtils.isEmpty(stringValue)) {
            return null;
        }
        MoneyValue moneyValue = new MoneyValue(new BigDecimal(stringValue), MoneyUnit.YUAN);
        return moneyValue.getHao().longValue();
    }

    @Override
    public WriteCellData<?> convertToExcelData(Long value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        if (Objects.isNull(value)) {
            return new WriteCellData("");
        }
        MoneyValue moneyValue = new MoneyValue(new BigDecimal(value), MoneyUnit.FEN);
        return new WriteCellData(moneyValue.getFen());
    }
}
