package cn.sliew.carp.framework.kubernetes.resources;

import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.client.KubernetesClient;

public class JobResource extends AbstractKubernetesResource<Job> {

    public JobResource(KubernetesClient client, Job origin) {
        super(client, origin);
    }
}
