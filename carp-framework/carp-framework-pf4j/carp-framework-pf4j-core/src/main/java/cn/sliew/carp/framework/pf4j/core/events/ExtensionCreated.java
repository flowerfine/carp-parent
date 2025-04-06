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
package cn.sliew.carp.framework.pf4j.core.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ExtensionCreated extends ApplicationEvent {

    private final String beanName;
    private final Object bean;
    private final Class<?> beanClass;

    /**
     * Emitted whenever an extension is created and injected into the service application context.
     *
     * @param source    The object that created the event
     * @param beanName  The name of the extension bean in Spring's context
     * @param bean      The extension object itself
     * @param beanClass The extension class
     */
    public ExtensionCreated(Object source, String beanName, Object bean, Class<?> beanClass) {
        super(source);
        this.beanName = beanName;
        this.bean = bean;
        this.beanClass = beanClass;
    }
}
