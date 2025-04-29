package cn.sliew.carp.framework.kubernetes.resources;

import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.client.KubernetesClient;

public class RoleBindingResource extends AbstractKubernetesResource<RoleBinding> {

    public RoleBindingResource(KubernetesClient client, RoleBinding origin) {
        super(client, origin);
    }
}
