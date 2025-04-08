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
package cn.sliew.carp.framework.socketio.configuration;

import cn.sliew.carp.framework.socketio.annotation.CarpSocketIoNamespace;
import cn.sliew.carp.framework.socketio.listener.CarpConnectionListener;
import cn.sliew.carp.framework.socketio.repository.SocketIORepository;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 优化 SpringAnnotationScanner
 */
@Slf4j
public class SocketIONamespaceBeanPostProcessor implements BeanPostProcessor {

    private final List<Class<? extends Annotation>> annotations =
            Arrays.asList(OnConnect.class, OnDisconnect.class, OnEvent.class);

    private final SocketIOServer socketIOServer;
    private final SocketIORepository socketIORepository;
    private Class originalBeanClass;
    private Object originalBean;
    private String originalBeanName;

    public SocketIONamespaceBeanPostProcessor(SocketIOServer socketIOServer, SocketIORepository socketIORepository) {
        super();
        this.socketIOServer = socketIOServer;
        this.socketIORepository = socketIORepository;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (originalBeanClass != null) {
            addListeners(originalBean, originalBeanClass, originalBeanName);
            originalBeanClass = null;
            originalBean = null;
            originalBeanName = null;
        }
        return bean;
    }

    private void addListeners(Object bean, Class<?> beanClass, String beanName) {
        CarpSocketIoNamespace annotation = AnnotationUtils.findAnnotation(beanClass, CarpSocketIoNamespace.class);
        if (Objects.isNull(annotation)) {
            socketIOServer.addListeners(bean, beanClass);
            log.debug("Socket.IO [{}] bean listeners added to [default] namespace", beanName);
        } else {
            if (socketIOServer.getAllNamespaces().contains(annotation.namespace()) == false) {
                socketIOServer.addNamespace(annotation.namespace());
            }
            SocketIONamespace namespace = socketIOServer.getNamespace(annotation.namespace());
            if (bean instanceof CarpConnectionListener listener) {
                listener.setNamespace(namespace);
                listener.setRepository(socketIORepository);
            }
            namespace.addListeners(bean, beanClass);
            log.debug("Socket.IO [{}] bean listeners added to [{}] namespace", beanName, annotation.namespace());
        }
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        final AtomicBoolean add = new AtomicBoolean();
        ReflectionUtils.doWithMethods(
                bean.getClass(),
                method -> add.set(true),
                method -> {
                    for (Class<? extends Annotation> annotationClass : annotations) {
                        if (method.isAnnotationPresent(annotationClass)) {
                            return true;
                        }
                    }
                    return false;
                });

        if (add.get()) {
            originalBeanClass = bean.getClass();
            originalBean = bean;
            originalBeanName = beanName;
        }
        return bean;
    }
}
