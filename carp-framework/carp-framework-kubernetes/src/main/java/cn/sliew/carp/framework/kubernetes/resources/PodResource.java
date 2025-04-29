package cn.sliew.carp.framework.kubernetes.resources;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;

public class PodResource extends AbstractKubernetesResource<Pod> {

    public PodResource(KubernetesClient client, Pod origin) {
        super(client, origin);
    }
}
