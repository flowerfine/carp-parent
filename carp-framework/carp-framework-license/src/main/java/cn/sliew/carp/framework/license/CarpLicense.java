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
package cn.sliew.carp.framework.license;

import cn.sliew.carp.framework.common.dict.license.LicenseType;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class CarpLicense {

    private LicenseType type;
    private LocalDateTime expireTime;

    public boolean isValid() {
        if (Objects.nonNull(type) && Objects.nonNull(expireTime)) {
            return Duration.between(expireTime, LocalDateTime.now()).toMillis() < 0L;
        }
        return false;
    }

    public boolean isPro() {
        return isValid() && (type == LicenseType.PRO || type == LicenseType.ENTERPRISE);
    }

    public boolean isEnterprise() {
        return isValid() && type == LicenseType.ENTERPRISE;
    }

}
