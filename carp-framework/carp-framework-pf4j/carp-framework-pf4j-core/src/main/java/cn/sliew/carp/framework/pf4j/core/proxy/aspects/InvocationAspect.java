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
package cn.sliew.carp.framework.pf4j.core.proxy.aspects;

import cn.sliew.carp.framework.pf4j.api.internal.CarpExtensionPoint;
import cn.sliew.carp.framework.pf4j.core.pf4j.finders.UnsafePluginDescriptor;
import cn.sliew.carp.framework.pf4j.core.proxy.ExtensionInvocationProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An aspect to use with {@link ExtensionInvocationProxy}, allows for storing state about an invocation
 * and accessing that state on {@link #error} and {@link #after} the invocation.
 */
public interface InvocationAspect<I extends InvocationState> {

    /**
     * Determines if the instance supports the specified {@link InvocationState} type.
     *
     * @param invocationState The {@link InvocationState} type that this aspect supports
     */
    boolean supports(Class<I> invocationState);

    /**
     * Called prior to method invocation.
     * <p>
     * The params {@code proxy}, {@code method}, and {@code args} are documented at {@link java.lang.reflect.InvocationHandler}
     *
     * @param target     The target object that is being proxied
     * @param descriptor The {@link UnsafePluginDescriptor} provides metadata about the plugin
     * @return I The {@link InvocationState} instance, which is used to store state about the invocation.
     */
    I before(CarpExtensionPoint target, Object proxy, Method method, Object[] args, UnsafePluginDescriptor descriptor);

    /**
     * After method invocation. Called immediately after invoking the method.
     *
     * @param invocationState The state object created via {@link #before}
     */
    void after(I invocationState);

    /**
     * If the method invocation threw an InvocationTargetException, apply some additional processing if
     * desired.  Called in a catch block.
     *
     * @param e               InvocationTargetException which is thrown via
     * @param invocationState The invocationState object created via {@link #before}
     */
    void error(InvocationTargetException e, I invocationState);

    /**
     * Called last and always called, regardless of invocation success or failure.
     * <p>
     * Optional, default implementation is a no-op.
     *
     * @param invocationState The invocationState object created via {@link #before}
     */
    default void finally_(I invocationState) { /* default implementation */ }
}
