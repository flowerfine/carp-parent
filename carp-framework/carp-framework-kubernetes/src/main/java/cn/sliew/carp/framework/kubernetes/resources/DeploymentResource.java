package cn.sliew.carp.framework.kubernetes.resources;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;

public class DeploymentResource extends AbstractKubernetesResource<Deployment> {

    public DeploymentResource(KubernetesClient client, Deployment origin) {
        super(client, origin);
    }
}
