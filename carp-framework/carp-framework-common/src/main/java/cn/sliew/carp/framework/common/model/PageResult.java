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
package cn.sliew.carp.framework.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页数", example = "1")
    private Long current = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long size = 10L;

    @Schema(description = "数据总数", example = "10")
    private Long total;

    @Schema(description = "数据")
    private List<T> records;

    public PageResult(Long current, Long size, Long total) {
        this.current = current;
        this.size = size;
        this.total = total;
    }

    public static PageResult build(List records, PageParam pageParam) {
        if (CollectionUtils.isEmpty(records)) {
            return new PageResult(pageParam.getCurrent(), pageParam.getPageSize(), 0L);
        }
        PageResult pageResult = new PageResult(pageParam.getCurrent(), pageParam.getPageSize(), Long.valueOf(records.size()));
        Long from = (pageParam.getCurrent() - 1) * pageParam.getPageSize();
        Long last = pageParam.getCurrent() * pageParam.getPageSize() <= records.size() ? pageParam.getCurrent() * pageParam.getPageSize() : records.size();
        pageResult.setRecords(records.subList(from.intValue(), last.intValue()));
        return pageResult;
    }
}
