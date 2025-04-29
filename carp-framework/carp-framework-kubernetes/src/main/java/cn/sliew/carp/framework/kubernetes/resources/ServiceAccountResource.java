package cn.sliew.carp.framework.kubernetes.resources;

import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.client.KubernetesClient;

public class ServiceAccountResource extends AbstractKubernetesResource<ServiceAccount> {

    public ServiceAccountResource(KubernetesClient client, ServiceAccount origin) {
        super(client, origin);
    }
}
