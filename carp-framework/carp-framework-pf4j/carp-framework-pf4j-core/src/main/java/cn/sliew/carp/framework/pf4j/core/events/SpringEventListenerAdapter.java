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

import cn.sliew.carp.framework.pf4j.api.events.CarpEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Adapts Spring {@link ApplicationEvent} events to the plugin-friendly {@link CarpEventListener} event type.
 */
@Slf4j
public class SpringEventListenerAdapter implements ApplicationListener<ApplicationEvent> {

    private final CarpEventListener<?> eventListener;
    private final Class<?> interestedType;

    public SpringEventListenerAdapter(CarpEventListener<?> eventListener) {
        this.eventListener = eventListener;
        this.interestedType = findEventType(eventListener);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (interestedType != null && interestedType.isAssignableFrom(event.getClass())) {
            Method method = ReflectionUtils.findMethod(eventListener.getClass(), "onApplicationEvent", interestedType);
            ReflectionUtils.makeAccessible(method);
            ReflectionUtils.invokeMethod(method, eventListener, event);
        }
    }

    /**
     * Finds the event type for a {@link CarpEventListener} class.
     * <p>
     * It's not enough to just look at the immediate interfaces of the type, since it's possible a plugin developer
     * will make an abstract class for all event listeners.
     */
    private Class<?> findEventType(CarpEventListener<?> eventListener) {
        try {
            ResolvableType eventListenerType = ResolvableType.forInstance(eventListener);
            return findEventType(eventListenerType);
        } catch (Exception e) {
            log.warn(
                    "Unable to resolve event type for listener, all events will be ignored: {}",
                    eventListener.getExtensionClass().getSimpleName()
            );
            return null;
        }
    }

    private Class<?> findEventType(ResolvableType type) {
        if (!type.getSuperType().getRawClass().getName().equals("java.lang.Object")) {
            return findEventType(type.getSuperType());
        }

        for (ResolvableType interfaceType : type.getInterfaces()) {
            if (CarpEventListener.class.isAssignableFrom(interfaceType.getRawClass())) {
                return interfaceType.getGeneric(0).getRawClass();
            }
        }
        return null;
    }
}
