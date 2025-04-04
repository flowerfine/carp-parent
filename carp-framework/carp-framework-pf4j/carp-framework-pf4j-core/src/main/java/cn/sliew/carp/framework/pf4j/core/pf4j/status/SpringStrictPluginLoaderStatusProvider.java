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
package cn.sliew.carp.framework.pf4j.core.pf4j.status;

import org.springframework.core.env.Environment;

/**
 * Backs strict plugin loading by the Spring environment, instead of using text files.
 */
public class SpringStrictPluginLoaderStatusProvider {

    private final Environment environment;

    public SpringStrictPluginLoaderStatusProvider(Environment environment) {
        this.environment = environment;
    }

    /**
     * Returns whether or not the service is configured to be strict about plugin loading or not.
     * <p>
     * When in strict mode, the service will fail to start if any plugin cannot be loaded.
     * <p>
     * Defaults to true.
     */
    public boolean isStrictPluginLoading() {
        String value = environment.getProperty("spinnaker.extensibility.strict-plugin-loading");
        return value == null || Boolean.parseBoolean(value);
    }
}
