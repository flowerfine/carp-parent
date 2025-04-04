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

import lombok.extern.slf4j.Slf4j;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.RuntimeMode;

import java.nio.file.Path;

/**
 * Decorates {@link PluginDescriptor}s as {@link UnsafePluginDescriptor}s.
 *
 * @see UnsafeManifestPluginDescriptorFinder
 * @see UnsafePropertiesPluginDescriptorFinder
 */
@Slf4j
public class UnsafePluginDescriptorFinder implements PluginDescriptorFinder {

    private final RuntimeMode runtimeMode;
    private final CompoundPluginDescriptorFinder finders;

    public UnsafePluginDescriptorFinder(RuntimeMode runtimeMode) {
        this(runtimeMode, new CompoundPluginDescriptorFinder());
    }

    public UnsafePluginDescriptorFinder(RuntimeMode runtimeMode, CompoundPluginDescriptorFinder finders) {
        this.runtimeMode = runtimeMode;
        this.finders = finders;

        if (finders.size() > 0) {
            // It's not currently obvious if someone would need to provide their own finders, but
            // I want to support this anyway. Doing so could potentially break plugins in Spinnaker
            // if it's not done correctly, however.
            log.warn("Custom finders have been provided, skipping defaults");
        } else {
            if (runtimeMode == RuntimeMode.DEVELOPMENT) {
                CompoundPluginDescriptorFinder spinnakerFinders = addUnsafeFinders(new CompoundPluginDescriptorFinder());
                finders.add(new PluginRefPluginDescriptorFinder(spinnakerFinders));
                finders.add(spinnakerFinders);
            } else {
                addUnsafeFinders(finders);
            }
        }
    }

    private CompoundPluginDescriptorFinder addUnsafeFinders(CompoundPluginDescriptorFinder finder) {
        finder.add(new UnsafePropertiesPluginDescriptorFinder());
        finder.add(new UnsafeManifestPluginDescriptorFinder());
        return finder;
    }

    @Override
    public boolean isApplicable(Path pluginPath) {
        return finders.isApplicable(pluginPath);
    }

    @Override
    public PluginDescriptor find(Path pluginPath) {
        return finders.find(pluginPath);
    }
}
