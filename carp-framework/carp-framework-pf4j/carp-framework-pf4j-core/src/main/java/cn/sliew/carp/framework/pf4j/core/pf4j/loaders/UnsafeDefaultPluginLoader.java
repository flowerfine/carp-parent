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
package cn.sliew.carp.framework.pf4j.core.pf4j.loaders;

import cn.sliew.carp.framework.pf4j.core.pf4j.finders.UnsafePluginDescriptor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.DefaultPluginLoader;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;

import java.nio.file.Path;

/**
 * Allows altering the a plugin's {@link ClassLoader} based on the plugin's `unsafe` flag.
 */
@Slf4j
public class UnsafeDefaultPluginLoader extends DefaultPluginLoader {

    public UnsafeDefaultPluginLoader(PluginManager pluginManager) {
        super(pluginManager);
    }

    @Override
    protected PluginClassLoader createPluginClassLoader(Path pluginPath, PluginDescriptor pluginDescriptor) {
        if (!(pluginDescriptor instanceof UnsafePluginDescriptor)) {
            log.debug(
                    "Descriptor for {} is not UnsafePluginDescriptor: Falling back to default behavior",
                    pluginDescriptor.getPluginId()
            );
            return super.createPluginClassLoader(pluginPath, pluginDescriptor);
        }

        UnsafePluginDescriptor unsafePluginDescriptor = (UnsafePluginDescriptor) pluginDescriptor;
        if (unsafePluginDescriptor.getUnsafe()) {
            return new UnsafePluginClassLoader(
                    pluginManager,
                    unsafePluginDescriptor,
                    getClass().getClassLoader()
            );
        } else {
            return super.createPluginClassLoader(pluginPath, pluginDescriptor);
        }
    }
}
