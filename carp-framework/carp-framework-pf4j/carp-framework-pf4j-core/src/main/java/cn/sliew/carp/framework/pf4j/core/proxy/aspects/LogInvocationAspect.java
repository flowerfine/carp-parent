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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Logs the invoked extension and method name.  Additionally adds the plugin-id and plugin-extension
 * to the MDC.
 * <p>
 * There is room for improvement here: The logger name could be mapped to the extension class and
 * there could be additional details logged (like arguments) based on configuration.
 */
@Slf4j
public class LogInvocationAspect implements InvocationAspect<InvocationState.LogInvocationState> {

    public static final String PLUGIN_ID = "PLUGIN_ID";
    public static final String PLUGIN_EXTENSION = "PLUGIN_EXTENSION";

    @Override
    public boolean supports(Class<InvocationState.LogInvocationState> invocationState) {
        return Objects.equals(InvocationState.LogInvocationState.class, invocationState);
    }

    @Override
    public InvocationState.LogInvocationState before(
            CarpExtensionPoint target,
            Object proxy,
            Method method,
            Object[] args,
            UnsafePluginDescriptor descriptor) {

        InvocationState.LogInvocationState logInvocationState = new InvocationState.LogInvocationState(
                target.getClass().getSimpleName(),
                method.getName()
        );

        setOrRemoveMdc(PLUGIN_ID, descriptor.getPluginId());
        setOrRemoveMdc(PLUGIN_EXTENSION, logInvocationState.getExtensionName());

        log.trace(
                "Invoking method={} on extension={}",
                logInvocationState.getMethodName(),
                logInvocationState.getExtensionName()
        );

        return logInvocationState;
    }

    @Override
    public void after(InvocationState.LogInvocationState invocationState) {
        log.trace(
                "Successful execution of method={} on extension={}",
                invocationState.getExtensionName(),
                invocationState.getMethodName()
        );
    }

    @Override
    public void error(InvocationTargetException e, InvocationState.LogInvocationState invocationState) {
        log.error(
                "Error invoking method={} on extension={}",
                invocationState.getMethodName(),
                invocationState.getExtensionName(),
                e.getCause()
        );
    }

    @Override
    public void finally_(InvocationState.LogInvocationState invocationState) {
        MDC.remove(PLUGIN_ID);
        MDC.remove(PLUGIN_EXTENSION);
    }

    private void setOrRemoveMdc(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        } else {
            MDC.remove(key);
        }
    }
}
