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
public class UnsafePluginDescriptor extends DefaultPluginDescriptor {

    /**
     * If set to true, a plugin will be created using the parent application ClassLoader.
     */
    private Boolean unsafe = false;

    public UnsafePluginDescriptor() {
        super();
    }

    public UnsafePluginDescriptor(String pluginId, String pluginDescription, String pluginClass, String version, String requires, String provider, String license) {
        super(pluginId, pluginDescription, pluginClass, version, requires, provider, license);
    }

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
                Objects.equals(getPluginId(), that.getPluginId()) &&
                Objects.equals(getPluginDescription(), that.getPluginDescription()) &&
                Objects.equals(getPluginClass(), that.getPluginClass()) &&
                Objects.equals(getVersion(), that.getVersion()) &&
                Objects.equals(getRequires(), that.getRequires()) &&
                Objects.equals(getProvider(), that.getProvider()) &&
                Objects.equals(getLicense(), that.getLicense());
    }

    @Override
    public int hashCode() {
        return Objects.hash(unsafe, getPluginId(), getPluginDescription(), getPluginClass(), getVersion(), getRequires(), getProvider(), getLicense());
    }
}
