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
package cn.sliew.carp.framework.pf4j.core.sdks;

import cn.sliew.carp.framework.pf4j.api.PluginSdks;
import cn.sliew.carp.framework.pf4j.api.sdks.ServiceSdk;
import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.Optional;

public class PluginSdksImpl implements PluginSdks {

    private List sdkServices;

    public PluginSdksImpl(List sdkServices) {
        this.sdkServices = sdkServices;
    }

    @Nonnull
    @Override
    public <T extends ServiceSdk> T serviceSdk(Class<T> type) {
        Optional<ServiceSdk> serviceSdk = sdkServices.stream()
                .filter(obj -> obj instanceof ServiceSdk)
                .findFirst();

        if (!serviceSdk.isPresent()) {
            throw new RuntimeException("No service SDK is configured for this service");
        }

        ServiceSdk sdk = serviceSdk.get();
        if (!type.isAssignableFrom(sdk.getClass())) {
            throw new RuntimeException("Requested unknown serivce SDK type, but '" +
                    sdk.getClass().getSimpleName() + "' is available");
        }

        return type.cast(sdk);
    }

    private <T> T service(Class<T> serviceClass) throws Throwable {
        return (T) sdkServices.stream()
                .filter(serviceClass::isInstance)
                .map(serviceClass::cast)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(serviceClass + " is not configured"));
    }
}
