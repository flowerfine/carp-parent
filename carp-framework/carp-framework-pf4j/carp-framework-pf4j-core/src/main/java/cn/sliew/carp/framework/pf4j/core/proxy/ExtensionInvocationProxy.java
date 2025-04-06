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
import cn.sliew.carp.framework.pf4j.core.proxy.aspects.InvocationState;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The invocation proxy for extensions.  Supports a list of {@link InvocationAspect} objects which
 * provides a pattern for instrumenting method invocation.
 */
public class ExtensionInvocationProxy implements ExtensionInvocationHandler {

    private final CarpExtensionPoint target;
    private final List<InvocationAspect> invocationAspects;
    private final UnsafePluginDescriptor pluginDescriptor;

    public ExtensionInvocationProxy(
            CarpExtensionPoint target,
            List<InvocationAspect> invocationAspects,
            UnsafePluginDescriptor pluginDescriptor) {
        this.target = target;
        this.invocationAspects = invocationAspects;
        this.pluginDescriptor = pluginDescriptor;
    }

    /**
     * Target class is exposed here so we can determine extension type via
     */
    @Override
    public Class<? extends CarpExtensionPoint> getTargetClass() {
        return target.getClass();
    }

    @Override
    public String getPluginId() {
        return pluginDescriptor.getPluginId();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Set<InvocationState> invocationStates = new HashSet<>();
        before(invocationStates, proxy, method, args);

        Object result;
        try {
            result = method.invoke(target, args != null ? args : new Object[0]);
            after(invocationStates);
        } catch (InvocationTargetException e) {
            error(invocationStates, e);
            throw e.getCause() != null ? e.getCause() :
                    new RuntimeException("Caught invocation target exception without cause.", e);
        } finally {
            doFinally(invocationStates);
        }

        return result;
    }

    private void before(Set<InvocationState> states, Object proxy, Method method, Object[] args) {
        invocationAspects.forEach(aspect ->
                states.add(aspect.before(target, proxy, method, args, pluginDescriptor))
        );
    }

    private void error(Set<InvocationState> states, InvocationTargetException e) {
        states.forEach(state ->
                invocationAspects.forEach(aspect -> {
                    if (aspect.supports(state.getClass())) {
                        aspect.error(e, state);
                    }
                })
        );
    }

    private void after(Set<InvocationState> states) {
        states.forEach(state ->
                invocationAspects.forEach(aspect -> {
                    if (aspect.supports(state.getClass())) {
                        aspect.after(state);
                    }
                })
        );
    }

    private void doFinally(Set<InvocationState> states) {
        states.forEach(state ->
                invocationAspects.forEach(aspect -> {
                    if (aspect.supports(state.getClass())) {
                        aspect.finally_(state);
                    }
                })
        );
    }

    /**
     * Factory method for wrapping a {@link CarpExtensionPoint} in an {@link ExtensionInvocationProxy}.
     */
    public static Object proxy(
            CarpExtensionPoint target,
            List<InvocationAspect> invocationAspects,
            UnsafePluginDescriptor descriptor) {
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new ExtensionInvocationProxy(target, invocationAspects, descriptor)
        );
    }
}
