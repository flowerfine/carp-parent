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
package cn.sliew.carp.framework.pf4j.api;

import javax.annotation.Nonnull;
import java.lang.annotation.*;

/**
 * Denotes that a class provides extension configuration. For example:
 *
 * <pre>{@code
 * &#064;ExtensionConfiguration("my-extension")
 * public class MyExtensionConfiguration {
 *   private String someProperty;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Deprecated
public @interface ExtensionConfiguration {

    /**
     * The property value of the extension configuration. For example, if set to `netflix.orca-stage`
     * the corresponding config coordinates would be:
     *
     * <p>`carp.extensibility.plugins.pluginId.extensions.netflix.orca-stage.config`
     *
     * @return
     */
    @Nonnull
    String value();
}
