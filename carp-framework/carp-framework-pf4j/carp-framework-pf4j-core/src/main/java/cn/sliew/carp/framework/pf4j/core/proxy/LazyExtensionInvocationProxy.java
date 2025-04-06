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
package cn.sliew.carp.framework.pf4j.core.proxy;

import cn.sliew.carp.framework.pf4j.api.internal.CarpExtensionPoint;
import cn.sliew.carp.framework.pf4j.api.internal.ExtensionInvocationHandler;
import cn.sliew.carp.framework.pf4j.core.pf4j.finders.UnsafePluginDescriptor;
import cn.sliew.carp.framework.pf4j.core.proxy.aspects.InvocationAspect;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Supplier;

/**
 * A wrapper around {@link ExtensionInvocationProxy}.
 * The provided {@code target} is resolved at method-call time.
 * <p>
 * Used by {@link UnsafePluginDescriptor} to define
 * beans that are injected early in Spring's lifecycle but whose implementations
 * are resolved late - only when the extension's methods are called for the first time.
 */
public class LazyExtensionInvocationProxy implements ExtensionInvocationHandler {

    private final Supplier<CarpExtensionPoint> target;
    private final Class<? extends CarpExtensionPoint> targetClass;
    private final List<InvocationAspect> invocationAspects;
    private final UnsafePluginDescriptor descriptor;
    private volatile ExtensionInvocationProxy delegate;

    public LazyExtensionInvocationProxy(
            Supplier<CarpExtensionPoint> target,
            Class<? extends CarpExtensionPoint> targetClass,
            List<InvocationAspect> invocationAspects,
            UnsafePluginDescriptor descriptor) {
        this.target = target;
        this.targetClass = targetClass;
        this.invocationAspects = invocationAspects;
        this.descriptor = descriptor;
    }

    private ExtensionInvocationProxy getDelegate() {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    delegate = new ExtensionInvocationProxy(target.get(), invocationAspects, descriptor);
                }
            }
        }
        return delegate;
    }

    @Override
    public Class<? extends CarpExtensionPoint> getTargetClass() {
        return targetClass;
    }

    @Override
    public String getPluginId() {
        return descriptor.getPluginId();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return getDelegate().invoke(proxy, method, args);
    }

    /**
     * Factory method for wrapping a {@link CarpExtensionPoint} in a {@link LazyExtensionInvocationProxy}.
     */
    public static Object proxy(
            Supplier<CarpExtensionPoint> target,
            Class<? extends CarpExtensionPoint> targetClass,
            List<InvocationAspect> invocationAspects,
            UnsafePluginDescriptor descriptor) {
        return Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                targetClass.getInterfaces(),
                new LazyExtensionInvocationProxy(target, targetClass, invocationAspects, descriptor)
        );
    }
}
