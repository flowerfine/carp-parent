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
package cn.sliew.carp.framework.kubernetes.resources;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.UnaryOperator;

@RequiredArgsConstructor
public class AbstractKubernetesResource<T extends HasMetadata> implements KubernetesResource<T> {

    protected final KubernetesClient client;
    protected final T origin;

    @Override
    public T getOrigin() {
        return origin;
    }

    @Override
    public Optional<T> get() {
        return Optional.ofNullable(client.resource(origin).get());
    }

    @Override
    public T createOrGet(UnaryOperator<T> operator) {
        return get().orElseGet(() -> {
            T resource = operator.apply(origin);
            return client.resource(resource).createOrReplace();
        });
    }

    @Override
    public T patch(UnaryOperator<T> operator) {
        return client.resource(origin).patch(operator.apply(origin));
    }

    @Override
    public boolean delete() {
        client.resource(origin).delete();
        return true;
    }
}
