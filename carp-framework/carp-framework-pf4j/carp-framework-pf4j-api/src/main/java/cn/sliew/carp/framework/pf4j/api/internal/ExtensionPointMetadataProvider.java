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
package cn.sliew.carp.framework.pf4j.api.internal;

import java.lang.reflect.Proxy;

/**
 * ExtensionPointMetadataProvider
 */
public class ExtensionPointMetadataProvider {

    /**
     * get extension class
     *
     * @param extensionPoint extension point
     * @return extension class
     */
    public static Class<? extends CarpExtensionPoint> getExtensionClass(CarpExtensionPoint extensionPoint) {
        if (Proxy.isProxyClass(extensionPoint.getClass())) {
            ExtensionInvocationHandler extensionInvocationHandler =
                    (ExtensionInvocationHandler) Proxy.getInvocationHandler(extensionPoint);
            return extensionInvocationHandler.getTargetClass();
        }
        return extensionPoint.getClass();
    }

    /**
     * get plugin id
     *
     * @param extensionPoint extension point
     * @return plugin id
     */
    public static String getPluginId(CarpExtensionPoint extensionPoint) {
        if (Proxy.isProxyClass(extensionPoint.getClass())) {
            ExtensionInvocationHandler extensionInvocationHandler =
                    (ExtensionInvocationHandler) Proxy.getInvocationHandler(extensionPoint);
            return extensionInvocationHandler.getPluginId();
        }
        return "default";
    }
}
