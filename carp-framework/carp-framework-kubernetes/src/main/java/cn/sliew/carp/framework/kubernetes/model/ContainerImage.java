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
package cn.sliew.carp.framework.kubernetes.model;

import cn.sliew.carp.framework.common.dict.k8s.CarpK8sImagePullPolicy;
import lombok.Builder;
import lombok.Data;
import lombok.With;
import lombok.extern.jackson.Jacksonized;
import org.apache.commons.lang3.StringUtils;

@Data
@With
@Builder(toBuilder = true)
@Jacksonized
public class ContainerImage {

    private final CarpK8sImagePullPolicy imagePullPolicy;
    private final String registry;
    private final String repository;
    private final String tag;
    private final String image;

    public String getImage() {
        if (StringUtils.isNotBlank(image)) {
            return image;
        }
        if (StringUtils.isNotBlank(registry)) {
            if (StringUtils.startsWithIgnoreCase(tag, "sha256")) {
                return String.format("%s/%s@%s", registry, repository, tag);
            } else {
                return String.format("%s/%s:%s", registry, repository, tag);
            }
        }
        if (StringUtils.startsWithIgnoreCase(tag, "sha256")) {
            return String.format("%s@%s", repository, tag);
        } else {
            return String.format("%s:%s", repository, tag);
        }
    }
}
