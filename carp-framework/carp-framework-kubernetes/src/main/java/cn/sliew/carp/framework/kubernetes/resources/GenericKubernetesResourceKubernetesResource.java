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

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.Serialization;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class GenericKubernetesResourceKubernetesResource implements KubernetesResource<GenericKubernetesResource> {

    protected final KubernetesClient client;
    protected final GenericKubernetesResource origin;
    protected final VersionGroupKind versionAndGroup;

    public GenericKubernetesResourceKubernetesResource(KubernetesClient client, GenericKubernetesResource origin) {
        this.client = client;
        this.origin = origin;
        this.versionAndGroup = new VersionGroupKind();
        versionAndGroup.setApiVersion(origin.getApiVersion());
        versionAndGroup.setKind(origin.getKind());
        versionAndGroup.setName(origin.getMetadata().getName());
        versionAndGroup.setNamespace(origin.getMetadata().getNamespace());
    }

    @Override
    public GenericKubernetesResource getOrigin() {
        return origin;
    }

    @Override
    public Optional<GenericKubernetesResource> get() {
        GenericKubernetesResource resource = client.genericKubernetesResources(versionAndGroup.getApiVersion(), versionAndGroup.getKind())
                .inNamespace(versionAndGroup.getNamespace())
                .withName(versionAndGroup.getName())
                .get();
        return Optional.ofNullable(resource);
    }

    @Override
    public GenericKubernetesResource createOrGet(UnaryOperator<GenericKubernetesResource> operator) {
        return get().orElseGet(() -> {
            GenericKubernetesResource resource = operator.apply(origin);
            String yaml = Serialization.asYaml(resource);
            client.load(new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8))).createOrReplace();
            return resource;
        });
    }

    @Override
    public GenericKubernetesResource patch(UnaryOperator<GenericKubernetesResource> operator) {
        return client.genericKubernetesResources(versionAndGroup.getApiVersion(), versionAndGroup.getKind())
                .inNamespace(versionAndGroup.getNamespace())
                .withName(versionAndGroup.getName())
                .patch(operator.apply(origin));
    }

    @Override
    public boolean delete() {
        List<StatusDetails> statusDetails = client.genericKubernetesResources(versionAndGroup.getApiVersion(), versionAndGroup.getKind())
                .inNamespace(versionAndGroup.getNamespace())
                .withName(versionAndGroup.getName())
                .delete();
        return statusDetails.isEmpty();
    }
}
