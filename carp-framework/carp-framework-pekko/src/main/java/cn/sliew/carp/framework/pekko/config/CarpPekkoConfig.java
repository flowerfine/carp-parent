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
package cn.sliew.carp.framework.pekko.config;

import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.SpawnProtocol;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class CarpPekkoConfig {

    @Primary
    @Bean(destroyMethod = "terminate")
    public ActorSystem<SpawnProtocol.Command> actorSystem() {
        ActorSystem<SpawnProtocol.Command> actorSystem = ActorSystem.create(Behaviors.setup(ctx -> SpawnProtocol.create()), "carp");
        actorSystem.whenTerminated().onComplete(done -> {
            if (done.isSuccess()) {
                actorSystem.log().info("ActorSystem terminate success!");
            } else {
                actorSystem.log().error("ActorSystem terminate failure!", done.failed().get());
            }
            return done.get();
        }, actorSystem.executionContext());

        // https://github.com/apache/pekko-persistence-jdbc/blob/main/core/src/main/resources/schema/mysql/mysql-create-schema.sql
//        CompletionStage<Done> createPersistenceSchemaFuture = SchemaUtils.createIfNotExists(actorSystem);
//        createPersistenceSchemaFuture.whenComplete((unused, throwable) -> {
//           if (throwable != null) {
//               actorSystem.log().error("try create pekko persistence jdbc schema if not exists exception!", throwable);
//           } else {
//               actorSystem.log().info("try create pekko persistence jdbc schema if not exists finish");
//           }
//        });

        return actorSystem;
    }
}
