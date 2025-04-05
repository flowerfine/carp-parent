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
package cn.sliew.carp.framework.pf4j.core.pf4j.versions;

import com.github.zafarkhaja.semver.Version;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.VersionManager;
import org.pf4j.util.StringUtils;

/**
 * Since plugins may require multiple services, this class is necessary to ensure we are making the
 * constraint check against the correct service.
 * 应用在加载插件时，需判断该插件是否满足当前应用版本要求。
 * 如插件v1支持应用在 1.0.0 ~ 2.0.0，插件v2支持应用 >= 3.0.0。在加载插件时需判断应用版本
 * 是否满足插件版本限制
 */
@Slf4j
public class ApplicationVersionManager implements VersionManager {

    private final String applicationName;

    /**
     * @param applicationName
     */
    public ApplicationVersionManager(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public boolean checkVersionConstraint(String version, String requires) {
        if (StringUtils.isNullOrEmpty(requires)) {
            log.warn("Loading plugin with empty Plugin-Requires attribute!");
            return true;
        }

        VersionRequirementsParser.VersionRequirements requirements =
                VersionRequirementsParser.parseAll(requires)
                        .stream()
                        .filter(req -> req.getService().equalsIgnoreCase(applicationName))
                        .findFirst()
                        .orElse(null);

        if (requirements != null) {
            return StringUtils.isNullOrEmpty(requirements.getConstraint()) ||
                    Version.valueOf(version).satisfies(requirements.getConstraint());
        }

        return false;
    }

    @Override
    public int compareVersions(String v1, String v2) {
        return Version.valueOf(v1).compareTo(Version.valueOf(v2));
    }
}
