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
package cn.sliew.carp.framework.common.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ValidateResult {

    private boolean valid;
    private String rootBeanClass;
    private Object rootBean;
    @Singular
    private List<ValidateResultItem> items;

    /**
     * 创建一个验证成功的 {@link ValidateResult} 实例。
     *
     * @return 包含 {@code valid = true} 结果
     */
    public static ValidateResult success() {
        return builder().valid(true).build();
    }

    /**
     * 创建一个验证失败的 {@link ValidateResult} 构建器。
     *
     * @return 配置了 {@code valid = false} 的结果
     */
    public static ValidateResultBuilder failure() {
        return builder().valid(false);
    }
}
