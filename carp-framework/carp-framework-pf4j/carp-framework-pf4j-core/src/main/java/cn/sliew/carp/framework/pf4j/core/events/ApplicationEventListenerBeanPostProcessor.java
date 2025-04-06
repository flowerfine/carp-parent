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

import cn.sliew.carp.framework.pf4j.api.events.Async;
import cn.sliew.carp.framework.pf4j.api.events.CarpEventListener;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Adapts {@link CarpEventListener} instances to Spring's {@link ApplicationListener}.
 * <p>
 * This seems to be the least invasive method of registering application event listeners for plugins. It would've been
 * nice to use a facade `EventListener` annotation, but Spring's processing for this annotation is closed to the
 * modifications needed to make it work easily. The side effect of creating an adapter here is that plugin event
 * listeners will be invoked more often than they need to; deferring filtering of events to the adapter itself, rather
 * than using Spring to do this for us.
 */
public class ApplicationEventListenerBeanPostProcessor implements BeanPostProcessor {

    @Override
    @SuppressWarnings("unchecked")
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (CarpEventListener.class.isAssignableFrom(bean.getClass())) {
            if (AnnotationUtils.findAnnotation(bean.getClass(), Async.class) == null) {
                return new AsyncSpringEventListenerAdapter((CarpEventListener<?>) bean);
            } else {
                return new SpringEventListenerAdapter((CarpEventListener<?>) bean);
            }
        }
        return bean;
    }
}
