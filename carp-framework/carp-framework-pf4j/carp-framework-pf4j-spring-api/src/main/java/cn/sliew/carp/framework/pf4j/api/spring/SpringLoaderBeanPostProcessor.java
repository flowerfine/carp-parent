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
package cn.sliew.carp.framework.pf4j.api.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Promotes ExposeToApp and RestControllers from a plugin to the application's context.
 */
@Slf4j
public class SpringLoaderBeanPostProcessor implements BeanPostProcessor {

    private final GenericApplicationContext pluginContext;
    private final BeanPromoter beanPromoter;

    /**
     * bean post processor
     *
     * @param pluginContext plugin context
     * @param beanPromoter  bean promoter
     */
    public SpringLoaderBeanPostProcessor(
            GenericApplicationContext pluginContext, BeanPromoter beanPromoter) {
        this.pluginContext = pluginContext;
        this.beanPromoter = beanPromoter;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            final BeanDefinition def = pluginContext.getBeanDefinition(beanName);

            // look for annotations that indicate a bean should be elevated to the service's app context
            boolean exposeToApp = false;
            // look annotations on bean class
            if (bean.getClass().isAnnotationPresent(ExposeToApp.class)
                    || bean.getClass().isAnnotationPresent(RestController.class)) {
                exposeToApp = true;
            } else {
                if (def instanceof AnnotatedBeanDefinition) {
                    final AnnotationMetadata metadata = ((AnnotatedBeanDefinition) def).getMetadata();
                    // look for annotation on an enclosing configuration
                    if (metadata.hasAnnotation(ExposeToApp.class.getName())) {
                        exposeToApp = true;
                    } else {
                        // look for annotation on the method that instantiates the bean in the enclosing
                        // configuration
                        final Set<MethodMetadata> methods =
                                metadata.getAnnotatedMethods(ExposeToApp.class.getName());
                        if (methods.stream().anyMatch(method -> method.getMethodName().equals(beanName))) {
                            exposeToApp = true;
                        }
                    }
                }
            }

            if (exposeToApp) {
                Class klass = bean.getClass();
                if (def.getBeanClassName() != null) {
                    klass = pluginContext.getClassLoader().loadClass(def.getBeanClassName());
                }
                log.debug("Adding plugin bean {} to application context", beanName);
                beanPromoter.promote(beanName, bean, klass, def.isPrimary());
            }
        } catch (ClassNotFoundException e) {
            log.error("Error loading plugin class for bean {}", beanName, e);
        }
        return bean;
    }
}
