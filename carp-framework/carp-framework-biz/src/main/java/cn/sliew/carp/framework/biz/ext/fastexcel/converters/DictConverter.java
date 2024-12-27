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
package cn.sliew.carp.framework.biz.ext.fastexcel.converters;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.exception.ExcelDataConvertException;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.ReadCellData;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import cn.sliew.carp.framework.common.dict.*;

import java.util.Objects;
import java.util.Optional;

public class DictConverter implements Converter<DictInstance> {

    private DictRegistry dictRegistry = EnumDictRegistry.INSTANCE;

    @Override
    public Class<?> supportJavaTypeKey() {
        return DictInstance.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public DictInstance convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        String value = cellData.getStringValue();
        try {
            Dict dictEnum = contentProperty.getField().getAnnotation(Dict.class);
            if (Objects.nonNull(dictEnum) && Objects.nonNull(value)) {
                String code = dictEnum.code();
                Optional<DictDefinition> optional = dictRegistry.getDictDefinition(code);
                if (optional.isEmpty()) {
                    throw new ExcelDataConvertException(cellData.getRowIndex(), cellData.getColumnIndex(), cellData, contentProperty, "unknown dict code");
                }
                Optional<DictInstance> dictInstance = dictRegistry.getDictInstance(optional.get(), value);
                if (dictInstance.isEmpty()) {
                    throw new ExcelDataConvertException(cellData.getRowIndex(), cellData.getColumnIndex(), cellData, contentProperty, "unknown dict instance code");
                }
                return dictInstance.get();
            }
            throw new ExcelDataConvertException(cellData.getRowIndex(), cellData.getColumnIndex(), cellData, contentProperty, "field must contains Dict annotation");
        } catch (Exception e) {
            throw new ExcelDataConvertException(cellData.getRowIndex(), cellData.getColumnIndex(), cellData, contentProperty, e.getMessage(), e);
        }
    }

    @Override
    public WriteCellData<?> convertToExcelData(DictInstance value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) throws Exception {
        return new WriteCellData(value.getValue());
    }
}
