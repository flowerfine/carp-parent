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
package cn.sliew.carp.framework.pekko.spring;

import org.apache.pekko.actor.AbstractExtensionId;
import org.apache.pekko.actor.ExtendedActorSystem;
import org.apache.pekko.actor.Extension;
import org.apache.pekko.actor.Props;
import org.springframework.context.ApplicationContext;

public class CarpPekkoSpringExtension
        extends AbstractExtensionId<CarpPekkoSpringExtension.SpringExt> {

    public static final CarpPekkoSpringExtension SPRING_EXTENSION_PROVIDER
            = new CarpPekkoSpringExtension();

    @Override
    public SpringExt createExtension(ExtendedActorSystem system) {
        return null;
    }

    public static class SpringExt implements Extension {
        private volatile ApplicationContext applicationContext;

        public void initialize(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        public Props props(String actorBeanName) {
            return Props.create(CarpPekkoSpringActorProducer.class, applicationContext, actorBeanName);
        }

        public Props props(String actorBeanName, Object... args) {
            return Props.create(CarpPekkoSpringActorProducer.class, applicationContext, actorBeanName, args);
        }
    }
}
