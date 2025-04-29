package cn.sliew.carp.framework.kubernetes.resources;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;

public class ServiceResource extends AbstractKubernetesResource<Service> {

    public ServiceResource(KubernetesClient client, Service origin) {
        super(client, origin);
    }
}
