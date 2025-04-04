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
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginManager;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * The {@link UnsafePluginClassLoader} allows a plugin to use the parent classloader. Caution should
 * be used while developing unsafe plugins, as they are given carte blanche integration into
 * the parent application.
 * <p>
 * PF4J is a little wonky in that all class loaders must be a {@link PluginClassLoader}... so this extends
 * that class, and then just delegates everything to the provided {@code ClassLoader parent}. It's a little
 * wasteful, but the only way to do things and stay in the PF4J ecosystem at the moment.
 */
public class UnsafePluginClassLoader extends PluginClassLoader {

    public UnsafePluginClassLoader(
            PluginManager pluginManager,
            UnsafePluginDescriptor pluginDescriptor,
            ClassLoader parent) {
        super(pluginManager, pluginDescriptor, parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return getParent().loadClass(name);
    }

    @Override
    public URL getResource(String name) {
        return getParent().getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return getParent().getResources(name);
    }

    @Override
    public void addURL(URL url) {
        try {
            Method method = ReflectionUtils.findMethod(URLClassLoader.class, "addURL", URL.class);
            ReflectionUtils.makeAccessible(method);
            ReflectionUtils.invokeMethod(method, getParent(), url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to add URL to parent classloader", e);
        }
    }
}
