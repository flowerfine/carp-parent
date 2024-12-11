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
package cn.sliew.carp.framework.spring.util;

import cn.sliew.carp.framework.common.model.PageParam;
import cn.sliew.carp.framework.common.model.PageResult;
import org.springframework.beans.support.PagedListHolder;

import java.util.List;

public enum PageUtil {
    ;

    public static PageResult buildPage(PageParam param, List list) {
        PagedListHolder holder = new PagedListHolder(list);
        holder.setPage(param.getCurrent().intValue());
        holder.setPageSize(param.getPageSize().intValue());
        PageResult pageResult = new PageResult(Long.valueOf(holder.getPage()), Long.valueOf(holder.getPageSize()), Long.valueOf(holder.getNrOfElements()));
        pageResult.setRecords(holder.getPageList());
        return pageResult;
    }

}
