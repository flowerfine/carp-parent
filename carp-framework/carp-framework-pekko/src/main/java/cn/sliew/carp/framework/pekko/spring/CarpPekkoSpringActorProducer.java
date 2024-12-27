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

import org.apache.pekko.actor.Actor;
import org.apache.pekko.actor.IndirectActorProducer;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

/**
 * 参考 https://www.baeldung.com/akka-with-spring
 */
public class CarpPekkoSpringActorProducer implements IndirectActorProducer {

    private ApplicationContext applicationContext;
    private String beanActorName;
    private Object[] args;

    public CarpPekkoSpringActorProducer(ApplicationContext applicationContext, String beanActorName) {
        this(applicationContext, beanActorName, null);
    }

    public CarpPekkoSpringActorProducer(ApplicationContext applicationContext, String beanActorName, Object... args) {
        this.applicationContext = applicationContext;
        this.beanActorName = beanActorName;
        this.args = args;
    }

    @Override
    public Actor produce() {
        if (Objects.nonNull(args) && args.length > 0) {
            return (Actor) applicationContext.getBean(beanActorName, args);
        } else {
            return (Actor) applicationContext.getBean(beanActorName);
        }
    }

    @Override
    public Class<? extends Actor> actorClass() {
        return (Class<? extends Actor>) applicationContext.getType(beanActorName);
    }
}
