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

import cn.sliew.carp.framework.pf4j.core.pluginref.PluginRef;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;

import java.nio.file.Path;

/**
 * A {@link PluginDescriptorFinder} that uses a {@link PluginRef} to determine the root directory to
 * then delegate to the provided {@code descriptorFinder}.
 */
public class PluginRefPluginDescriptorFinder implements PluginDescriptorFinder {

    private final PluginDescriptorFinder descriptorFinder;

    public PluginRefPluginDescriptorFinder(PluginDescriptorFinder descriptorFinder) {
        this.descriptorFinder = descriptorFinder;
    }

    @Override
    public boolean isApplicable(Path pluginPath) {
        if (!PluginRef.isPluginRef(pluginPath)) {
            return false;
        }

        PluginRef ref = PluginRef.loadPluginRef(pluginPath);
        return descriptorFinder.isApplicable(ref.getRefPath());
    }

    @Override
    public PluginDescriptor find(Path pluginPath) {
        PluginRef ref = PluginRef.loadPluginRef(pluginPath);
        return descriptorFinder.find(ref.getRefPath());
    }
}
