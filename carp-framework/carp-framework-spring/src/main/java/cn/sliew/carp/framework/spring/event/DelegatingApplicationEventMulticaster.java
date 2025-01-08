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
package cn.sliew.carp.framework.spring.event;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.core.ResolvableType;

import java.util.function.Predicate;

/**
 * 默认异步执行，可通过 {@code Sync} 注解切换到同步执行
 */
public class DelegatingApplicationEventMulticaster implements ApplicationEventMulticaster, BeanFactoryAware, BeanClassLoaderAware {

    private final ApplicationEventMulticaster syncApplicationEventMulticaster;
    private final ApplicationEventMulticaster asyncApplicationEventMulticaster;

    public DelegatingApplicationEventMulticaster(
            ApplicationEventMulticaster syncApplicationEventMulticaster,
            ApplicationEventMulticaster asyncApplicationEventMulticaster) {
        this.syncApplicationEventMulticaster = syncApplicationEventMulticaster;
        this.asyncApplicationEventMulticaster = asyncApplicationEventMulticaster;
    }

    @Override
    public void multicastEvent(ApplicationEvent event) {
        asyncApplicationEventMulticaster.multicastEvent(event);
        syncApplicationEventMulticaster.multicastEvent(event);
    }

    @Override
    public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
        asyncApplicationEventMulticaster.multicastEvent(event, eventType);
        syncApplicationEventMulticaster.multicastEvent(event, eventType);
    }

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        if (isSynchronous(listener)) {
            syncApplicationEventMulticaster.addApplicationListener(listener);
        } else {
            asyncApplicationEventMulticaster.addApplicationListener(listener);
        }
    }

    private boolean isSynchronous(ApplicationListener<?> listener) {
        if (listener.getClass().getAnnotation(Sync.class) != null) {
            return true;
        }
        if (listener instanceof InspectableApplicationListenerMethodAdapter &&
                ((InspectableApplicationListenerMethodAdapter) listener).getMethod().getAnnotation(Sync.class) != null) {
            return true;
        }
        return false;
    }

    @Override
    public void addApplicationListenerBean(String listenerBeanName) {
        // Bean-name based listeners are async-only.
        asyncApplicationEventMulticaster.addApplicationListenerBean(listenerBeanName);
    }

    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
        asyncApplicationEventMulticaster.removeApplicationListener(listener);
        syncApplicationEventMulticaster.removeApplicationListener(listener);
    }

    @Override
    public void removeApplicationListenerBean(String listenerBeanName) {
        // Bean-name based listeners are async-only.
        asyncApplicationEventMulticaster.removeApplicationListenerBean(listenerBeanName);
    }

    @Override
    public void removeApplicationListeners(Predicate<ApplicationListener<?>> predicate) {
        asyncApplicationEventMulticaster.removeApplicationListeners(predicate);
        syncApplicationEventMulticaster.removeApplicationListeners(predicate);
    }

    @Override
    public void removeApplicationListenerBeans(Predicate<String> predicate) {
        asyncApplicationEventMulticaster.removeApplicationListenerBeans(predicate);
        syncApplicationEventMulticaster.removeApplicationListenerBeans(predicate);
    }

    @Override
    public void removeAllListeners() {
        asyncApplicationEventMulticaster.removeAllListeners();
        syncApplicationEventMulticaster.removeAllListeners();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (asyncApplicationEventMulticaster instanceof BeanFactoryAware) {
            ((BeanFactoryAware) asyncApplicationEventMulticaster).setBeanFactory(beanFactory);
        }
        if (syncApplicationEventMulticaster instanceof BeanFactoryAware) {
            ((BeanFactoryAware) syncApplicationEventMulticaster).setBeanFactory(beanFactory);
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        if (asyncApplicationEventMulticaster instanceof BeanClassLoaderAware) {
            ((BeanClassLoaderAware) asyncApplicationEventMulticaster).setBeanClassLoader(classLoader);
        }
        if (syncApplicationEventMulticaster instanceof BeanClassLoaderAware) {
            ((BeanClassLoaderAware) syncApplicationEventMulticaster).setBeanClassLoader(classLoader);
        }
    }
}
