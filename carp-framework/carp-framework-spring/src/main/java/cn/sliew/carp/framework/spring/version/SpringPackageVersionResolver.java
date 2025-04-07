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
package cn.sliew.carp.framework.spring.version;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Resolves the version by finding the Spring bean annotated with {@link SpringBootApplication}
 * and then determining the Implementation-Version attribute from the package.
 *
 * <p>See {@link Package#getImplementationVersion()}
 */
public class SpringPackageVersionResolver implements VersionResolver {

    @Nullable
    @Override
    public String resolve(@Nonnull String serviceName) {
        Map<String, Object> annotatedBeans =
                SpringUtil.getApplicationContext().getBeansWithAnnotation(SpringBootApplication.class);
        return annotatedBeans.isEmpty()
                ? null
                : annotatedBeans.values().toArray()[0].getClass().getPackage().getImplementationVersion();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
