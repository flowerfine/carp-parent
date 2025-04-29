package cn.sliew.carp.framework.kubernetes.resources;

import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.client.KubernetesClient;

public class RoleResource extends AbstractKubernetesResource<Role> {

    public RoleResource(KubernetesClient client, Role origin) {
        super(client, origin);
    }
}
