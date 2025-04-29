package cn.sliew.carp.framework.kubernetes.resources;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;

public class SecretResource extends AbstractKubernetesResource<Secret> {

    public SecretResource(KubernetesClient client, Secret origin) {
        super(client, origin);
    }
}
