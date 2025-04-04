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
package cn.sliew.carp.framework.pf4j.core.pf4j.finders;

import lombok.Getter;
import lombok.Setter;
import org.pf4j.DefaultPluginDescriptor;
import org.pf4j.PluginDependency;
import org.pf4j.PluginDescriptor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Decorates the default {@link PluginDescriptor} with additional Carp-specific metadata.
 */
@Getter
@Setter
public class UnsafePluginDescriptor extends DefaultPluginDescriptor {

    private String pluginId;
    private String pluginDescription;
    private String pluginClass;
    private String version;
    private String requires;
    private String provider;
    private String license;

    /**
     * If set to true, a plugin will be created using the parent application ClassLoader.
     */
    private Boolean unsafe = false;

    // Jackson compatible private setter
    private void setDependencies(List<PluginDependency> dependencies) {
        this.setDependencies(dependencies.stream()
                .map(PluginDependency::toString)
                .collect(Collectors.joining(",")));
    }

    // TODO(cf): rework once upstream equals/hashCode change is released:
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UnsafePluginDescriptor that = (UnsafePluginDescriptor) o;
        return Objects.equals(unsafe, that.unsafe) &&
                Objects.equals(pluginId, that.pluginId) &&
                Objects.equals(pluginDescription, that.pluginDescription) &&
                Objects.equals(pluginClass, that.pluginClass) &&
                Objects.equals(version, that.version) &&
                Objects.equals(requires, that.requires) &&
                Objects.equals(provider, that.provider) &&
                Objects.equals(license, that.license);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unsafe, pluginId, pluginDescription, pluginClass, version, requires, provider, license);
    }
}
