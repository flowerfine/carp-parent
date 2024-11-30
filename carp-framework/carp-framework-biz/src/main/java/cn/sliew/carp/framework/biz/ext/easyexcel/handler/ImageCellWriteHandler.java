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
package cn.sliew.carp.framework.biz.ext.easyexcel.handler;

import cn.hutool.http.HttpUtil;
import cn.sliew.carp.framework.biz.ext.easyexcel.annotation.ExcelImage;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.CellData;
import com.alibaba.excel.metadata.data.ImageData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.Units;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 使用时需注册一下，才能生效
 */
public class ImageCellWriteHandler implements CellWriteHandler {

    private List<String> repeats = new ArrayList<>();

    @Override
    public void beforeCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Head head, Integer columnIndex, Integer relativeRowIndex, Boolean isHead) {

    }

    @Override
    public void afterCellCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {

    }

    @Override
    public void afterCellDataConverted(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, WriteCellData<?> cellData, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        //  在数据转换成功后 不是 Head 就把类型设置成空
        if (isHead) {
            return;
        }
        //将要插入图片的单元格的type设置为空,下面再填充图片
        Object data = cellData.getData();
        if (cellData.getImageDataList() != null || data instanceof ArrayList) {
            cellData.setType(CellDataTypeEnum.EMPTY);
            return;
        }
        // 写入图片
        handlerExcelImage(cellData, cell, head);
    }

    private void handlerExcelImage(WriteCellData<?> cellData, Cell cell, Head head) {
        ExcelImage excelImage = head.getField().getAnnotation(ExcelImage.class);
        String stringValue = cellData.getStringValue();
        if (Objects.nonNull(excelImage) && Objects.nonNull(stringValue)) {
            //处理单张图片
            if (!excelImage.isMultiple()) {
                cellData.setType(CellDataTypeEnum.RICH_TEXT_STRING);
                ImageData imageData = downloadImageData(stringValue, excelImage);
                if (Objects.nonNull(imageData)) {
                    cellData.setImageDataList(Collections.singletonList(imageData));
                }
            } else {
                cellData.setType(CellDataTypeEnum.EMPTY);
                // 默认使用 , 分割
                List<String> list = Arrays.asList(stringValue.replace("，", ",").split(","));
                Sheet sheet = cell.getSheet();
                int maxColumnWidth = Math.max(sheet.getColumnWidth(cell.getColumnIndex()), 240 * 8 * list.size());
                sheet.setColumnWidth(cell.getColumnIndex(), maxColumnWidth);
                List<ImageData> imageDataList = list.stream()
                        .map(u -> downloadImageData(u, excelImage))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                for (int i = 0; i < imageDataList.size(); i++) {
                    insertImage(sheet, cell, imageDataList.get(i).getImage(), i);
                }
            }
        }
    }

    private ImageData downloadImageData(String strUrl, ExcelImage excelImage) {
        byte[] bytes = HttpUtil.downloadBytes(strUrl);
        ImageData imageData = new ImageData();
        imageData.setImage(bytes);
        imageData.setAnchorType(excelImage.anchorType());
        return imageData;
    }

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        //  在单元格写入完毕后，自己填充图片
        if (isHead || CollectionUtils.isEmpty(cellDataList)) {
            return;
        }
        WriteCellData<?> writeCellData = cellDataList.get(0);
        if (writeCellData.getData() != null && writeCellData.getData() instanceof ArrayList) {
            String key = cell.getRowIndex() + "_" + cell.getColumnIndex();
            if (repeats.contains(key)) {
                return;
            }
            repeats.add(key);
            List<CellData<byte[]>> cellDatas = (List<CellData<byte[]>>) writeCellData.getData();
            Sheet sheet = cell.getSheet();
            sheet.getRow(cell.getRowIndex()).setHeight((short) 900);
            int maxColumnWidth = Math.max(sheet.getColumnWidth(cell.getColumnIndex()), 240 * 8 * cellDatas.size());
            sheet.setColumnWidth(cell.getColumnIndex(), maxColumnWidth);
            for (int i = 0; i < cellDatas.size(); i++) {
                insertImage(sheet, cell, cellDatas.get(i).getData(), i);
            }
        }
    }

    private void insertImage(Sheet sheet, Cell cell, byte[] pictureData, int i) {
        int picWidth = Units.pixelToEMU(60);
        int index = sheet.getWorkbook().addPicture(pictureData, HSSFWorkbook.PICTURE_TYPE_PNG);
        Drawing drawing = sheet.getDrawingPatriarch();
        if (drawing == null) {
            drawing = sheet.createDrawingPatriarch();
        }
        CreationHelper helper = sheet.getWorkbook().getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();
        // 设置图片坐标
        anchor.setDx1(picWidth * i);
        anchor.setDx2(picWidth + picWidth * i);
        anchor.setDy1(0);
        anchor.setDy2(0);
        //设置图片位置
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex());
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 1);

        // 设置图片可以随着单元格移动
        anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
        drawing.createPicture(anchor, index);
    }
}

