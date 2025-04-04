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

import cn.sliew.carp.framework.pf4j.core.pf4j.Util;
import org.pf4j.DefaultPluginDescriptor;
import org.pf4j.PluginDescriptor;
import org.pf4j.PropertiesPluginDescriptorFinder;

import java.util.Properties;

/**
 * Decorates the default {@link PluginDescriptor} created from {@link PropertiesPluginDescriptorFinder}
 * with Carp-specific descriptor metadata.
 */
public class UnsafePropertiesPluginDescriptorFinder extends PropertiesPluginDescriptorFinder {

    /**
     * The property key for the unsafe configuration.
     */
    public static final String PLUGIN_UNSAFE = "plugin.unsafe";

    public UnsafePropertiesPluginDescriptorFinder() {
        super();
    }

    public UnsafePropertiesPluginDescriptorFinder(String propertiesFileName) {
        super(propertiesFileName);
    }

    @Override
    protected PluginDescriptor createPluginDescriptor(Properties properties) {
        PluginDescriptor descriptor = super.createPluginDescriptor(properties);
        if (descriptor instanceof UnsafePluginDescriptor unsafePluginDescriptor) {
            String unsafeValue = properties.getProperty(PLUGIN_UNSAFE);
            unsafePluginDescriptor.setUnsafe(unsafeValue != null ? Boolean.parseBoolean(unsafeValue) : false);
        }
        Util.validate(descriptor);
        return descriptor;
    }

    @Override
    protected DefaultPluginDescriptor createPluginDescriptorInstance() {
        return new UnsafePluginDescriptor();
    }
}
