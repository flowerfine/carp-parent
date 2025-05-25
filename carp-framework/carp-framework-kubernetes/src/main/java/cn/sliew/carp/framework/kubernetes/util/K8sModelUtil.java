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
package cn.sliew.carp.framework.kubernetes.util;

import cn.sliew.carp.framework.kubernetes.model.K8sResourceList;
import cn.sliew.carp.framework.kubernetes.model.K8sResourceModel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.function.Function;

public enum K8sModelUtil {
    ;

    public <S, T extends K8sResourceModel> K8sResourceList<T> wrap(@Nonnull IPage<S> page, @Nonnull Function<List<S>, List<T>> mapper, @Nonnull Class<T> modelClass) {
        K8sResourceList.K8sResourceListMetadata metadata = K8sResourceList.K8sResourceListMetadata.builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .build();
        List<T> items = mapper.apply(page.getRecords());
        String kind = String.format("%sList", modelClass.getSimpleName());
        // fixme unknown is not good
        String apiVersion = "unknown";
        if (CollectionUtils.isNotEmpty(items)) {
            T item = items.get(0);
            kind = String.format("%sList", item.getKind());
            apiVersion = item.getApiVersion();
        }
        return K8sResourceList.<T>builder()
                .kind(kind)
                .apiVersion(apiVersion)
                .metadata(metadata)
                .items(items)
                .build();
    }
}
