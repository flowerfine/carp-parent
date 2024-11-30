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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.sliew.carp.framework.common.constant.Constants;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页&排序参数")
public class PageParam implements Serializable {
    private static final long serialVersionUID = -860020632404225667L;

    @Schema(description = "当前页数", example = "1")
    private Long current = 1L;

    @Schema(description = "每页条数", example = "10")
    private Long pageSize = 10L;

    @Schema(description = "排序", example = "[{order: \"ASC\"" + "field: \"fieldName\"}]")
    private List<SortArg> sorter;

    public List<OrderItem> buildSortItems() {
        if (CollectionUtil.isEmpty(sorter)) {
            return Collections.emptyList();
        }

        return sorter.stream()
                .filter(arg -> StringUtils.isNotBlank(arg.getField()))
                .map(arg -> {
                    if (StringUtils.equalsIgnoreCase(arg.getOrder(), Constants.SORT_ASC)
                            || StringUtils.equalsIgnoreCase(arg.getOrder(), Constants.SORT_ASC_ANTD)) {
                        return OrderItem.asc(StrUtil.toUnderlineCase(arg.getField()));
                    }
                    if (StringUtils.equalsIgnoreCase(arg.getOrder(), Constants.SORT_DESC)
                            || StringUtils.equalsIgnoreCase(arg.getOrder(), Constants.SORT_DESC_ANTD)) {
                        return OrderItem.desc(StrUtil.toUnderlineCase(arg.getField()));
                    }
                    // 默认升序
                    return OrderItem.asc(StrUtil.toUnderlineCase(arg.getField()));
                }).collect(Collectors.toList());
    }

    public Long getCurrent() {
        if (current == null || current < 1L) {
            return 1L;
        }
        if (current > 500L) {
            return 500L;
        }
        return current;
    }

    public Long getPageSize() {
        if (pageSize == null || pageSize < 1L) {
            return 10L;
        }
        if (pageSize > 500L) {
            return 500L;
        }
        return pageSize;
    }

}
