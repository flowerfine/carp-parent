package cn.sliew.carp.framework.kubernetes.resources;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;

public class ConfigMapResource extends AbstractKubernetesResource<ConfigMap> {

    public ConfigMapResource(KubernetesClient client, ConfigMap origin) {
        super(client, origin);
    }
}
